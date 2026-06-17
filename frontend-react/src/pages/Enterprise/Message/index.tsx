/**
 * 消息中心（收件箱）
 *
 * - 分页查询 /enterprise/message（list5，boxType=1 收件箱）。
 * - 操作：标记已读（markAsRead）、全部已读（markAllAsRead）、批量已读、删除。
 * - 发送消息：ModalForm（send），接收人来自 /enterprise/message/receivers。
 */
import { AddButton } from '@/components/AccessButton';
import {
  delete13 as deleteMsg,
  list5 as pageMessage,
  markAllAsRead,
  markAsRead,
  markAsReadByIds,
  receivers as listReceivers,
  send as sendMessage,
} from '@/services/aiperm/message';
import {
  ModalForm,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-components';
import { Button, Popconfirm, Tag, message } from 'antd';
import React, { useRef, useState } from 'react';

const MessageList: React.FC = () => {
  const actionRef = useRef<any>();
  const [sendOpen, setSendOpen] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);

  const columns: ProColumns<API.MessageVO>[] = [
    { title: 'ID', dataIndex: 'id', width: 70, hideInSearch: true },
    { title: '发送人', dataIndex: 'senderName', hideInSearch: true, width: 100 },
    { title: '标题', dataIndex: 'title' },
    { title: '内容', dataIndex: 'content', hideInSearch: true, ellipsis: true },
    {
      title: '是否已读',
      dataIndex: 'isRead',
      valueType: 'select',
      valueEnum: {
        0: { text: '未读', status: 'Error' },
        1: { text: '已读', status: 'Success' },
      },
      render: (_, r) =>
        r.isRead === 1 ? <Tag color="success">已读</Tag> : <Tag color="error">未读</Tag>,
    },
    { title: '阅读时间', dataIndex: 'readTime', valueType: 'dateTime', hideInSearch: true },
    {
      title: '操作',
      valueType: 'option',
      width: 160,
      render: (_, record) => [
        record.isRead === 0 && (
          <a
            key="read"
            onClick={async () => {
              await markAsRead({ id: record.id! });
              message.success('已标记已读');
              actionRef.current?.reload();
            }}
          >
            标记已读
          </a>
        ),
        <Popconfirm
          key="del"
          title="确认删除？"
          onConfirm={async () => {
            try {
              await deleteMsg({ id: record.id! });
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
    <>
      <ProTable<API.MessageVO>
        rowKey="id"
        actionRef={actionRef}
        columns={columns}
        search={{ labelWidth: 'auto' }}
        rowSelection={{
          selectedRowKeys,
          onChange: setSelectedRowKeys,
        }}
        tableAlertOptionRender={() => {
          return (
            <a
              onClick={async () => {
                await markAsReadByIds({ ids: selectedRowKeys as number[] });
                message.success('批量已读成功');
                setSelectedRowKeys([]);
                actionRef.current?.reload();
              }}
            >
              批量已读
            </a>
          );
        }}
        request={async (params) => {
          const { current, pageSize, ...rest } = params;
          try {
            const res = await pageMessage({
              dto: { page: current, pageSize, boxType: 1, ...rest } as API.MessageDTO,
            } as any);
            const data: API.PageResultMessageVO = (res as any) || {};
            return { data: data.list || [], total: data.total || 0, success: true };
          } catch {
            return { data: [], total: 0, success: false };
          }
        }}
        toolBarRender={() => [
          <Button
            key="read-all"
            onClick={async () => {
              await markAllAsRead();
              message.success('全部已读');
              actionRef.current?.reload();
            }}
          >
            全部已读
          </Button>,
          <AddButton
            key="send"
            onClick={() => setSendOpen(true)}
          >
            发送消息
          </AddButton>,
        ]}
      />

      <ModalForm
        title="发送消息"
        open={sendOpen}
        onOpenChange={setSendOpen}
        width={640}
        grid
        rowProps={{ gutter: 16 }}
        modalProps={{ destroyOnHidden: true }}
        onFinish={async (values) => {
          try {
            await sendMessage(values as API.MessageDTO);
            message.success('发送成功');
            setSendOpen(false);
            return true;
          } catch {
            return false;
          }
        }}
        request={async () => {
          const res: any = await listReceivers();
          const list: API.MessageReceiverVO[] = res?.data ?? res ?? [];
          return {
            receiverOptions: list.map((r) => ({
              label: r.nickname || r.username || `用户${r.id}`,
              value: r.id,
            })),
          };
        }}
      >
        <ProFormSelect
          name="receiverId"
          label="接收人"
          colProps={{ span: 12 }}
          request={async () => {
            const res: any = await listReceivers();
            const list: API.MessageReceiverVO[] = res?.data ?? res ?? [];
            return list.map((r) => ({
              label: r.nickname || r.username || `用户${r.id}`,
              value: r.id,
            }));
          }}
          rules={[{ required: true, message: '请选择接收人' }]}
        />
        <ProFormText
          name="title"
          label="标题"
          colProps={{ span: 12 }}
          rules={[{ required: true, message: '请输入标题' }]}
        />
        <ProFormTextArea
          name="content"
          label="内容"
          colProps={{ span: 24 }}
          fieldProps={{ autoSize: { minRows: 4 } }}
          rules={[{ required: true, message: '请输入内容' }]}
        />
      </ModalForm>
    </>
  );
};

export default MessageList;
