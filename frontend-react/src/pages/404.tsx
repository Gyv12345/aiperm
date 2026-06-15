import { Button, Result } from 'antd';
import { history } from '@umijs/max';
import React from 'react';

const NoFoundPage: React.FC = () => (
  <Result
    status="404"
    title="404"
    subTitle="抱歉，您访问的页面不存在。"
    extra={
      <Button type="primary" onClick={() => history.push('/welcome')}>
        返回首页
      </Button>
    }
  />
);

export default NoFoundPage;
