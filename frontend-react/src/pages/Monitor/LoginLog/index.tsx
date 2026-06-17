/**
 * 登录日志
 * - 分页查询 /monitor/login-log（page6，参数平铺，返回 PageResultLoginLogVO）。
 * - 操作：删除、清空。
 */
import {
  clean as cleanLoginLog,
  delete15 as deleteLoginLog,
  page6 as pageLoginLog,
} from '@/services/aiperm/loginLog';
import { ProTable } from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-components';
import { Button, Popconfirm, message } from 'antd';
import React, { useRef } from 'react';

const LoginLogList: React.FC = () => {
  const actionRef = useRef<any>();

  const columns: ProColumns<API.LoginLogVO>[] = [
    { title: 'ID', dataIndex: 'id', width: 70, hideInSearch: true },
    { title: '用户名', dataIndex: 'username' },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: { 0: { text: '成功' }, 1: { text: '失败', status: 'Error' } },
      render: (_, r) =>
        r.status === 0 ? '成功' : <span style={{ color: '#ff4d4f' }}>失败</span>,
    },
    { title: 'IP', dataIndex: 'ip', hideInSearch: true },
    { title: '登录地点', dataIndex: 'location', hideInSearch: true },
    { title: '浏览器', dataIndex: 'browser', hideInSearch: true },
    { title: '操作系统', dataIndex: 'os', hideInSearch: true },
    { title: '消息', dataIndex: 'msg', hideInSearch: true, ellipsis: true },
    { title: '登录时间', dataIndex: 'loginTime', valueType: 'dateTime', hideInSearch: true },
    {
      title: '操作',
      valueType: 'option',
      width: 90,
      render: (_, record) => [
        <Popconfirm
          key="del"
          title="确认删除？"
          onConfirm={async () => {
            try {
              await deleteLoginLog({ id: record.id! });
              message.success('删除成功');
              actionRef.current?.reload();
            } catch {
              // 业务失败已在拦截器统一提示，吞掉避免 Unhandled Rejection
            }
          }}
        >
          <a style={{ color: '#ff4d4f' }}>删除</a>
        </Popconfirm>,
      ],
    },
  ];

  return (
    <ProTable<API.LoginLogVO>
      rowKey="id"
      actionRef={actionRef}
      columns={columns}
      search={{ labelWidth: 'auto' }}
      request={async (params) => {
        const { current, pageSize, ...rest } = params;
        try {
          const res: any = await pageLoginLog({ page: current, pageSize, ...rest });
          const data = res || {};
          return { data: data.list || [], total: data.total || 0, success: true };
        } catch {
          return { data: [], total: 0, success: false };
        }
      }}
      toolBarRender={() => [
        <Popconfirm
          key="clean"
          title="确认清空所有登录日志？此操作不可恢复"
          onConfirm={async () => {
            try {
              await cleanLoginLog();
              message.success('已清空');
              actionRef.current?.reload();
            } catch {
              // 业务失败已在拦截器统一提示，吞掉避免 Unhandled Rejection
            }
          }}
        >
          <Button danger>清空</Button>
        </Popconfirm>,
      ]}
    />
  );
};

export default LoginLogList;
