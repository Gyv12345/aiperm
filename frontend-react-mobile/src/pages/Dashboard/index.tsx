import { Card, NavBar } from 'antd-mobile';
import React from 'react';
import { useModel } from '@umijs/max';

const Dashboard: React.FC = () => {
  const { initialState } = useModel('@@initialState');
  const name = initialState?.currentUser?.nickname ||
    initialState?.currentUser?.username ||
    '用户';

  return (
    <>
      <NavBar back={null}>AIPerm</NavBar>
      <div style={{ padding: 12 }}>
        <Card title="工作台" style={{ marginBottom: 12 }}>
          <div style={{ fontSize: 16 }}>你好，{name} 👋</div>
          <div style={{ color: '#999', fontSize: 13, marginTop: 8 }}>
            移动端管理平台 · Phase 0 骨架
          </div>
        </Card>
        <Card title="提示">
          <div style={{ color: '#999', fontSize: 13 }}>
            工作台看板将在 Phase 4 完善统计数据。
          </div>
        </Card>
      </div>
    </>
  );
};

export default Dashboard;
