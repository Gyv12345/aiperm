/**
 * 工作台
 *
 * 说明：
 * - 工作台是登录后的默认首页，对所有已登录用户开放，不依赖任何菜单权限。
 * - 内容为静态展示（欢迎语 + 系统简介），不调用后端统计接口，
 *   避免因无菜单权限或接口缺失导致页面异常。
 * - 当前用户信息来自 getInitialState（/auth/info），仅用于欢迎语。
 */
import { PageContainer, ProCard, StatisticCard } from '@ant-design/pro-components';
import { Col, Row, Tag, Typography } from 'antd';
import React from 'react';
import { useModel } from '@umijs/max';

const { Paragraph, Title } = Typography;

const Dashboard: React.FC = () => {
  const { initialState } = useModel('@@initialState');
  const user = initialState?.currentUser;
  const displayName = user?.nickname || user?.username || '用户';
  const roles = user?.roles ?? [];
  const isSuperAdmin = roles.includes('admin');

  const greeting = (() => {
    const h = new Date().getHours();
    if (h < 6) return '凌晨好';
    if (h < 9) return '早上好';
    if (h < 12) return '上午好';
    if (h < 14) return '中午好';
    if (h < 18) return '下午好';
    return '晚上好';
  })();

  return (
    <PageContainer>
      <ProCard
        bordered
        style={{ marginBottom: 16 }}
        bodyStyle={{ padding: 24 }}
      >
        <Title level={4} style={{ marginBottom: 4 }}>
          {greeting}，{displayName} 👋
        </Title>
        <Paragraph type="secondary" style={{ marginBottom: 8 }}>
          欢迎使用 AIPerm 权限管理系统，祝您工作愉快。
        </Paragraph>
        {isSuperAdmin && <Tag color="gold">超级管理员</Tag>}
        {roles
          .filter((r) => r !== 'admin')
          .map((r) => (
            <Tag key={r} color="blue">
              {r}
            </Tag>
          ))}
      </ProCard>

      <Row gutter={16}>
        <Col xs={24} sm={12} md={8}>
          <StatisticCard
            statistic={{
              title: '技术后端',
              value: 'Spring Boot 4',
              description: (
                <Paragraph type="secondary" style={{ marginBottom: 0, fontSize: 12 }}>
                  Sa-Token 鉴权 · JPA · Flyway · Redis
                </Paragraph>
              ),
            }}
          />
        </Col>
        <Col xs={24} sm={12} md={8}>
          <StatisticCard
            statistic={{
              title: '技术前端',
              value: 'React + UmiJS',
              description: (
                <Paragraph type="secondary" style={{ marginBottom: 0, fontSize: 12 }}>
                  Ant Design Pro · Orval · TypeScript
                </Paragraph>
              ),
            }}
          />
        </Col>
        <Col xs={24} sm={12} md={8}>
          <StatisticCard
            statistic={{
              title: '权限模型',
              value: 'RBAC',
              description: (
                <Paragraph type="secondary" style={{ marginBottom: 0, fontSize: 12 }}>
                  角色 · 菜单 · 按钮 · 数据权限
                </Paragraph>
              ),
            }}
          />
        </Col>
      </Row>

      <ProCard title="系统简介" style={{ marginTop: 16 }}>
        <Paragraph>
          AIPerm（Architectural Ledger）是一套基于 Spring Boot 4 + Sa-Token 的 RBAC
          权限管理系统，提供用户、角色、菜单、部门、岗位、字典等完整后台能力，
          并集成监控、定时任务、消息通知、文件管理等企业级模块。
        </Paragraph>
        <Paragraph type="secondary" style={{ marginBottom: 0 }}>
          提示：具体可见功能取决于当前账号分配的菜单与按钮权限，请联系管理员开通。
        </Paragraph>
      </ProCard>
    </PageContainer>
  );
};

export default Dashboard;
