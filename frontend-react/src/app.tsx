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
  AppstoreOutlined,
  BellOutlined,
  DashboardOutlined,
  DesktopOutlined,
  HomeOutlined,
  LogoutOutlined,
  MessageOutlined,
  MonitorOutlined,
  BankOutlined,
  SettingOutlined,
  TeamOutlined,
  ToolOutlined,
  UserOutlined,
} from '@ant-design/icons';
import type { RunTimeLayoutConfig } from '@umijs/max';
import { RuntimeConfig as RuntimeConfigTypes, history } from '@umijs/max';
import { App as AntdApp, Dropdown, message } from 'antd';
import type { ReactNode } from 'react';
import React from 'react';
import MfaVerifyModal from './components/MfaVerifyModal';
import { getToken, requestConfig, setToken, SILENT_BUSINESS_ERROR } from './requestErrorConfig';
import {
  getMenus,
  getUserInfo,
  logout as doLogout,
  type MenuNode,
  type UserInfo,
} from './services/auth';

const loginPath = '/user/login';

/**
 * 全局吞掉「已被提示的业务错误」的未捕获 rejection，避免 dev 下 React
 * 弹出全屏错误页。
 *
 * 背景：requestErrorConfig 的 responseInterceptor 在业务码非 200 时，会
 * message.error 提示并 reject 一个带 SILENT_BUSINESS_ERROR 标记的 Error。
 * 若调用方未 try/catch，该 rejection 会冒泡为 unhandledrejection，在
 * 开发环境触发全屏错误覆盖层。这里按标记吞掉这类 rejection（业务提示
 * 已完成，无需再上报）。其它 rejection 原样抛出，便于排查真实 bug。
 */
if (typeof window !== 'undefined') {
  window.addEventListener('unhandledrejection', (event) => {
    const reason = event.reason;
    if (reason && typeof reason === 'object' && (reason as any)[SILENT_BUSINESS_ERROR]) {
      event.preventDefault();
    }
  });
}

/**
 * 后端菜单 icon 字段 → antd 图标组件映射。
 * 后端存储为 PascalCase 且不带 Outlined 后缀（如 Setting / Monitor），
 * 这里统一映射为 @ant-design/icons 组件。未知值返回 undefined（不渲染图标）。
 */
const menuIconMap: Record<string, React.ReactNode> = {
  Setting: <SettingOutlined />,
  Bank: <BankOutlined />,
  OfficeBuilding: <BankOutlined />,
  Monitor: <MonitorOutlined />,
  Odometer: <DashboardOutlined />,
  ChatDotRound: <MessageOutlined />,
  System: <SettingOutlined />,
  User: <UserOutlined />,
  Users: <TeamOutlined />,
  Tree: <TeamOutlined />,
  Post: <AppstoreOutlined />,
  Menu: <ToolOutlined />,
  Dict: <AppstoreOutlined />,
  Log: <DesktopOutlined />,
  Server: <DesktopOutlined />,
  Cache: <DashboardOutlined />,
  Online: <UserOutlined />,
  Job: <BellOutlined />,
  Message: <MessageOutlined />,
  Notice: <BellOutlined />,
  Home: <HomeOutlined />,
};

/** 把后端 icon 字符串解析为 React 图标节点 */
function resolveMenuIcon(icon: string | undefined): React.ReactNode | undefined {
  if (!icon) return undefined;
  // 去除可能的空白与 Outlined 后缀（兼容历史数据）
  const key = icon.trim().replace(/Outlined$/i, '');
  return menuIconMap[key];
}

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
      // 区分绝对/相对路径：以后端原始 path 是否以 / 开头为准。
      // 注意必须用原始值判断——normalizePath 会给相对路径补前导 /，
      // 若在补 / 之后再判断 startsWith('/')，子级会被误判为绝对路径，
      // 从而跳过与父级的拼接（例如 user → /user 而非 /system/user）。
      const original = (n.path || '').trim();
      let path: string;
      if (!original) {
        // 目录无 path：用占位
        path = parentPath ? `${parentPath}/menu-${n.id}` : `/menu-${n.id}`;
      } else if (original.startsWith('/')) {
        // 子级是绝对路径，直接用
        path = original;
      } else {
        // 子级是相对路径，与父级拼接（顶层父级为空时补前导 /）
        path = parentPath ? `${parentPath}/${original}` : `/${original}`;
      }
      const item: MenuDataItem = {
        key: String(n.id),
        name: n.menuName,
        path,
        icon: resolveMenuIcon(n.icon),
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
    // 动态菜单：后端菜单 + 前端补充（仅工作台首页，后端菜单未含）
    // 监控中心现已由后端菜单提供（V4.8.0），不再硬编码，避免重复。
    menuDataRender: () => {
      const backend = toMenuData((initialState as InitialState)?.menus);
      const extra: MenuDataItem[] = [
        { key: 'welcome', name: '工作台', path: '/welcome' },
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
