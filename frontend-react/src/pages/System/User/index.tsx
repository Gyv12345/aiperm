/**
 * 用户管理
 *
 * - ProTable：分页查询 /system/user（page/pageSize + 查询字段平铺）。
 * - 新增/编辑：ModalForm（ProForm）。
 * - 操作：删除、重置密码、切换状态。
 *
 * 注意：生成的分页函数 page() 期望参数平铺（后端 @ModelAttribute 绑定）。
 * 返回 PageResult<UserVO>（list/total/pageNum），与 ProTable 默认字段不同，
 * 在 request 适配器里做映射。
 */
import { AddButton } from '@/components/AccessButton';
import {
  changeStatus,
  create,
  deleteUsingDelete,
  page,
  resetPassword,
  update,
} from '@/services/aiperm/user';
import {
  ModalForm,
  ProFormSelect,
  ProFormText,
  ProTable,
} from '@ant-design/pro-components';
import { Popconfirm, Tag, message } from 'antd';
import type { ProColumns } from '@ant-design/pro-components';
import React, { useRef, useState } from 'react';

const UserList: React.FC = () => {
  const actionRef = useRef<any>();
  const [modalOpen, setModalOpen] = useState(false);
  const [current, setCurrent] = useState<API.UserVO | undefined>(undefined);
  const [pwdOpen, setPwdOpen] = useState(false);
  const [pwdTarget, setPwdTarget] = useState<API.UserVO | undefined>();

  const columns: ProColumns<API.UserVO>[] = [
    { title: 'ID', dataIndex: 'id', width: 70, hideInSearch: true },
    { title: '用户名', dataIndex: 'username' },
    { title: '昵称', dataIndex: 'nickname', hideInSearch: true },
    { title: '真实姓名', dataIndex: 'realName', hideInSearch: true },
    { title: '邮箱', dataIndex: 'email', hideInSearch: true },
    { title: '手机号', dataIndex: 'phone' },
    {
      title: '部门',
      dataIndex: 'deptName',
      hideInSearch: true,
      renderText: (v) => v || '-',
    },
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
    { title: '创建时间', dataIndex: 'createTime', valueType: 'dateTime', hideInSearch: true },
    {
      title: '操作',
      valueType: 'option',
      width: 220,
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
        <a
          key="pwd"
          onClick={() => {
            setPwdTarget(record);
            setPwdOpen(true);
          }}
        >
          重置密码
        </a>,
        <Popconfirm
          key="del"
          title="确认删除该用户？"
          onConfirm={async () => {
            await deleteUsingDelete({ id: record.id! });
            message.success('删除成功');
            actionRef.current?.reload();
          }}
        >
          <a style={{ color: '#ff4d4f' }}>删除</a>
        </Popconfirm>,
        record.status === 0 ? (
          <a
            key="disable"
            style={{ color: '#faad14' }}
            onClick={async () => {
              await changeStatus({ id: record.id!, status: 1 });
              message.success('已停用');
              actionRef.current?.reload();
            }}
          >
            停用
          </a>
        ) : (
          <a
            key="enable"
            onClick={async () => {
              await changeStatus({ id: record.id!, status: 0 });
              message.success('已启用');
              actionRef.current?.reload();
            }}
          >
            启用
          </a>
        ),
      ],
    },
  ];

  return (
    <>
      <ProTable<API.UserVO>
        rowKey="id"
        actionRef={actionRef}
        columns={columns}
        search={{ labelWidth: 'auto' }}
        request={async (params) => {
          // ProTable 传 current/pageSize，后端 UserDTO 用 page/pageSize（@ModelAttribute 平铺）
          const { current, pageSize, ...rest } = params;
          try {
            // 生成的 page() 期望 { dto: UserDTO }，内部展开 dto 字段为 query
            const res = await page({
              dto: { page: current, pageSize, ...rest } as API.UserDTO,
            } as any);
            // PageResult<UserVO>: { list, total, pageNum, pageSize }
            const data: API.PageResultUserVO = (res as any) || {};
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
            新增用户
          </AddButton>,
        ]}
      />

      <ModalForm
        title={current ? '编辑用户' : '新增用户'}
        open={modalOpen}
        onOpenChange={setModalOpen}
        initialValues={current}
        modalProps={{ destroyOnClose: true }}
        onFinish={async (values) => {
          if (current?.id) {
            await update({ id: current.id }, values as API.UserDTO);
            message.success('更新成功');
          } else {
            await create(values as API.UserDTO);
            message.success('创建成功');
          }
          setModalOpen(false);
          actionRef.current?.reload();
          return true;
        }}
      >
        <ProFormText
          name="username"
          label="用户名"
          rules={[{ required: true, message: '请输入用户名' }]}
          disabled={!!current?.id}
        />
        {!current?.id && (
          <ProFormText.Password
            name="password"
            label="密码"
            rules={[{ required: true, message: '请输入密码' }]}
          />
        )}
        <ProFormText name="nickname" label="昵称" />
        <ProFormText name="realName" label="真实姓名" />
        <ProFormText name="email" label="邮箱" rules={[{ type: 'email', message: '邮箱格式不正确' }]} />
        <ProFormText name="phone" label="手机号" />
        <ProFormSelect
          name="gender"
          label="性别"
          options={[
            { label: '未知', value: 0 },
            { label: '男', value: 1 },
            { label: '女', value: 2 },
          ]}
        />
        <ProFormSelect
          name="status"
          label="状态"
          options={[
            { label: '正常', value: 0 },
            { label: '停用', value: 1 },
          ]}
        />
      </ModalForm>

      <ModalForm
        title={`重置密码 - ${pwdTarget?.username || ''}`}
        open={pwdOpen}
        onOpenChange={setPwdOpen}
        modalProps={{ destroyOnClose: true }}
        onFinish={async (values) => {
          await resetPassword(
            { id: pwdTarget?.id! },
            { password: values.password } as API.UserDTO,
          );
          message.success('密码已重置');
          setPwdOpen(false);
          return true;
        }}
      >
        <ProFormText.Password
          name="password"
          label="新密码"
          rules={[{ required: true, message: '请输入新密码' }]}
        />
      </ModalForm>
    </>
  );
};

export default UserList;
