import { Button, Result } from 'antd-mobile';
import { history } from '@umijs/max';
import React from 'react';

const NotFound: React.FC = () => (
  <Result
    status="error"
    title="页面不存在"
    description="你访问的页面走丢啦"
    style={{ padding: '64px 24px' }}
  />
);

export default NotFound;
