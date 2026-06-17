/**
 * 部门管理（树形）
 *
 * - 部门树 /system/dept/tree（tree1，返回 RListSysDept，需取 .data）。
 * - 新增/编辑：ModalForm。
 * - 部门树自带 children，直接展开渲染。
 */
import { AddButton } from '@/components/AccessButton';
import {
  create7 as createDept,
  delete7 as deleteDept,
  tree1 as getDeptTree,
  update8 as updateDept,
} from '@/services/aiperm/dept';
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

const DeptList: React.FC = () => {
  const actionRef = useRef<any>();
  const [modalOpen, setModalOpen] = useState(false);
  const [current, setCurrent] = useState<API.SysDept | undefined>();

  const columns: ProColumns<API.SysDept>[] = [
    { title: '部门名称', dataIndex: 'deptName', width: 200 },
    { title: '负责人', dataIndex: 'leader', width: 100, hideInSearch: true },
    { title: '联系电话', dataIndex: 'phone', width: 130, hideInSearch: true },
    { title: '邮箱', dataIndex: 'email', hideInSearch: true, ellipsis: true },
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
            setCurrent({ parentId: record.id } as API.SysDept);
            setModalOpen(true);
          }}
        >
          新增子项
        </a>,
        <Popconfirm
          key="del"
          title="确认删除该部门？"
          onConfirm={async () => {
            try {
              await deleteDept({ id: record.id! });
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
      <ProTable<API.SysDept>
        rowKey="id"
        actionRef={actionRef}
        columns={columns}
        search={false}
        pagination={false}
        expandable={{ defaultExpandAllRows: true }}
        request={async () => {
          try {
            const res: any = await getDeptTree();
            const list: API.SysDept[] = res?.data ?? res ?? [];
            return { data: list, success: true };
          } catch {
            return { data: [], success: false };
          }
        }}
        toolBarRender={() => [
          <AddButton
            key="add"
            onClick={() => {
              setCurrent({ parentId: 0 } as API.SysDept);
              setModalOpen(true);
            }}
          >
            新增部门
          </AddButton>,
        ]}
      />

      <ModalForm
        title={current?.id ? '编辑部门' : '新增部门'}
        open={modalOpen}
        onOpenChange={setModalOpen}
        initialValues={current}
        width={720}
        grid
        rowProps={{ gutter: 16 }}
        modalProps={{ destroyOnHidden: true }}
        onFinish={async (values) => {
          const payload = { ...current, ...values } as API.DeptDTO;
          try {
            if (current?.id) {
              await updateDept({ id: current.id }, payload);
              message.success('更新成功');
            } else {
              await createDept(payload);
              message.success('创建成功');
            }
            setModalOpen(false);
            actionRef.current?.reload();
            return true;
          } catch {
            // 业务失败已在拦截器统一提示，这里吞掉避免 Unhandled Rejection 崩溃
            return false;
          }
        }}
      >
        <ProFormText
          name="deptName"
          label="部门名称"
          colProps={{ span: 12 }}
          rules={[{ required: true, message: '请输入部门名称' }]}
        />
        <ProFormDigit name="parentId" label="父级ID" colProps={{ span: 12 }} min={0} />
        <ProFormDigit name="sort" label="排序" colProps={{ span: 12 }} min={0} />
        <ProFormText name="leader" label="负责人" colProps={{ span: 12 }} />
        <ProFormText name="phone" label="联系电话" colProps={{ span: 12 }} />
        <ProFormText name="email" label="邮箱" colProps={{ span: 12 }} rules={[{ type: 'email', message: '邮箱格式不正确' }]} />
        <ProFormSelect
          name="status"
          label="状态"
          colProps={{ span: 12 }}
          options={[
            { label: '正常', value: 1 },
            { label: '停用', value: 0 },
          ]}
        />
        <ProFormTextArea name="remark" label="备注" colProps={{ span: 24 }} />
      </ModalForm>
    </>
  );
};

export default DeptList;
