/**
 * 公告通知管理
 *
 * - 分页查询 /enterprise/notice（list4）。
 * - 新增/编辑：ModalForm（type 1=通知 2=公告，status 0=草稿 1=发布）。
 * - 操作：发布（publish）、撤回（withdraw）、删除。
 */
import { AddButton } from '@/components/AccessButton';
import {
  create9 as createNotice,
  delete9 as deleteNotice,
  list4 as pageNotice,
  publish as publishNotice,
  update10 as updateNotice,
  withdraw as withdrawNotice,
} from '@/services/aiperm/notice';
import {
  ModalForm,
  ProFormDigit,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-components';
import { Button, Popconfirm, Tag, message } from 'antd';
import React, { useRef, useState } from 'react';

const NoticeList: React.FC = () => {
  const actionRef = useRef<any>();
  const [modalOpen, setModalOpen] = useState(false);
  const [current, setCurrent] = useState<API.NoticeVO | undefined>();

  const columns: ProColumns<API.NoticeVO>[] = [
    { title: 'ID', dataIndex: 'id', width: 70, hideInSearch: true },
    { title: '标题', dataIndex: 'title' },
    {
      title: '类型',
      dataIndex: 'type',
      valueType: 'select',
      valueEnum: { 1: { text: '通知' }, 2: { text: '公告' } },
      render: (_, r) => (
        <Tag color={r.type === 2 ? 'blue' : 'default'}>{r.type === 2 ? '公告' : '通知'}</Tag>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: {
        0: { text: '草稿', status: 'Default' },
        1: { text: '已发布', status: 'Success' },
      },
      render: (_, r) =>
        r.status === 1 ? <Tag color="success">已发布</Tag> : <Tag>草稿</Tag>,
    },
    { title: '发布时间', dataIndex: 'publishTime', valueType: 'dateTime', hideInSearch: true },
    { title: '创建人', dataIndex: 'createBy', hideInSearch: true, width: 100 },
    {
      title: '操作',
      valueType: 'option',
      width: 240,
      render: (_, record) => [
        <a
          key="edit"
          style={record.status === 1 ? { color: '#d9d9d9', pointerEvents: 'none' } : undefined}
          onClick={() => {
            if (record.status === 1) return;
            setCurrent(record);
            setModalOpen(true);
          }}
        >
          编辑
        </a>,
        record.status === 0 && (
          <Popconfirm
            key="pub"
            title="确认发布？"
            onConfirm={async () => {
              await publishNotice({ id: record.id! } as any);
              message.success('已发布');
              actionRef.current?.reload();
            }}
          >
            <a>发布</a>
          </Popconfirm>
        ),
        record.status === 1 && (
          <Popconfirm
            key="wd"
            title="确认撤回？"
            onConfirm={async () => {
              await withdrawNotice({ id: record.id! } as any);
              message.success('已撤回');
              actionRef.current?.reload();
            }}
          >
            <a>撤回</a>
          </Popconfirm>
        ),
        <Popconfirm
          key="del"
          title="确认删除？"
          onConfirm={async () => {
            await deleteNotice({ id: record.id! } as any);
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
    <>
      <ProTable<API.NoticeVO>
        rowKey="id"
        actionRef={actionRef}
        columns={columns}
        search={{ labelWidth: 'auto' }}
        request={async (params) => {
          const { current, pageSize, ...rest } = params;
          try {
            const res = await pageNotice({
              dto: { page: current, pageSize, ...rest } as API.NoticeDTO,
            } as any);
            const data: API.PageResultNoticeVO = (res as any) || {};
            return { data: data.list || [], total: data.total || 0, success: true };
          } catch {
            return { data: [], total: 0, success: false };
          }
        }}
        toolBarRender={() => [
          <AddButton
            key="add"
            onClick={() => {
              setCurrent(undefined);
              setModalOpen(true);
            }}
          >
            新增公告
          </AddButton>,
        ]}
      />

      <ModalForm
        title={current?.id ? '编辑公告' : '新增公告'}
        open={modalOpen}
        onOpenChange={setModalOpen}
        initialValues={current}
        modalProps={{ destroyOnHidden: true }}
        onFinish={async (values) => {
          try {
            if (current?.id) {
              await updateNotice({ id: current.id } as any, values as API.NoticeDTO);
              message.success('更新成功');
            } else {
              await createNotice(values as API.NoticeDTO);
              message.success('创建成功');
            }
            setModalOpen(false);
            actionRef.current?.reload();
            return true;
          } catch {
            return false;
          }
        }}
      >
        <ProFormText
          name="title"
          label="标题"
          rules={[{ required: true, message: '请输入标题' }]}
        />
        <ProFormSelect
          name="type"
          label="类型"
          options={[
            { label: '通知', value: 1 },
            { label: '公告', value: 2 },
          ]}
        />
        <ProFormTextArea
          name="content"
          label="内容"
          fieldProps={{ autoSize: { minRows: 4 } }}
        />
      </ModalForm>
    </>
  );
};

export default NoticeList;
