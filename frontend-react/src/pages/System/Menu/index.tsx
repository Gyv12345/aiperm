/**
 * 菜单管理
 *
 * - 树形 ProTable，数据来自 /system/menu/tree（后端已组装成嵌套树，
 *   节点 children 字段即子菜单，与 ProTable 默认 childrenColumnName 对齐）。
 * - 新增/编辑：ModalForm，菜单类型 1=目录/2=菜单/3=按钮（后端存储数字字符串）。
 */
import { AddButton } from '@/components/AccessButton';
import {
  create4 as createMenu,
  delete4 as deleteMenu,
  tree as menuTree,
  update4 as updateMenu,
} from '@/services/aiperm/menu';
import {
  ModalForm,
  ProFormDigit,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-components';
import * as AllIcons from '@ant-design/icons';
import { Popconfirm, Tag, message } from 'antd';
import React, { useRef, useState } from 'react';

/**
 * 菜单类型映射。
 * 后端实际存储数字字符串：'1'=目录 / '2'=菜单 / '3'=按钮；
 * 同时兼容部分历史数据可能用 M/C/F（OpenAPI schema 标注）。
 */
const MENU_TYPE_MAP: Record<string, { text: string; color: string }> = {
  '1': { text: '目录', color: 'blue' },
  '2': { text: '菜单', color: 'green' },
  '3': { text: '按钮', color: 'orange' },
  M: { text: '目录', color: 'blue' },
  C: { text: '菜单', color: 'green' },
  F: { text: '按钮', color: 'orange' },
};

/**
 * 把后端存的图标驼峰名（如 Odometer / Setting）渲染为 antd 图标。
 * 后端约定：存不带 Outlined 后缀的 PascalCase 名称，这里拼上 Outlined 后查表。
 * 查不到或为空时回退显示原始字符串 / '-'。
 */
function renderIcon(name?: string) {
  if (!name) return <span style={{ color: '#ccc' }}>-</span>;
  const fullName = `${name}Outlined`;
  const IconComp = (AllIcons as any)[fullName];
  if (IconComp) return <IconComp />;
  return <span>{name}</span>;
}

const MenuList: React.FC = () => {
  const actionRef = useRef<any>();
  const [modalOpen, setModalOpen] = useState(false);
  const [current, setCurrent] = useState<API.SysMenu | undefined>();

  const columns: ProColumns<API.SysMenu>[] = [
    { title: '菜单名称', dataIndex: 'menuName', width: 200 },
    {
      title: '类型',
      dataIndex: 'menuType',
      width: 80,
      hideInSearch: true,
      render: (_, r) => {
        const t = MENU_TYPE_MAP[r.menuType || ''];
        return t ? <Tag color={t.color}>{t.text}</Tag> : r.menuType;
      },
    },
    {
      title: '图标',
      dataIndex: 'icon',
      width: 80,
      align: 'center',
      hideInSearch: true,
      render: (_, r) => renderIcon(r.icon),
    },
    { title: '路由地址', dataIndex: 'path', hideInSearch: true, ellipsis: true },
    { title: '组件路径', dataIndex: 'component', hideInSearch: true, ellipsis: true },
    { title: '权限标识', dataIndex: 'perms', hideInSearch: true, ellipsis: true },
    { title: '排序', dataIndex: 'sort', width: 70, hideInSearch: true },
    {
      title: '状态',
      dataIndex: 'status',
      width: 80,
      hideInSearch: true,
      render: (_, r) =>
        r.status === 1 ? <Tag color="success">正常</Tag> : <Tag color="error">停用</Tag>,
    },
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
          key="add-child"
          onClick={() => {
            setCurrent({ parentId: record.id } as API.SysMenu);
            setModalOpen(true);
          }}
        >
          新增子项
        </a>,
        <Popconfirm
          key="del"
          title="确认删除该菜单？"
          onConfirm={async () => {
            await deleteMenu({ id: record.id! });
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
      <ProTable<API.SysMenu>
        rowKey="id"
        actionRef={actionRef}
        columns={columns}
        search={false}
        pagination={false}
        expandable={{
          defaultExpandAllRows: true,
          // ProTable 树形缩进依赖 childrenColumnName（默认 children）
          childrenColumnName: 'children',
        }}
        request={async () => {
          try {
            const res: any = await menuTree();
            // tree 返回 RListSysMenu：responseInterceptor 已解包，这里兼容两种形态
            const treeData: API.SysMenu[] = res?.data ?? res ?? [];
            return { data: treeData, success: true };
          } catch {
            return { data: [], success: false };
          }
        }}
        toolBarRender={() => [
          <AddButton
            key="add"
            onClick={() => {
              setCurrent({ parentId: 0 } as API.SysMenu);
              setModalOpen(true);
            }}
          >
            新增菜单
          </AddButton>,
        ]}
      />

      <ModalForm
        title={current?.id ? '编辑菜单' : '新增菜单'}
        open={modalOpen}
        onOpenChange={setModalOpen}
        initialValues={current}
        modalProps={{ destroyOnClose: true }}
        onFinish={async (values) => {
          const payload = { ...current, ...values } as API.MenuDTO;
          try {
            if (current?.id) {
              await updateMenu({ id: current.id }, payload);
              message.success('更新成功');
            } else {
              await createMenu(payload);
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
        <ProFormSelect
          name="menuType"
          label="菜单类型"
          rules={[{ required: true, message: '请选择菜单类型' }]}
          options={[
            { label: '目录', value: '1' },
            { label: '菜单', value: '2' },
            { label: '按钮', value: '3' },
          ]}
        />
        <ProFormText
          name="menuName"
          label="菜单名称"
          rules={[{ required: true, message: '请输入菜单名称' }]}
        />
        <ProFormDigit name="parentId" label="父级ID" min={0} />
        <ProFormDigit name="sort" label="排序" min={0} />
        <ProFormText name="path" label="路由地址" />
        <ProFormText name="component" label="组件路径" />
        <ProFormText name="perms" label="权限标识" placeholder="如 system:user:add" />
        <ProFormText name="icon" label="图标" />
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
    </>
  );
};

export default MenuList;
