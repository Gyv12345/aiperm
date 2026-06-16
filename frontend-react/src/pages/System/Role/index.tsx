/**
 * 角色管理
 *
 * - ProTable 分页查询 /system/role。
 * - 新增/编辑：ModalForm。
 * - 分配菜单：DrawerForm + 菜单树（/system/menu/tree）。
 *
 * 注意：getRoleMenus 返回 RListLong（数组包装未自动解包），手动取 .data。
 */
import { AddButton } from '@/components/AccessButton';
import { tree as getMenuTree } from '@/services/aiperm/menu';
import {
  assignMenus,
  create1 as createRole,
  delete1 as deleteRole,
  getRoleMenus,
  page1 as pageRole,
  update1 as updateRole,
} from '@/services/aiperm/role';
import {
  DrawerForm,
  ModalForm,
  ProFormDigit,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-components';
import { Popconfirm, Tag, Tree, message } from 'antd';
import type { DataNode } from 'antd/es/tree';
import React, { useEffect, useRef, useState } from 'react';

/** 后端菜单树节点 → antd Tree DataNode */
function toTreeData(nodes: any[] | undefined): DataNode[] {
  if (!nodes) return [];
  return nodes.map((n) => ({
    key: n.id,
    title: n.menuName,
    children: toTreeData(n.children),
  }));
}

const RoleList: React.FC = () => {
  const actionRef = useRef<any>();
  const [modalOpen, setModalOpen] = useState(false);
  const [current, setCurrent] = useState<API.SysRole | undefined>();
  const [menuDrawer, setMenuDrawer] = useState(false);
  const [menuTarget, setMenuTarget] = useState<API.SysRole | undefined>();
  const [menuTree, setMenuTree] = useState<DataNode[]>([]);
  const [checkedKeys, setCheckedKeys] = useState<React.Key[]>([]);

  const loadMenuTree = async () => {
    try {
      const res: any = await getMenuTree();
      // RListSysMenu 未解包，取 data
      const list = res?.data ?? res ?? [];
      setMenuTree(toTreeData(list));
    } catch {
      // 业务失败已在拦截器统一提示，这里静默兜底避免崩溃
    }
  };

  const columns: ProColumns<API.SysRole>[] = [
    { title: 'ID', dataIndex: 'id', width: 70, hideInSearch: true },
    { title: '角色名称', dataIndex: 'roleName' },
    { title: '角色编码', dataIndex: 'roleCode', hideInSearch: true },
    { title: '排序', dataIndex: 'sort', hideInSearch: true },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: {
        1: { text: '正常', status: 'Success' },
        0: { text: '停用', status: 'Error' },
      },
      render: (_, r) =>
        r.status === 1 ? <Tag color="success">正常</Tag> : <Tag color="error">停用</Tag>,
    },
    { title: '备注', dataIndex: 'remark', hideInSearch: true, ellipsis: true },
    {
      title: '操作',
      valueType: 'option',
      width: 200,
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
          key="menu"
          onClick={async () => {
            setMenuTarget(record);
            setMenuDrawer(true);
            await loadMenuTree();
            try {
              const res: any = await getRoleMenus({ id: record.id! });
              const ids: number[] = res?.data ?? res ?? [];
              setCheckedKeys(ids.map(String));
            } catch {
              setCheckedKeys([]);
            }
          }}
        >
          分配菜单
        </a>,
        <Popconfirm
          key="del"
          title="确认删除该角色？"
          onConfirm={async () => {
            await deleteRole({ id: record.id! });
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
      <ProTable<API.SysRole>
        rowKey="id"
        actionRef={actionRef}
        columns={columns}
        search={{ labelWidth: 'auto' }}
        request={async (params) => {
          const { current, pageSize, ...rest } = params;
          const res = await pageRole({
            dto: { page: current, pageSize, ...rest } as API.RoleDTO,
          } as any);
          const data: API.PageResultSysRole = (res as any) || {};
          return {
            data: data.list || [],
            total: data.total || 0,
            success: true,
          };
        }}
        toolBarRender={() => [
          <AddButton
            key="add"
            onClick={() => {
              setCurrent(undefined);
              setModalOpen(true);
            }}
          >
            新增角色
          </AddButton>,
        ]}
      />

      <ModalForm
        title={current ? '编辑角色' : '新增角色'}
        open={modalOpen}
        onOpenChange={setModalOpen}
        initialValues={current}
        modalProps={{ destroyOnClose: true }}
        onFinish={async (values) => {
          try {
            if (current?.id) {
              await updateRole({ id: current.id }, values as API.RoleDTO);
              message.success('更新成功');
            } else {
              await createRole(values as API.RoleDTO);
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
          name="roleName"
          label="角色名称"
          rules={[{ required: true, message: '请输入角色名称' }]}
        />
        <ProFormText
          name="roleCode"
          label="角色编码"
          rules={[{ required: true, message: '请输入角色编码' }]}
        />
        <ProFormDigit name="sort" label="排序" min={0} />
        <ProFormSelect
          name="status"
          label="状态"
          options={[
            { label: '正常', value: 1 },
            { label: '停用', value: 0 },
          ]}
        />
        <ProFormTextArea name="remark" label="备注" />
      </ModalForm>

      <DrawerForm
        title={`分配菜单 - ${menuTarget?.roleName || ''}`}
        open={menuDrawer}
        onOpenChange={setMenuDrawer}
        drawerProps={{ destroyOnClose: true, width: 480 }}
        submitter={{ searchConfig: { submitText: '保存' } }}
        onFinish={async () => {
          await assignMenus(
            { id: menuTarget?.id! },
            checkedKeys.map((k) => Number(k)),
          );
          message.success('分配成功');
          setMenuDrawer(false);
          return true;
        }}
      >
        <Tree
          checkable
          defaultExpandAll
          treeData={menuTree}
          checkedKeys={checkedKeys}
          onCheck={(keys) => setCheckedKeys(keys as React.Key[])}
        />
      </DrawerForm>
    </>
  );
};

export default RoleList;
