/**
 * umi/max 运行时配置
 *
 * - getInitialState：从 localStorage 读 token，已登录则拉取用户信息与菜单。
 * - request：接入 src/requestErrorConfig.ts（R<T> 解包、401/423、Authorization 无 Bearer）。
 * - layout：ProLayout 配置 + 后端菜单渲染 + 用户下拉 + MFA 监听。
 *
 * 文档：https://umijs.org/docs/max/runtime-config
 */
import type { MenuDataItem } from '@ant-design/pro-components';
import {
  LogoutOutlined,
  SettingOutlined,
  UserOutlined,
} from '@ant-design/icons';
import type { RunTimeLayoutConfig } from '@umijs/max';
import { RuntimeConfig as RuntimeConfigTypes, history } from '@umijs/max';
import { App as AntdApp, Dropdown, message } from 'antd';
import type { ReactNode } from 'react';
import React from 'react';
import MfaVerifyModal from './components/MfaVerifyModal';
import { getToken, requestConfig, setToken } from './requestErrorConfig';
import {
  getMenus,
  getUserInfo,
  logout as doLogout,
  type MenuNode,
  type UserInfo,
} from './services/auth';

const loginPath = '/user/login';

/** 全局初始状态类型 */
export type InitialState = {
  currentUser?: UserInfo;
  menus?: MenuNode[];
  fetchUserInfo?: () => Promise<UserInfo | undefined>;
  fetchMenus?: () => Promise<MenuNode[] | undefined>;
  logout?: () => Promise<void>;
};

/**
 * 判断是否为按钮类型（不进菜单）。
 * 后端实际返回 menuType 为 "1"=目录 / "2"=菜单 / "3"=按钮（数字字符串），
 * OpenAPI schema 标注为 M/C/F，这里两种都兼容。
 */
function isButton(menuType: string | undefined): boolean {
  return menuType === '3' || menuType === 'F' || menuType === 'f';
}

/** 把单个 path 规整为以 / 开头 */
function normalizePath(p: string | null | undefined): string {
  let path = (p || '').trim();
  if (!path) return '';
  if (!path.startsWith('/')) path = `/${path}`;
  return path;
}

/**
 * 把后端菜单树转为 ProLayout 的 MenuDataItem
 * - 仅渲染目录(1/M)与菜单(2/C)，按钮(3/F)不进菜单。
 * - 子菜单 path 是相对路径，需与父级拼接成完整路径（/system + user → /system/user）。
 * - ProLayout 会对 path 调用 .endsWith，path 不能为 null/空，
 *   目录无 path 时用占位，避免 mergePath 崩溃。
 */
function toMenuData(
  nodes: MenuNode[] | undefined,
  parentPath = '',
): MenuDataItem[] {
  if (!nodes) return [];
  return nodes
    .filter((n) => !isButton(n.menuType))
    .sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
    .map((n) => {
      // 拼接完整路径：父级绝对路径 + 子级相对路径
      const rawPath = normalizePath(n.path);
      let path: string;
      if (!rawPath) {
        // 目录无 path：用占位
        path = parentPath ? `${parentPath}/menu-${n.id}` : `/menu-${n.id}`;
      } else if (rawPath.startsWith('/')) {
        // 子级是绝对路径，直接用
        path = rawPath;
      } else {
        // 子级是相对路径，与父级拼接
        path = `${parentPath}${rawPath}`;
      }
      const item: MenuDataItem = {
        key: String(n.id),
        name: n.menuName,
        path,
        icon: n.icon,
      };
      const children = toMenuData(n.children, path);
      if (children.length) item.children = children;
      return item;
    });
}

async function fetchUserInfo(): Promise<UserInfo | undefined> {
  try {
    return await getUserInfo();
  } catch {
    return undefined;
  }
}

async function fetchMenus(): Promise<MenuNode[] | undefined> {
  try {
    return await getMenus();
  } catch {
    return undefined;
  }
}

/** 全局初始状态：有 token 才拉用户信息与菜单 */
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
    fetchMenus,
    logout: logoutFn,
  };

  if (getToken()) {
    const [userInfo, menus] = await Promise.all([
      fetchUserInfo(),
      fetchMenus(),
    ]);
    state.currentUser = userInfo;
    state.menus = menus;
  }

  return state;
}

/**
 * ProLayout 运行时配置
 */
export const layout: RunTimeLayoutConfig = ({ initialState }) => {
  const { currentUser, logout } = (initialState as InitialState) ?? {};

  return {
    title: 'AIPerm',
    logo: '/logo.svg',
    siderWidth: 230,
    layout: 'mix',
    navTheme: 'light',
    fixedHeader: true,
    fixSiderbar: true,
    contentWidth: 'Fluid',
    colorPrimary: '#0060a9',
    // 动态菜单：后端菜单 + 前端补充（工作台首页、系统监控，后端菜单未含）
    menuDataRender: () => {
      const backend = toMenuData((initialState as InitialState)?.menus);
      // 前端补充菜单：固定首页 + 监控类（后端菜单未含，但接口已实现）
      const extra: MenuDataItem[] = [
        { key: 'welcome', name: '工作台', path: '/welcome' },
        {
          key: 'monitor',
          name: '系统监控',
          path: '/monitor',
          children: [
            { key: 'monitor-online', name: '在线用户', path: '/monitor/online' },
            { key: 'monitor-login-log', name: '登录日志', path: '/monitor/login-log' },
            { key: 'monitor-job-log', name: '任务日志', path: '/monitor/job-log' },
            { key: 'monitor-oper-log', name: '操作日志', path: '/monitor/oper-log' },
          ],
        },
      ];
      return [...extra, ...backend];
    },
    // 右上角用户区
    avatarProps: {
      src: currentUser?.avatar,
      size: 'small',
      title: currentUser?.nickname || currentUser?.username || '用户',
      render: (_, dom) => {
        const items = [
          {
            key: 'center',
            label: '个人中心',
            icon: <UserOutlined />,
            onClick: () => history.push('/profile'),
          },
          {
            key: 'settings',
            label: '设置',
            icon: <SettingOutlined />,
            disabled: true,
          },
          { type: 'divider' as const },
          {
            key: 'logout',
            label: '退出登录',
            icon: <LogoutOutlined />,
            onClick: async () => {
              const hide = message.loading('退出中...');
              try {
                await logout?.();
              } finally {
                hide();
              }
            },
          },
        ];
        return <Dropdown menu={{ items }}>{dom}</Dropdown>;
      },
    },
    footerRender: () => (
      <div style={{ textAlign: 'center', padding: 12, color: '#999' }}>
        AIPerm · 权限结构总览 ©{new Date().getFullYear()}
      </div>
    ),
    menuHeaderRender: undefined,
    unAccessible: <div>无权限访问</div>,
  };
};

/** 全局 request 配置 */
export const request: RuntimeConfigTypes['request'] = {
  ...requestConfig,
};

/**
 * 根运行时：用 antd App 包裹，使静态 message/modal 能消费主题 context；
 * 同时挂载全局 MFA 校验弹窗（监听 'mfa-required' 事件）。
 */
export function rootContainer(container: ReactNode): ReactNode {
  return (
    <AntdApp>
      {container}
      <MfaVerifyModal />
    </AntdApp>
  );
}
