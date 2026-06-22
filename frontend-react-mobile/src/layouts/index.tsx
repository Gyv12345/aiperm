/**
 * 移动端全局布局（umi 约定式 src/layouts/index.tsx）
 *
 * 关键点：
 * - umi 4 的 isLayout 全局 layout **不通过 children prop 传子路由**，必须用
 *   <Outlet/> 让 react-router 渲染当前匹配的路由组件（用 children 会拿到
 *   undefined，导致白屏只剩 layout 骨架）。
 * - 所有路由组件均为 React.lazy，必须用 <Suspense> 包裹 <Outlet/>。
 * - 仅 4 个 TabBar 主路由（/dashboard /apps /message /profile）显示底部 TabBar，
 *   其余路由全屏，靠页面内 NavBar 返回。
 */
import {
  AppOutline,
  MessageOutline,
  UnorderedListOutline,
  UserOutline,
} from 'antd-mobile-icons';
import { TabBar } from 'antd-mobile';
import { Outlet, useLocation, history } from '@umijs/max';
import React, { Suspense } from 'react';

const TABS = [
  { key: '/dashboard', title: '首页', icon: <AppOutline /> },
  { key: '/apps', title: '应用', icon: <UnorderedListOutline /> },
  { key: '/message', title: '消息', icon: <MessageOutline /> },
  { key: '/profile', title: '我的', icon: <UserOutline /> },
];

const TAB_PATHS = new Set(TABS.map((t) => t.key));

const Layout: React.FC = () => {
  const location = useLocation();
  const showTabBar = TAB_PATHS.has(location.pathname);

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        height: '100vh',
        background: '#f5f7fa',
      }}
    >
      <div
        style={{ flex: 1, overflowY: 'auto', WebkitOverflowScrolling: 'touch' }}
      >
        <Suspense fallback={null}>
          <Outlet />
        </Suspense>
      </div>
      {showTabBar && (
        <div style={{ borderTop: '1px solid #eee', background: '#fff' }}>
          <TabBar
            activeKey={location.pathname}
            onChange={(key) => history.push(key)}
            safeArea
          >
            {TABS.map((t) => (
              <TabBar.Item key={t.key} icon={t.icon} title={t.title} />
            ))}
          </TabBar>
        </div>
      )}
    </div>
  );
};

export default Layout;
