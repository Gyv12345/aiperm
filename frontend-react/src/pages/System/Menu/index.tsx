/**
 * 菜单管理
 *
 * - 树形 ProTable，数据来自 /system/menu（全量列表）。
 * - 新增/编辑：ModalForm，菜单类型 M=目录/C=菜单/F=按钮。
 *
 * 注意：list1 返回 RListSysMenu（未解包），手动取 .data。
 */
import { AddButton } from '@/components/AccessButton';
import {
  create4 as createMenu,
  delete4 as deleteMenu,
  list1 as listMenu,
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
import { Popconfirm, Tag, message } from 'antd';
import React, { useRef, useState } from 'react';

const MENU_TYPE_MAP: Record<string, { text: string; color: string }> = {
  M: { text: '目录', color: 'blue' },
  C: { text: '菜单', color: 'green' },
  F: { text: '按钮', color: 'orange' },
};

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
    { title: '图标', dataIndex: 'icon', width: 100, hideInSearch: true },
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
        r.status === 0 ? <Tag color="success">正常</Tag> : <Tag color="error">停用</Tag>,
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
        expandable={{ defaultExpandAllRows: true }}
        request={async () => {
          try {
            const res: any = await listMenu();
            // RListSysMenu 未解包，取 data
            const list: API.SysMenu[] = res?.data ?? res ?? [];
            return { data: list, success: true };
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
        }}
      >
        <ProFormSelect
          name="menuType"
          label="菜单类型"
          rules={[{ required: true, message: '请选择菜单类型' }]}
          options={[
            { label: '目录', value: 'M' },
            { label: '菜单', value: 'C' },
            { label: '按钮', value: 'F' },
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
            { label: '正常', value: 0 },
            { label: '停用', value: 1 },
          ]}
        />
        <ProFormTextArea name="remark" label="备注" />
      </ModalForm>
    </>
  );
};

export default MenuList;
