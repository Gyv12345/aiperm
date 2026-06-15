/**
 * 操作日志
 * - 分页查询 /log/oper（page8，参数平铺）。
 * - 操作：删除、清空。
 */
import {
  clean2 as cleanOperLog,
  delete12 as deleteOperLog,
  page8 as pageOperLog,
} from '@/services/aiperm/operLog';
import { ProTable } from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-components';
import { Button, Popconfirm, message } from 'antd';
import React, { useRef } from 'react';

const OperLogList: React.FC = () => {
  const actionRef = useRef<any>();

  const columns: ProColumns<API.SysOperLog>[] = [
    { title: 'ID', dataIndex: 'id', width: 70, hideInSearch: true },
    { title: '模块', dataIndex: 'title' },
    { title: '操作人', dataIndex: 'operUser', hideInSearch: true, width: 100 },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: { 0: { text: '成功' }, 1: { text: '失败', status: 'Error' } },
      render: (_, r) =>
        r.status === 0 ? '成功' : <span style={{ color: '#ff4d4f' }}>失败</span>,
    },
    { title: '请求方式', dataIndex: 'requestMethod', hideInSearch: true, width: 90 },
    { title: 'IP', dataIndex: 'operIp', hideInSearch: true, width: 120 },
    { title: '耗时(ms)', dataIndex: 'costTime', hideInSearch: true, width: 90 },
    { title: '操作时间', dataIndex: 'createTime', valueType: 'dateTime', hideInSearch: true },
    {
      title: '操作',
      valueType: 'option',
      width: 90,
      render: (_, record) => [
        <Popconfirm
          key="del"
          title="确认删除？"
          onConfirm={async () => {
            await deleteOperLog({ id: record.id! });
            message.success('删除成功');
            actionRef.current?.reload();
          }}
        >
          <a style={{ color: '#ff4d4f' }}>删除</a>
        </Popconfirm>,
      ],
    },
  ];

  return (
    <ProTable<API.SysOperLog>
      rowKey="id"
      actionRef={actionRef}
      columns={columns}
      search={{ labelWidth: 'auto' }}
      request={async (params) => {
        const { current, pageSize, ...rest } = params;
        try {
          const res: any = await pageOperLog({ page: current, pageSize, ...rest });
          const data = res || {};
          return { data: data.list || [], total: data.total || 0, success: true };
        } catch {
          return { data: [], total: 0, success: false };
        }
      }}
      toolBarRender={() => [
        <Popconfirm
          key="clean"
          title="确认清空所有操作日志？此操作不可恢复"
          onConfirm={async () => {
            await cleanOperLog();
            message.success('已清空');
            actionRef.current?.reload();
          }}
        >
          <Button danger>清空</Button>
        </Popconfirm>,
      ]}
    />
  );
};

export default OperLogList;
