/**
 * 移动端全局布局（umi 约定式 src/layouts/index.tsx）
 *
 * - 仅 4 个 TabBar 主路由（/dashboard /apps /message /profile）显示底部 TabBar。
 * - 其余路由（登录、列表、详情等）全屏，无 TabBar，靠页面内 NavBar 返回。
 * - 底部 SafeArea 适配 iPhone home 条。
 */
import {
  AppOutline,
  MessageOutline,
  UnorderedListOutline,
  UserOutline,
} from 'antd-mobile-icons';
import { TabBar } from 'antd-mobile';
import { useLocation, history } from '@umijs/max';
import React from 'react';

const TABS = [
  { key: '/dashboard', title: '首页', icon: <AppOutline /> },
  { key: '/apps', title: '应用', icon: <UnorderedListOutline /> },
  { key: '/message', title: '消息', icon: <MessageOutline /> },
  { key: '/profile', title: '我的', icon: <UserOutline /> },
];

const TAB_PATHS = new Set(TABS.map((t) => t.key));

const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
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
      <div style={{ flex: 1, overflowY: 'auto', WebkitOverflowScrolling: 'touch' }}>
        {children}
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
