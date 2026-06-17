/**
 * 部门管理（树形）
 *
 * - 部门树 /system/dept/tree（tree1，返回 RListSysDept，需取 .data）。
 * - 新增/编辑：ModalForm。
 *   - 父级部门：ProFormTreeSelect（复用部门树，编辑时排除自身及子孙避免循环引用）。
 *   - 负责人：ProFormSelect 远程搜索（按用户名查 user.page，value 存 user id）。
 * - 部门树自带 children，直接展开渲染。
 */
import { AddButton } from '@/components/AccessButton';
import {
  create7 as createDept,
  delete7 as deleteDept,
  tree1 as getDeptTree,
  update8 as updateDept,
} from '@/services/aiperm/dept';
import * as userService from '@/services/aiperm/user';
import {
  ModalForm,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  ProFormTreeSelect,
  ProTable,
} from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-components';
import { Popconfirm, Tag, message } from 'antd';
import React, { useRef, useState } from 'react';

const DeptList: React.FC = () => {
  const actionRef = useRef<any>();
  const formRef = useRef<any>();
  const [modalOpen, setModalOpen] = useState(false);
  const [current, setCurrent] = useState<API.SysDept | undefined>();

  // 父级部门树选项（弹窗打开时加载，用 fieldNames 直接喂后端树，无需转换）
  const [deptTreeOptions, setDeptTreeOptions] = useState<any[]>([]);
  // 负责人下拉选项：{label,value}[]，value=user id
  const [leaderOptions, setLeaderOptions] = useState<{ label: string; value: number }[]>([]);

  /** 把 UserVO 组装成下拉展示文案：真实姓名(用户名) */
  const formatUserLabel = (u: API.UserVO): string => {
    const name = u.realName || u.nickname || u.username || `用户${u.id}`;
    return u.username ? `${name}(${u.username})` : name;
  };

  /**
   * 收集某个部门的所有子孙 id（含自身），用于父级下拉排除。
   * 部门不能挂到自身或自己的子孙下，否则形成循环。
   */
  const collectSubIds = (nodes: API.SysDept[], targetId?: number): Set<number> => {
    const result = new Set<number>();
    if (!targetId) return result;
    const walk = (list: API.SysDept[]) => {
      for (const n of list) {
        if (n.id === targetId) {
          result.add(n.id!);
          if (n.children) walk(n.children);
        } else if (n.children) {
          walk(n.children);
        }
      }
    };
    walk(nodes);
    return result;
  };

  /**
   * 从整棵树中剔除指定 id 集合的节点（递归过滤，返回新树）。
   * 用于父级下拉排除"自身及子孙"。
   */
  const filterTree = (nodes: API.SysDept[], excludeIds: Set<number>): API.SysDept[] =>
    nodes
      .filter((n) => !excludeIds.has(n.id!))
      .map((n) => ({
        ...n,
        children: n.children ? filterTree(n.children, excludeIds) : [],
      }));

  /** 加载部门树作为父级下拉候选；编辑场景排除自身及子孙 */
  const loadDeptTree = async (editingId?: number) => {
    try {
      const res: any = await getDeptTree();
      let tree: API.SysDept[] = res?.data ?? res ?? [];
      if (editingId) {
        const excludeIds = collectSubIds(tree, editingId);
        tree = filterTree(tree, excludeIds);
      }
      setDeptTreeOptions(tree as any[]);
    } catch {
      setDeptTreeOptions([]);
    }
  };

  /** 编辑回填：按 leader(user id) 查用户名，组装成下拉初始选项 */
  const loadLeaderLabel = async (leaderId?: number) => {
    if (!leaderId) return;
    try {
      const detail: any = await userService.getById({ id: leaderId });
      const u: API.UserVO = detail?.data ?? detail;
      if (u && u.id) {
        setLeaderOptions([{ label: formatUserLabel(u), value: u.id }]);
      }
    } catch {
      // 详情拉取失败则下拉只显示 id
    }
  };

  /** 负责人远程搜索：输入关键字查用户列表 */
  const searchLeaders = async (keyword: string) => {
    if (!keyword) {
      setLeaderOptions([]);
      return;
    }
    try {
      const res: any = await userService.page({
        dto: { username: keyword, page: 1, pageSize: 20 } as API.UserDTO,
      } as any);
      const data: API.PageResultUserVO = res?.data ?? res ?? {};
      const list = data.list ?? [];
      setLeaderOptions(list.map((u) => ({ label: formatUserLabel(u), value: u.id! })));
    } catch {
      setLeaderOptions([]);
    }
  };

  /** 打开新增弹窗 */
  const openCreate = async () => {
    setCurrent({ parentId: 0 } as API.SysDept);
    setLeaderOptions([]);
    setModalOpen(true);
    await loadDeptTree();
    formRef.current?.setFieldsValue({ parentId: 0 });
  };

  /** 打开编辑弹窗 */
  const openEdit = async (record: API.SysDept) => {
    setCurrent(record);
    setLeaderOptions([]);
    setModalOpen(true);
    await loadDeptTree(record.id);
    formRef.current?.setFieldsValue(record);
    // 负责人是 user id，需反查用户名组装下拉 label
    if (record.leader) {
      loadLeaderLabel(record.leader);
    }
  };

  /** 新增子部门 */
  const openAddChild = async (record: API.SysDept) => {
    setCurrent({ parentId: record.id } as API.SysDept);
    setLeaderOptions([]);
    setModalOpen(true);
    await loadDeptTree();
    formRef.current?.setFieldsValue({ parentId: record.id });
  };

  const columns: ProColumns<API.SysDept>[] = [
    { title: '部门名称', dataIndex: 'deptName', width: 200 },
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
        <a key="edit" onClick={() => openEdit(record)}>
          编辑
        </a>,
        <a key="add-child" onClick={() => openAddChild(record)}>
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
          <AddButton key="add" onClick={openCreate}>
            新增部门
          </AddButton>,
        ]}
      />

      <ModalForm
        title={current?.id ? '编辑部门' : '新增部门'}
        open={modalOpen}
        onOpenChange={setModalOpen}
        formRef={formRef}
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
        <ProFormTreeSelect
          name="parentId"
          label="父级部门"
          colProps={{ span: 12 }}
          placeholder="请选择父级部门"
          allowClear
          rules={[{ required: true, message: '请选择父级部门' }]}
          fieldProps={{
            treeData: deptTreeOptions,
            fieldNames: { label: 'deptName', value: 'id', children: 'children' },
            treeDefaultExpandAll: true,
            treeNodeFilterProp: 'deptName',
            showSearch: true,
          }}
        />
        <ProFormSelect
          name="leader"
          label="负责人"
          colProps={{ span: 12 }}
          placeholder="输入用户名/姓名搜索"
          allowClear
          showSearch
          options={leaderOptions}
          fieldProps={{
            filterOption: false,
            onSearch: (kw: string) => searchLeaders(kw),
          }}
        />
        <ProFormText name="phone" label="联系电话" colProps={{ span: 12 }} />
        <ProFormText
          name="email"
          label="邮箱"
          colProps={{ span: 12 }}
          rules={[{ type: 'email', message: '邮箱格式不正确' }]}
        />
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
