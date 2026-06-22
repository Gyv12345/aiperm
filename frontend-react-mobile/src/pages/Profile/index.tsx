import { Avatar, Button, Dialog, List, NavBar, Toast } from 'antd-mobile';
import React from 'react';
import { useModel } from '@umijs/max';

const Profile: React.FC = () => {
  const { initialState, setInitialState } = useModel('@@initialState');
  const user = initialState?.currentUser;

  const handleLogout = async () => {
    const ok = await Dialog.confirm({
      content: '确定退出登录？',
      confirmText: '退出',
      cancelText: '取消',
    });
    if (!ok) return;
    try {
      await initialState?.logout?.();
      await setInitialState({ ...initialState, currentUser: undefined });
      Toast.show({ content: '已退出', icon: 'success' });
    } catch {
      // 忽略
    }
  };

  return (
    <>
      <NavBar back={null}>我的</NavBar>
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          padding: 24,
          background: '#fff',
        }}
      >
        <Avatar src={user?.avatar || ''} style={{ '--size': '56px', marginRight: 16 }} />
        <div>
          <div style={{ fontSize: 17 }}>{user?.nickname || user?.username || '用户'}</div>
          <div style={{ color: '#999', fontSize: 13 }}>{user?.username}</div>
        </div>
      </div>
      <List style={{ marginTop: 12 }}>
        <List.Item description={user?.roles?.join('、') || '-'}>角色</List.Item>
        <List.Item description={`${user?.permissions?.length ?? 0} 项`}>权限数</List.Item>
      </List>
      <div style={{ padding: 24 }}>
        <Button block color="danger" onClick={handleLogout}>
          退出登录
        </Button>
      </div>
    </>
  );
};

export default Profile;
