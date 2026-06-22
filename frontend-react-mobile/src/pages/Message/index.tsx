import { Empty, NavBar } from 'antd-mobile';
import React from 'react';

const Message: React.FC = () => (
  <>
    <NavBar back={null}>消息中心</NavBar>
    <div style={{ padding: 48 }}>
      <Empty description="暂无消息（Phase 3 接入）" />
    </div>
  </>
);

export default Message;
