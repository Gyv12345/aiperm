/**
 * 缓存监控
 *
 * - 仪表盘型页面，GET /monitor/cache 返回 CacheMonitorVO。
 * - 展示 Redis 内存、连接数、Key 数、命中率，以及缓存分区条目表。
 *
 * 纯只读，无表单/无 CRUD。
 */
import { overview1 as getCache } from '@/services/aiperm/monitorCache';
import { PageContainer, ProCard, StatisticCard } from '@ant-design/pro-components';
import { Button, Col, Progress, Row, Spin, Table, Tooltip } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import React, { useCallback, useEffect, useState } from 'react';

/** 0~1 小数 → 百分比文本（0 位小数） */
function formatPercent(v?: number): string {
  if (v == null || Number.isNaN(v)) return '-';
  return `${Math.round(v * 100)}%`;
}

/** 0~1 小数 → 0~100 整数（给 Progress 用） */
function toPercent(v?: number): number {
  if (v == null || Number.isNaN(v)) return 0;
  return Math.min(Math.max(Math.round(v * 100), 0), 100);
}

/** 秒 → 可读 TTL */
function formatTtl(v?: number): string {
  if (v == null) return '-';
  if (v <= 0) return '永久/未知';
  if (v < 60) return `${v}s`;
  if (v < 3600) return `${Math.floor(v / 60)}m`;
  return `${Math.floor(v / 3600)}h`;
}

const CacheMonitor: React.FC = () => {
  const [data, setData] = useState<API.CacheMonitorVO>({});
  const [loading, setLoading] = useState(false);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const res: any = await getCache();
      setData((res as API.CacheMonitorVO) || {});
    } catch {
      // 业务失败已在拦截器统一提示，这里静默兜底避免页面崩溃
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const entryColumns: ColumnsType<API.CacheEntryVO> = [
    { title: '缓存名称', dataIndex: 'cacheName' },
    {
      title: 'Key 前缀',
      dataIndex: 'keyPrefix',
      ellipsis: { showTitle: false },
      render: (text) => (
        <Tooltip title={text}>
          <span>{text}</span>
        </Tooltip>
      ),
    },
    { title: '估算数量', dataIndex: 'estimatedSize', align: 'center', width: 120 },
    {
      title: '样例 TTL',
      dataIndex: 'sampleTtl',
      align: 'center',
      width: 120,
      render: (v) => formatTtl(v as number),
    },
  ];

  const hitPercent = toPercent(data.hitRate);

  return (
    <PageContainer>
      <Spin spinning={loading}>
        <Row gutter={16}>
          <Col xs={24} sm={12} lg={6}>
            <StatisticCard
              statistic={{ title: 'Redis 已用内存', value: data.usedMemoryHuman ?? '-', loading }}
            />
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <StatisticCard
              statistic={{ title: '客户端连接', value: data.connectedClients ?? '-', loading }}
            />
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <StatisticCard
              statistic={{ title: '总 Key 数', value: data.totalKeys ?? '-', loading }}
            />
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <StatisticCard
              statistic={{ title: '命中率', value: formatPercent(data.hitRate), loading }}
              chart={
                <div style={{ marginTop: -8 }}>
                  <Progress percent={hitPercent} size="small" status="success" />
                </div>
              }
            />
          </Col>
        </Row>

        <ProCard
          title="读写命中"
          style={{ marginTop: 16 }}
          extra={
            <Button onClick={loadData} loading={loading}>
              刷新
            </Button>
          }
        >
          <Row gutter={16}>
            <Col xs={24} sm={12}>
              <div
                style={{
                  background: '#f6ffed',
                  border: '1px solid #b7eb8f',
                  borderRadius: 8,
                  padding: 16,
                  color: '#389e0d',
                }}
              >
                <div style={{ fontSize: 13 }}>命中次数</div>
                <div style={{ fontSize: 28, fontWeight: 600, color: '#135200' }}>
                  {data.hits ?? 0}
                </div>
              </div>
            </Col>
            <Col xs={24} sm={12}>
              <div
                style={{
                  background: '#fff7e6',
                  border: '1px solid #ffd591',
                  borderRadius: 8,
                  padding: 16,
                  color: '#d46b08',
                }}
              >
                <div style={{ fontSize: 13 }}>未命中次数</div>
                <div style={{ fontSize: 28, fontWeight: 600, color: '#874d00' }}>
                  {data.misses ?? 0}
                </div>
              </div>
            </Col>
          </Row>
        </ProCard>

        <ProCard title="缓存分区" style={{ marginTop: 16 }}>
          <Table<API.CacheEntryVO>
            rowKey="cacheName"
            size="small"
            columns={entryColumns}
            dataSource={data.entries || []}
            pagination={false}
            locale={{ emptyText: '暂无缓存条目' }}
          />
        </ProCard>
      </Spin>
    </PageContainer>
  );
};

export default CacheMonitor;
