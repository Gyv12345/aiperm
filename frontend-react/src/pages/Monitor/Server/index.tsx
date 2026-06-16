/**
 * 服务监控
 *
 * - 仪表盘型页面，GET /monitor/server 返回 ServerMonitorVO。
 * - 展示应用状态、运行时长、CPU、内存/磁盘使用、运行环境、健康检查。
 *
 * 纯只读，无表单/无 CRUD。
 */
import { overview as getServer } from '@/services/aiperm/monitorServer';
import { PageContainer, ProCard, StatisticCard } from '@ant-design/pro-components';
import { Button, Col, Descriptions, Progress, Row, Spin, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import React, { useCallback, useEffect, useMemo, useState } from 'react';

/** 0~1 小数 → 百分比文本 */
function formatPercent(v?: number): string {
  if (v == null || Number.isNaN(v)) return '-';
  return `${Math.round(v * 100)}%`;
}

/** 0~1 小数 → 0~100 整数（给 Progress 用） */
function toPercent(v?: number): number {
  if (v == null || Number.isNaN(v)) return 0;
  return Math.min(Math.max(Math.round(v * 100), 0), 100);
}

/** 字节 → 自适应可读大小 */
function formatBytes(v?: number): string {
  if (v == null || v <= 0 || Number.isNaN(v)) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  let idx = 0;
  let val = v;
  while (val >= 1024 && idx < units.length - 1) {
    val /= 1024;
    idx += 1;
  }
  // >=10 或最小单位取 0 位小数，否则 1 位
  const digits = val >= 10 || idx === 0 ? 0 : 1;
  return `${val.toFixed(digits)} ${units[idx]}`;
}

/** 毫秒 → 运行时长（天/时/分） */
function formatUptime(v?: number): string {
  if (v == null || v <= 0 || Number.isNaN(v)) return '-';
  const totalSec = Math.floor(v / 1000);
  const days = Math.floor(totalSec / 86400);
  const hours = Math.floor((totalSec % 86400) / 3600);
  const minutes = Math.floor((totalSec % 3600) / 60);
  if (days > 0) return `${days}天 ${hours}小时 ${minutes}分钟`;
  if (hours > 0) return `${hours}小时 ${minutes}分钟`;
  return `${minutes}分钟`;
}

/** 已用/总量 → 百分比（处理 max=0 除零） */
function usagePercent(used?: number, max?: number): number {
  if (!max || max <= 0) return 0;
  return Math.min(Math.max(Math.round(((used ?? 0) / max) * 100), 0), 100);
}

const ServerMonitor: React.FC = () => {
  const [data, setData] = useState<API.ServerMonitorVO>({});
  const [loading, setLoading] = useState(false);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const res: any = await getServer();
      setData((res as API.ServerMonitorVO) || {});
    } catch {
      // 业务失败已在拦截器统一提示，这里静默兜底避免页面崩溃
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const heapPercent = useMemo(
    () => usagePercent(data.heapUsed, data.heapMax),
    [data.heapUsed, data.heapMax],
  );
  const nonHeapPercent = useMemo(
    () => usagePercent(data.nonHeapUsed, data.nonHeapMax),
    [data.nonHeapUsed, data.nonHeapMax],
  );
  const diskPercent = useMemo(() => {
    if (!data.diskTotal || data.diskTotal <= 0) return 0;
    const used = data.diskTotal - (data.diskUsable ?? 0);
    return Math.min(Math.max(Math.round((used / data.diskTotal) * 100), 0), 100);
  }, [data.diskTotal, data.diskUsable]);

  const healthColumns: ColumnsType<API.HealthComponentVO> = [
    { title: '组件', dataIndex: 'name', width: 180 },
    {
      title: '状态',
      dataIndex: 'status',
      width: 120,
      align: 'center',
      render: (status) => (
        <Tag color={status === 'UP' ? 'success' : 'error'}>{(status as string) || '-'}</Tag>
      ),
    },
    { title: '详情', dataIndex: 'details' },
  ];

  const isUp = data.status === 'UP';

  return (
    <PageContainer>
      <Spin spinning={loading}>
        <Row gutter={16}>
          <Col xs={24} sm={12} lg={6}>
            <ProCard>
              <div style={{ marginBottom: 8, color: '#999', fontSize: 13 }}>应用状态</div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <Tag color={isUp ? 'success' : 'error'} style={{ fontSize: 14, padding: '2px 12px' }}>
                  {data.status || '-'}
                </Tag>
                <span style={{ fontWeight: 600 }}>{data.appName || 'AIPerm'}</span>
              </div>
            </ProCard>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <StatisticCard statistic={{ title: '运行时长', value: formatUptime(data.uptime) }} />
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <ProCard title="系统 CPU">
              <div style={{ marginBottom: 4 }}>{formatPercent(data.systemCpuUsage)}</div>
              <Progress percent={toPercent(data.systemCpuUsage)} size="small" />
            </ProCard>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <ProCard title="进程 CPU">
              <div style={{ marginBottom: 4 }}>{formatPercent(data.processCpuUsage)}</div>
              <Progress percent={toPercent(data.processCpuUsage)} size="small" status="success" />
            </ProCard>
          </Col>
        </Row>

        <Row gutter={16} style={{ marginTop: 16 }}>
          <Col xs={24} lg={14}>
            <ProCard
              title="资源使用"
              extra={
                <Button onClick={loadData} loading={loading}>
                  刷新
                </Button>
              }
            >
              <div style={{ marginBottom: 16 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                  <span>堆内存</span>
                  <span style={{ color: '#999' }}>
                    {formatBytes(data.heapUsed)} / {formatBytes(data.heapMax)}
                  </span>
                </div>
                <Progress percent={heapPercent} size="small" />
              </div>
              <div style={{ marginBottom: 16 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                  <span>非堆内存</span>
                  <span style={{ color: '#999' }}>
                    {formatBytes(data.nonHeapUsed)} / {formatBytes(data.nonHeapMax)}
                  </span>
                </div>
                <Progress percent={nonHeapPercent} size="small" status="success" />
              </div>
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                  <span>磁盘使用</span>
                  <span style={{ color: '#999' }}>
                    {formatBytes((data.diskTotal ?? 0) - (data.diskUsable ?? 0))} /{' '}
                    {formatBytes(data.diskTotal)}
                  </span>
                </div>
                <Progress percent={diskPercent} size="small" status="active" strokeColor="#faad14" />
              </div>
            </ProCard>
          </Col>
          <Col xs={24} lg={10}>
            <ProCard title="运行环境">
              <Descriptions column={1} size="small" bordered>
                <Descriptions.Item label="环境">
                  {data.activeProfiles?.length ? data.activeProfiles.join(', ') : 'default'}
                </Descriptions.Item>
                <Descriptions.Item label="Java">{data.javaVersion || '-'}</Descriptions.Item>
                <Descriptions.Item label="操作系统">{data.osName || '-'}</Descriptions.Item>
                <Descriptions.Item label="CPU 核数">{data.processors ?? '-'}</Descriptions.Item>
                <Descriptions.Item label="线程">
                  活动 {data.liveThreads ?? '-'} / 守护 {data.daemonThreads ?? '-'} / 峰值{' '}
                  {data.peakThreads ?? '-'}
                </Descriptions.Item>
              </Descriptions>
            </ProCard>
          </Col>
        </Row>

        <ProCard title="健康检查" style={{ marginTop: 16 }}>
          <Table<API.HealthComponentVO>
            rowKey="name"
            size="small"
            columns={healthColumns}
            dataSource={data.healthComponents || []}
            pagination={false}
            locale={{ emptyText: '暂无健康检查数据' }}
          />
        </ProCard>
      </Spin>
    </PageContainer>
  );
};

export default ServerMonitor;
