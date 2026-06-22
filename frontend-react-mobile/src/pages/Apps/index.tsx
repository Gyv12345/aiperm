import { Grid, NavBar } from 'antd-mobile';
import { SetOutline, UserOutline } from 'antd-mobile-icons';
import { history } from '@umijs/max';
import React from 'react';

const APPS = [
  { key: '/system/user', text: '用户管理', icon: <UserOutline /> },
  // 其余模块入口随 Phase 2/3 增补
  { key: '/profile', text: '个人中心', icon: <SetOutline /> },
];

const Apps: React.FC = () => (
  <>
    <NavBar back={null}>全部应用</NavBar>
    <Grid columns={3} gap={8} style={{ padding: 12 }}>
      {APPS.map((a) => (
        <Grid.Item
          key={a.key}
          onClick={() => history.push(a.key)}
          style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '16px 0',
            background: '#fff',
            borderRadius: 8,
          }}
        >
          <div style={{ fontSize: 24, color: '#0060a9', marginBottom: 6 }}>
            {a.icon}
          </div>
          <div style={{ fontSize: 13 }}>{a.text}</div>
        </Grid.Item>
      ))}
    </Grid>
  </>
);

export default Apps;
