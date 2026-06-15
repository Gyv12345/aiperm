/**
 * 岗位管理
 *
 * - 分页查询 /system/post（page2，参数 { dto: PostDTO }，返回 PageResultSysPost）。
 * - 新增/编辑：ModalForm。
 */
import { AddButton } from '@/components/AccessButton';
import {
  create2 as createPost,
  delete2 as deletePost,
  page2 as pagePost,
  update2 as updatePost,
} from '@/services/aiperm/post';
import {
  ModalForm,
  ProFormDigit,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-components';
import { Popconfirm, Tag, message } from 'antd';
import React, { useRef, useState } from 'react';

const PostList: React.FC = () => {
  const actionRef = useRef<any>();
  const [modalOpen, setModalOpen] = useState(false);
  const [current, setCurrent] = useState<API.SysPost | undefined>();

  const columns: ProColumns<API.SysPost>[] = [
    { title: 'ID', dataIndex: 'id', width: 70, hideInSearch: true },
    { title: '岗位名称', dataIndex: 'postName' },
    { title: '岗位编码', dataIndex: 'postCode', hideInSearch: true },
    { title: '排序', dataIndex: 'sort', hideInSearch: true },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: {
        0: { text: '正常', status: 'Success' },
        1: { text: '停用', status: 'Error' },
      },
      render: (_, r) =>
        r.status === 0 ? <Tag color="success">正常</Tag> : <Tag color="error">停用</Tag>,
    },
    { title: '备注', dataIndex: 'remark', hideInSearch: true, ellipsis: true },
    {
      title: '操作',
      valueType: 'option',
      width: 160,
      render: (_, record) => [
        <a
          key="edit"
          onClick={() => {
            setCurrent(record);
            setModalOpen(true);
          }}
        >
          编辑
        </a>,
        <Popconfirm
          key="del"
          title="确认删除该岗位？"
          onConfirm={async () => {
            await deletePost({ id: record.id! });
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
      <ProTable<API.SysPost>
        rowKey="id"
        actionRef={actionRef}
        columns={columns}
        search={{ labelWidth: 'auto' }}
        request={async (params) => {
          const { current, pageSize, ...rest } = params;
          try {
            const res = await pagePost({
              dto: { page: current, pageSize, ...rest } as API.PostDTO,
            } as any);
            const data: API.PageResultSysPost = (res as any) || {};
            return {
              data: data.list || [],
              total: data.total || 0,
              success: true,
            };
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
            新增岗位
          </AddButton>,
        ]}
      />

      <ModalForm
        title={current ? '编辑岗位' : '新增岗位'}
        open={modalOpen}
        onOpenChange={setModalOpen}
        initialValues={current}
        modalProps={{ destroyOnHidden: true }}
        onFinish={async (values) => {
          if (current?.id) {
            await updatePost({ id: current.id }, values as API.PostDTO);
            message.success('更新成功');
          } else {
            await createPost(values as API.PostDTO);
            message.success('创建成功');
          }
          setModalOpen(false);
          actionRef.current?.reload();
          return true;
        }}
      >
        <ProFormText
          name="postName"
          label="岗位名称"
          rules={[{ required: true, message: '请输入岗位名称' }]}
        />
        <ProFormText
          name="postCode"
          label="岗位编码"
          rules={[{ required: true, message: '请输入岗位编码' }]}
        />
        <ProFormDigit name="sort" label="排序" min={0} />
        <ProFormSelect
          name="status"
          label="状态"
          options={[
            { label: '正常', value: 0 },
            { label: '停用', value: 1 },
          ]}
        />
        <ProFormTextArea name="remark" label="备注" />
      </ModalForm>
    </>
  );
};

export default PostList;
