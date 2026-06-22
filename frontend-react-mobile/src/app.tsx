/**
 * umi/max 运行时配置（移动端）
 *
 * - getInitialState：从 localStorage 读 token，已登录则拉用户信息（仅 userInfo，
 *   不拉菜单树——移动端硬编码路由）。
 * - request：接入 src/requestErrorConfig.ts。
 * - onRouteChange：登录守卫。替代桌面端 layout 插件的 onPageChange（本项目不开
 *   layout 插件，故用 umi core 的 onRouteChange）。
 * - rootContainer：注入 antd-mobile ConfigProvider（中文 locale）。
 *
 * 文档：https://umijs.org/docs/max/runtime-config
 */
import { history } from '@umijs/max';
import { ConfigProvider } from 'antd-mobile';
import zhCN from 'antd-mobile/es/locales/zh-CN';
import type { ReactNode } from 'react';
import {
  getToken,
  requestConfig,
  setToken,
  SILENT_BUSINESS_ERROR,
} from './requestErrorConfig';
import {
  getUserInfo,
  logout as doLogout,
  type UserInfo,
} from './services/auth';

const loginPath = '/user/login';

/** 全局吞掉「已被提示的业务错误」的未捕获 rejection（同桌面端） */
if (typeof window !== 'undefined') {
  window.addEventListener('unhandledrejection', (event) => {
    const reason = event.reason;
    if (
      reason &&
      typeof reason === 'object' &&
      (reason as any)[SILENT_BUSINESS_ERROR]
    ) {
      event.preventDefault();
    }
  });
}

/** 全局初始状态类型 */
export type InitialState = {
  currentUser?: UserInfo;
  fetchUserInfo?: () => Promise<UserInfo | undefined>;
  logout?: () => Promise<void>;
};

async function fetchUserInfo(): Promise<UserInfo | undefined> {
  try {
    return await getUserInfo();
  } catch {
    return undefined;
  }
}

/** 全局初始状态：有 token 才拉用户信息（不拉菜单） */
export async function getInitialState(): Promise<InitialState> {
  const logoutFn = async () => {
    try {
      await doLogout();
    } catch {
      // 忽略登出接口错误
    }
    setToken(null);
    history.push(loginPath);
  };

  const state: InitialState = {
    fetchUserInfo,
    logout: logoutFn,
  };

  if (getToken()) {
    state.currentUser = await fetchUserInfo();
  }

  return state;
}

/**
 * 登录守卫：未登录且访问非 /user/* 页面 → 跳登录页。
 * umi core 的 onRouteChange，不依赖 layout 插件。
 */
export function onRouteChange({
  location,
}: {
  location: { pathname: string };
}) {
  const pathname = location?.pathname;
  const onUserPage = !!pathname && pathname.startsWith('/user');
  if (!getToken() && !onUserPage) {
    history.replace(loginPath);
  }
}

/** 全局 request 配置（umi 运行时配置，类型由运行时匹配） */
export const request = {
  ...requestConfig,
};

/**
 * 根运行时：注入 antd-mobile ConfigProvider（中文 locale）。
 * 注：MFA 校验弹窗（MfaVerifyDialog）在 Phase 4 挂载。
 */
export function rootContainer(container: ReactNode): ReactNode {
  return <ConfigProvider locale={zhCN}>{container}</ConfigProvider>;
}
