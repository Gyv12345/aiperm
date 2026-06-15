/**
 * 在线用户
 * - 分页查询 /monitor/online（page5，参数平铺）。
 * - 操作：强退（forceLogout）、批量强退（forceLogoutBatch）。
 */
import {
  forceLogout,
  forceLogoutBatch,
  page5 as pageOnline,
} from '@/services/aiperm/monitorOnline';
import { ProTable } from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-components';
import { Popconfirm, Tag, message } from 'antd';
import React, { useRef, useState } from 'react';

const OnlineList: React.FC = () => {
  const actionRef = useRef<any>();
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);

  const columns: ProColumns<API.OnlineUserVO>[] = [
    { title: 'ID', dataIndex: 'id', width: 70, hideInSearch: true },
    { title: '用户名', dataIndex: 'username' },
    { title: '昵称', dataIndex: 'nickname', hideInSearch: true },
    { title: '部门', dataIndex: 'deptName', hideInSearch: true },
    { title: '角色', dataIndex: 'roleNames', hideInSearch: true, ellipsis: true },
    { title: 'IP', dataIndex: 'ip', hideInSearch: true },
    { title: '登录时间', dataIndex: 'loginTime', valueType: 'dateTime', hideInSearch: true },
    {
      title: '操作',
      valueType: 'option',
      width: 100,
      render: (_, record) => [
        <Popconfirm
          key="kick"
          title={`确认强退用户 ${record.username}？`}
          onConfirm={async () => {
            await forceLogout({ id: record.id! } as any);
            message.success('已强退');
            actionRef.current?.reload();
          }}
        >
          <a style={{ color: '#ff4d4f' }}>强退</a>
        </Popconfirm>,
      ],
    },
  ];

  return (
    <ProTable<API.OnlineUserVO>
      rowKey="id"
      actionRef={actionRef}
      columns={columns}
      search={{ labelWidth: 'auto' }}
      rowSelection={{
        selectedRowKeys,
        onChange: setSelectedRowKeys,
      }}
      tableAlertOptionRender={() => (
        <a
          onClick={async () => {
            await forceLogoutBatch(selectedRowKeys as number[]);
            message.success('批量强退成功');
            setSelectedRowKeys([]);
            actionRef.current?.reload();
          }}
        >
          批量强退
        </a>
      )}
      request={async (params) => {
        const { current, pageSize, ...rest } = params;
        try {
          const res: any = await pageOnline({ page: current, pageSize, ...rest });
          const data = res || {};
          return { data: data.list || [], total: data.total || 0, success: true };
        } catch {
          // 后端权限码 monitor:online:list 可能未配置，降级为空列表
          return { data: [], total: 0, success: true };
        }
      }}
    />
  );
};

export default OnlineList;
