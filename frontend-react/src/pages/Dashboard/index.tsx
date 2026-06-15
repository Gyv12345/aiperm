/**
 * 工作台 / 仪表盘
 * 展示统计卡片：用户数、角色数、菜单数、在线用户数。
 * 数据来源：/dashboard/stats
 */
import { getStats } from '@/services/aiperm/dashboard';
import { PageContainer, ProCard, StatisticCard } from '@ant-design/pro-components';
import { Col, Row } from 'antd';
import React, { useEffect, useState } from 'react';

const Dashboard: React.FC = () => {
  const [stats, setStats] = useState<API.DashboardStatsVO>({});
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const res = await getStats();
        setStats(res || {});
      } catch {
        // 业务失败已在拦截器统一提示，这里静默兜底避免页面崩溃
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  return (
    <PageContainer>
      <Row gutter={16}>
        <Col xs={24} sm={12} md={6}>
          <StatisticCard
            statistic={{
              title: '用户总数',
              value: stats.userCount ?? '-',
              loading,
            }}
          />
        </Col>
        <Col xs={24} sm={12} md={6}>
          <StatisticCard
            statistic={{
              title: '角色数量',
              value: stats.roleCount ?? '-',
              loading,
            }}
          />
        </Col>
        <Col xs={24} sm={12} md={6}>
          <StatisticCard
            statistic={{
              title: '菜单/权限数量',
              value: stats.menuCount ?? '-',
              loading,
            }}
          />
        </Col>
        <Col xs={24} sm={12} md={6}>
          <StatisticCard
            statistic={{
              title: '在线用户',
              value: stats.onlineCount ?? '-',
              loading,
            }}
          />
        </Col>
      </Row>

      <ProCard title="系统简介" style={{ marginTop: 16 }}>
        <p>
          AIPerm（Architectural Ledger）是一套基于 Spring Boot 4 + Sa-Token 的 RBAC
          权限管理系统。本页面为其 React + Ant Design Pro 版本前端。
        </p>
        <p>
          当前已接入：登录鉴权、动态菜单、用户/角色/菜单管理、个人中心。
          审批、企业配置、监控等模块待逐步迁移（见侧边栏占位）。
        </p>
      </ProCard>
    </PageContainer>
  );
};

export default Dashboard;
