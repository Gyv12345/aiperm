/**
 * 用户管理（样板页）
 *
 * 相比默认 ProTable 套模板的升级点：
 * - 搜索区：紧凑布局 + 状态筛选。
 * - 工具栏：新增 / 批量删除（勾选后可见）/ 刷新 / 列设置。
 * - 表格：头像 + 用户名、角色多 Tag、状态可点 Switch（带 loading 与二次确认）。
 * - 操作列：主操作「编辑」+「更多」下拉（重置密码、删除），绑定权限码。
 * - 新增/编辑表单：分组（账号 / 基本 / 组织角色 / 备注），部门树选、角色/岗位多选，
 *   编辑时通过 getById 回填关联字段；两列栅格布局。
 *
 * 关联数据来源（均已被 responseInterceptor 解包）：
 * - 部门：dept.tree1 → SysDept 树（含 children）
 * - 角色：role.list8 → SysRole[]
 * - 岗位：post.list9 → SysPost[]
 */
import { AddButton } from '@/components/AccessButton';
import { tree1 as deptTree } from '@/services/aiperm/dept';
import { list9 as postList } from '@/services/aiperm/post';
import { list8 as roleList } from '@/services/aiperm/role';
import {
  changeStatus,
  create,
  deleteBatch,
  deleteUsingDelete,
  getById,
  page,
  resetPassword,
  update,
} from '@/services/aiperm/user';
import {
  ModalForm,
  ProForm,
  ProFormRadio,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  ProFormTreeSelect,
  ProTable,
} from '@ant-design/pro-components';
import { Access, useAccess } from '@umijs/max';
import {
  Avatar,
  Button,
  Dropdown,
  Popconfirm,
  Space,
  Switch,
  Tag,
  Tooltip,
  message,
} from 'antd';
import {
  DeleteOutlined,
  EditOutlined,
  EllipsisOutlined,
  KeyOutlined,
  PlusOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import type { MenuProps } from 'antd';
import type { ProColumns } from '@ant-design/pro-components';
import React, { useRef, useState } from 'react';

/** 状态：1=正常 0=停用（与后端 UserVO.status 语义一致） */
const STATUS = {
  NORMAL: 1,
  DISABLED: 0,
} as const;

/** 性别映射 */
const GENDER = {
  UNKNOWN: 0,
  MALE: 1,
  FEMALE: 2,
} as const;

/** 角色 Tag 配色轮转，便于一眼区分 */
const ROLE_COLORS = [
  'blue',
  'green',
  'gold',
  'purple',
  'cyan',
  'magenta',
  'geekblue',
  'volcano',
];

const UserList: React.FC = () => {
  const actionRef = useRef<any>();
  const formRef = useRef<any>();
  const access = useAccess();

  const [modalOpen, setModalOpen] = useState(false);
  const [current, setCurrent] = useState<API.UserVO | undefined>(undefined);
  const [editLoading, setEditLoading] = useState(false);

  const [pwdOpen, setPwdOpen] = useState(false);
  const [pwdTarget, setPwdTarget] = useState<API.UserVO | undefined>();

  // 批量选中
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [batchDeleting, setBatchDeleting] = useState(false);

  // 状态 Switch 的逐行 loading（用 Set 记录正在切换的 id）
  const [statusLoading, setStatusLoading] = useState<Set<number>>(new Set());

  // 关联选项（弹窗打开时懒加载）
  const [deptOptions, setDeptOptions] = useState<any[]>([]);
  const [roleOptions, setRoleOptions] = useState<any[]>([]);
  const [postOptions, setPostOptions] = useState<any[]>([]);

  const canCreate = access.canPermission('system:user:create');
  const canUpdate = access.canPermission('system:user:update');
  const canDelete = access.canPermission('system:user:delete');
  const canResetPwd = access.canPermission('system:user:reset-password');

  /** 懒加载部门/角色/岗位选项 */
  const loadOptions = async () => {
    try {
      const [dept, role, post] = await Promise.all([
        deptTree(),
        roleList(),
        postList(),
      ]);
      // 部门是树（RListSysMenu 同款：可能是 { data: [...] } 或直接数组）
      const deptData: any[] = (dept as any)?.data ?? (dept as any) ?? [];
      setDeptOptions(deptData);
      const roleData: any[] = (role as any)?.data ?? (role as any) ?? [];
      setRoleOptions(roleData.map((r) => ({ label: r.roleName, value: r.id })));
      const postData: any[] = (post as any)?.data ?? (post as any) ?? [];
      setPostOptions(postData.map((p) => ({ label: p.postName, value: p.id })));
    } catch {
      // 选项加载失败不阻塞弹窗，只是下拉为空
    }
  };

  /** 打开新增弹窗 */
  const openCreate = () => {
    setCurrent(undefined);
    setModalOpen(true);
    loadOptions();
  };

  /** 打开编辑弹窗：先拉详情再回填（列表行可能缺 roleIds/postIds/deptId） */
  const openEdit = async (record: API.UserVO) => {
    setEditLoading(true);
    setCurrent(record);
    setModalOpen(true);
    await loadOptions();
    try {
      const detail = await getById({ id: record.id! });
      setCurrent(detail);
      // 注意：ModalForm 的 initialValues 仅在挂载时读取一次，弹窗已用列表行先挂载，
      // 详情异步回来后必须用 setFieldsValue 主动回填（含 roleIds/postIds/deptId）。
      formRef.current?.setFieldsValue(detail);
    } catch {
      // 详情拉取失败则用列表行兜底回填
      formRef.current?.setFieldsValue(record);
    } finally {
      setEditLoading(false);
    }
  };

  /** 切换状态 */
  const toggleStatus = async (record: API.UserVO, next: number) => {
    setStatusLoading((s) => new Set(s).add(record.id!));
    try {
      await changeStatus({ id: record.id!, status: next });
      message.success(next === STATUS.NORMAL ? '已启用' : '已停用');
      actionRef.current?.reload();
    } catch {
      // 拦截器已统一提示
    } finally {
      setStatusLoading((s) => {
        const ns = new Set(s);
        ns.delete(record.id!);
        return ns;
      });
    }
  };

  /** 批量删除 */
  const onBatchDelete = async () => {
    setBatchDeleting(true);
    try {
      await deleteBatch(selectedRowKeys as number[]);
      message.success(`已删除 ${selectedRowKeys.length} 项`);
      setSelectedRowKeys([]);
      actionRef.current?.reload();
    } catch {
      // 拦截器已统一提示
    } finally {
      setBatchDeleting(false);
    }
  };

  const columns: ProColumns<API.UserVO>[] = [
    {
      title: '用户名',
      dataIndex: 'username',
      width: 180,
      render: (_, record) => (
        <Space>
          <Avatar size="small" src={record.avatar}>
            {(record.nickname || record.username || '?').charAt(0).toUpperCase()}
          </Avatar>
          <div>
            <div style={{ fontWeight: 500 }}>{record.username}</div>
            {record.nickname && (
              <div style={{ fontSize: 12, color: '#999' }}>{record.nickname}</div>
            )}
          </div>
        </Space>
      ),
    },
    {
      title: '真实姓名',
      dataIndex: 'realName',
      width: 100,
      hideInSearch: true,
      renderText: (v) => v || '-',
    },
    {
      title: '部门',
      dataIndex: 'deptName',
      width: 120,
      hideInSearch: true,
      renderText: (v) => v || '-',
    },
    {
      title: '角色',
      dataIndex: 'roleNames',
      width: 180,
      hideInSearch: true,
      render: (_, record) => {
        const names = (record.roleNames || '')
          .split(',')
          .map((s) => s.trim())
          .filter(Boolean);
        if (!names.length) return <span style={{ color: '#ccc' }}>-</span>;
        return (
          <Space size={[0, 4]} wrap>
            {names.map((n, i) => (
              <Tag key={n} color={ROLE_COLORS[i % ROLE_COLORS.length]}>
                {n}
              </Tag>
            ))}
          </Space>
        );
      },
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      width: 130,
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      width: 180,
      hideInSearch: true,
      ellipsis: true,
      renderText: (v) => v || '-',
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 90,
      valueType: 'select',
      valueEnum: {
        1: { text: '正常' },
        0: { text: '停用' },
      },
      render: (_, record) => {
        if (!canUpdate) {
          return record.status === STATUS.NORMAL ? (
            <Tag color="success">正常</Tag>
          ) : (
            <Tag color="error">停用</Tag>
          );
        }
        return (
          <Popconfirm
            title={`确认${record.status === STATUS.NORMAL ? '停用' : '启用'}该用户？`}
            onConfirm={() =>
              toggleStatus(
                record,
                record.status === STATUS.NORMAL
                  ? STATUS.DISABLED
                  : STATUS.NORMAL,
              )
            }
          >
            <Switch
              size="small"
              checked={record.status === STATUS.NORMAL}
              loading={statusLoading.has(record.id!)}
            />
          </Popconfirm>
        );
      },
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      valueType: 'dateTime',
      width: 160,
      hideInSearch: true,
    },
    {
      title: '操作',
      valueType: 'option',
      width: 130,
      fixed: 'right',
      render: (_, record) => {
        // 「更多」下拉项（按权限过滤）
        const moreItems: MenuProps['items'] = [];
        if (canResetPwd) {
          moreItems.push({
            key: 'pwd',
            icon: <KeyOutlined />,
            label: '重置密码',
            onClick: () => {
              setPwdTarget(record);
              setPwdOpen(true);
            },
          });
        }
        if (canDelete) {
          moreItems.push({ type: 'divider' });
          moreItems.push({
            key: 'del',
            danger: true,
            icon: <DeleteOutlined />,
            label: '删除',
            onClick: async () => {
              try {
                await deleteUsingDelete({ id: record.id! });
                message.success('删除成功');
                actionRef.current?.reload();
              } catch {
                // 拦截器已统一提示
              }
            },
          });
        }

        return (
          <Space size={4}>
            {canUpdate && (
              <Button
                type="link"
                size="small"
                icon={<EditOutlined />}
                onClick={() => openEdit(record)}
              >
                编辑
              </Button>
            )}
            {moreItems.length > 0 && (
              <Dropdown menu={{ items: moreItems }} placement="bottomRight">
                <Button type="link" size="small" icon={<EllipsisOutlined />} />
              </Dropdown>
            )}
          </Space>
        );
      },
    },
  ];

  return (
    <>
      <ProTable<API.UserVO>
        rowKey="id"
        actionRef={actionRef}
        columns={columns}
        size="middle"
        scroll={{ x: 1200 }}
        search={{ labelWidth: 80, defaultCollapsed: false }}
        tableAlertRender={({ selectedRowKeys: keys }) => (
          <Space>
            <span>已选</span>
            <a style={{ fontWeight: 600 }}>{keys.length}</a>
            <span>项</span>
          </Space>
        )}
        tableAlertOptionRender={({ selectedRowKeys: keys }) => (
          <Space>
            <Access accessible={canDelete}>
              <Popconfirm
                title={`确认删除选中的 ${keys.length} 个用户？`}
                onConfirm={async () => {
                  setBatchDeleting(true);
                  try {
                    await deleteBatch(keys as number[]);
                    message.success(`已删除 ${keys.length} 项`);
                    setSelectedRowKeys([]);
                    actionRef.current?.reload();
                  } catch {
                    // 拦截器已统一提示
                  } finally {
                    setBatchDeleting(false);
                  }
                }}
              >
                <Button
                  type="link"
                  danger
                  size="small"
                  loading={batchDeleting}
                  icon={<DeleteOutlined />}
                >
                  批量删除
                </Button>
              </Popconfirm>
            </Access>
          </Space>
        )}
        rowSelection={{
          selectedRowKeys,
          onChange: (keys) => setSelectedRowKeys(keys),
        }}
        request={async (params) => {
          // ProTable 传 current/pageSize，后端 UserDTO 用 page/pageSize（@ModelAttribute 平铺）
          const { current, pageSize, ...rest } = params;
          try {
            const res = await page({
              dto: { page: current, pageSize, ...rest } as API.UserDTO,
            } as any);
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
          <Button
            key="reload"
            icon={<ReloadOutlined />}
            onClick={() => actionRef.current?.reload()}
          >
            刷新
          </Button>,
          <AddButton key="add" perm="system:user:create" onClick={openCreate}>
            <PlusOutlined /> 新增用户
          </AddButton>,
        ]}
      />

      <ModalForm
        title={
          <Space>
            <span>{current?.id ? '编辑用户' : '新增用户'}</span>
            {current?.username && (
              <Tag color="blue">{current.username}</Tag>
            )}
          </Space>
        }
        width={680}
        open={modalOpen}
        formRef={formRef}
        onOpenChange={(open) => {
          setModalOpen(open);
          if (!open) {
            setCurrent(undefined);
            formRef.current?.resetFields();
          }
        }}
        loading={editLoading}
        modalProps={{ destroyOnHidden: true }}
        initialValues={current}
        layout="horizontal"
        labelCol={{ flex: '90px' }}
        grid
        rowProps={{ gutter: 8 }}
        onFinish={async (values) => {
          // 新增时校验两次密码一致
          if (!current?.id && values.password !== values.confirmPassword) {
            message.error('两次输入的密码不一致');
            return false;
          }
          const payload = { ...values } as API.UserDTO;
          delete (payload as any).confirmPassword;
          try {
            if (current?.id) {
              await update({ id: current.id }, payload);
              message.success('更新成功');
            } else {
              await create(payload);
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
        {/* 账号信息 */}
        <ProForm.Group
          title="账号信息"
          direction="horizontal"
          labelLayout="default"
        >
          <ProFormText
            name="username"
            label="用户名"
            colProps={{ span: 12 }}
            placeholder="请输入登录用户名"
            rules={[
              { required: true, message: '请输入用户名' },
              {
                pattern: /^[a-zA-Z0-9_]{3,20}$/,
                message: '3-20 位字母、数字、下划线',
              },
            ]}
            disabled={!!current?.id}
          />
          {!current?.id && (
            <>
              <ProFormText.Password
                name="password"
                label="密码"
                colProps={{ span: 12 }}
                placeholder="6-20 位"
                rules={[
                  { required: true, message: '请输入密码' },
                  { min: 6, message: '至少 6 位' },
                ]}
              />
              <ProFormText.Password
                name="confirmPassword"
                label="确认密码"
                colProps={{ span: 12 }}
                placeholder="再次输入密码"
                rules={[{ required: true, message: '请确认密码' }]}
              />
            </>
          )}
        </ProForm.Group>

        {/* 基本信息 */}
        <ProForm.Group title="基本信息">
          <ProFormText
            name="nickname"
            label="昵称"
            colProps={{ span: 12 }}
            placeholder="显示昵称"
          />
          <ProFormText
            name="realName"
            label="真实姓名"
            colProps={{ span: 12 }}
            placeholder="请输入"
          />
          <ProFormRadio.Group
            name="gender"
            label="性别"
            colProps={{ span: 12 }}
            initialValue={GENDER.UNKNOWN}
            options={[
              { label: '男', value: GENDER.MALE },
              { label: '女', value: GENDER.FEMALE },
              { label: '未知', value: GENDER.UNKNOWN },
            ]}
          />
          <ProFormSelect
            name="status"
            label="状态"
            colProps={{ span: 12 }}
            initialValue={STATUS.NORMAL}
            options={[
              { label: '正常', value: STATUS.NORMAL },
              { label: '停用', value: STATUS.DISABLED },
            ]}
          />
          <ProFormText
            name="phone"
            label="手机号"
            colProps={{ span: 12 }}
            placeholder="请输入手机号"
            rules={[{ pattern: /^1\d{10}$/, message: '手机号格式不正确' }]}
          />
          <ProFormText
            name="email"
            label="邮箱"
            colProps={{ span: 12 }}
            placeholder="请输入邮箱"
            rules={[{ type: 'email', message: '邮箱格式不正确' }]}
          />
        </ProForm.Group>

        {/* 组织角色 */}
        <ProForm.Group title="组织角色">
          <ProFormTreeSelect
            name="deptId"
            label="部门"
            colProps={{ span: 12 }}
            placeholder="请选择部门"
            allowClear
            fieldProps={{
              treeData: deptOptions,
              fieldNames: {
                label: 'deptName',
                value: 'id',
                children: 'children',
              },
              treeDefaultExpandAll: true,
              treeNodeFilterProp: 'deptName',
              showSearch: true,
            }}
          />
          <ProFormSelect
            name="roleIds"
            label="角色"
            colProps={{ span: 12 }}
            placeholder="请选择角色（可多选）"
            mode="multiple"
            allowClear
            options={roleOptions}
          />
          <ProFormSelect
            name="postIds"
            label="岗位"
            colProps={{ span: 12 }}
            placeholder="请选择岗位（可多选）"
            mode="multiple"
            allowClear
            options={postOptions}
          />
        </ProForm.Group>

        <ProFormTextArea
          name="remark"
          label="备注"
          colProps={{ span: 24 }}
          placeholder="选填"
          fieldProps={{ autoSize: { minRows: 2, maxRows: 4 } }}
        />
      </ModalForm>

      <ModalForm
        title={
          <Space>
            <KeyOutlined />
            <span>重置密码</span>
            {pwdTarget?.username && <Tag color="blue">{pwdTarget.username}</Tag>}
          </Space>
        }
        width={420}
        open={pwdOpen}
        onOpenChange={setPwdOpen}
        modalProps={{ destroyOnHidden: true }}
        onFinish={async (values) => {
          if (values.password !== values.confirmPassword) {
            message.error('两次输入的密码不一致');
            return false;
          }
          try {
            await resetPassword(
              { id: pwdTarget?.id! },
              { password: values.password } as API.UserDTO,
            );
            message.success('密码已重置');
            setPwdOpen(false);
            return true;
          } catch {
            return false;
          }
        }}
      >
        <ProFormText.Password
          name="password"
          label="新密码"
          placeholder="请输入新密码"
          rules={[
            { required: true, message: '请输入新密码' },
            { min: 6, message: '至少 6 位' },
          ]}
        />
        <ProFormText.Password
          name="confirmPassword"
          label="确认密码"
          placeholder="请再次输入新密码"
          rules={[{ required: true, message: '请确认新密码' }]}
        />
      </ModalForm>
    </>
  );
};

export default UserList;
