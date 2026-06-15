import { Alert, Card } from 'antd';
import React from 'react';

/**
 * 占位页面
 * 长尾业务模块（审批/企业/监控等）在骨架阶段先挂这里，
 * 后续按模块单独迁移为 ProTable + ProForm 实现。
 */
const Placeholder: React.FC<{ title?: string }> = ({ title }) => (
  <Card>
    <Alert
      type="info"
      showIcon
      message={title || '功能开发中'}
      description="该模块已接入路由与权限，页面待从 Vue 版迁移。接口已由 OpenAPI 生成（src/services/aiperm）。"
    />
  </Card>
);

export default Placeholder;
