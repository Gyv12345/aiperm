/**
 * 字典管理
 *
 * 布局：主表全宽展示「字典类型」，点击某类型的「数据」按钮从右侧滑出
 * 抽屉，在抽屉内全宽管理该类型的字典数据明细。
 *
 * - 字典类型：/system/dict/type（list2 分页、create5、update6、delete5）。
 * - 字典数据：/system/dict/data（listByDictType、create6、update7、delete6）。
 *
 * 注意：字典 status 语义为 0=禁用 1=启用（与其他实体相反）。
 */
import { AddButton } from '@/components/AccessButton';
import {
  create5 as createDictType,
  delete5 as deleteDictType,
  list2 as pageDictType,
  update6 as updateDictType,
} from '@/services/aiperm/dictType';
import {
  create6 as createDictData,
  delete6 as deleteDictData,
  listByDictType,
  update7 as updateDictData,
} from '@/services/aiperm/dictData';
import {
  ModalForm,
  ProColumns,
  ProFormDigit,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import { Drawer, Popconfirm, Tag, message } from 'antd';
import React, { useEffect, useRef, useState } from 'react';

const Dict: React.FC = () => {
  const typeActionRef = useRef<any>();
  const [typeModalOpen, setTypeModalOpen] = useState(false);
  const [currentType, setCurrentType] = useState<API.DictTypeVO | undefined>();
  const [selectedType, setSelectedType] = useState<API.DictTypeVO | undefined>();
  const [dataDrawerOpen, setDataDrawerOpen] = useState(false);
  const [dataModalOpen, setDataModalOpen] = useState(false);
  const [currentData, setCurrentData] = useState<API.DictDataVO | undefined>();

  // 抽屉内字典数据列表 actionRef
  const dataActionRefInner = useRef<any>();

  // 抽屉打开时加载数据
  useEffect(() => {
    if (dataDrawerOpen) {
      dataActionRefInner.current?.reload();
    }
  }, [dataDrawerOpen, selectedType]);

  const statusEnum = {
    0: { text: '禁用', status: 'Error' },
    1: { text: '启用', status: 'Success' },
  };

  /** 打开数据抽屉：选中类型并展开抽屉 */
  const openDataDrawer = (type: API.DictTypeVO) => {
    setSelectedType(type);
    setDataDrawerOpen(true);
  };

  const typeColumns: ProColumns<API.DictTypeVO>[] = [
    {
      title: '字典名称',
      dataIndex: 'dictName',
      render: (_, r) => (
        <a
          onClick={() => openDataDrawer(r)}
          style={{ fontWeight: r.id === selectedType?.id ? 600 : 400 }}
        >
          {r.dictName}
        </a>
      ),
    },
    {
      title: '字典类型',
      dataIndex: 'dictType',
      hideInSearch: true,
      render: (_, r) => (
        <a
          onClick={() => openDataDrawer(r)}
          style={{ fontWeight: r.id === selectedType?.id ? 600 : 400 }}
        >
          {r.dictType}
        </a>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: statusEnum,
      render: (_, r) =>
        r.status === 1 ? <Tag color="success">启用</Tag> : <Tag color="error">禁用</Tag>,
    },
    { title: '备注', dataIndex: 'remark', hideInSearch: true, ellipsis: true },
    { title: '创建时间', dataIndex: 'createTime', valueType: 'dateTime', hideInSearch: true },
    {
      title: '操作',
      valueType: 'option',
      width: 180,
      render: (_, record) => [
        <a key="data" onClick={() => openDataDrawer(record)}>数据</a>,
        <a
          key="edit"
          onClick={() => {
            setCurrentType(record);
            setTypeModalOpen(true);
          }}
        >
          编辑
        </a>,
        <Popconfirm
          key="del"
          title="确认删除该字典类型？"
          onConfirm={async () => {
            try {
              await deleteDictType({ id: record.id! });
              message.success('删除成功');
              typeActionRef.current?.reload();
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

  const dataColumns: ProColumns<API.DictDataVO>[] = [
    { title: '字典标签', dataIndex: 'dictLabel' },
    { title: '字典键值', dataIndex: 'dictValue' },
    { title: '排序', dataIndex: 'sort', hideInSearch: true, width: 70 },
    {
      title: '状态',
      dataIndex: 'status',
      hideInSearch: true,
      width: 90,
      render: (_, r) =>
        r.status === 1 ? <Tag color="success">启用</Tag> : <Tag color="error">禁用</Tag>,
    },
    { title: '样式', dataIndex: 'listClass', hideInSearch: true },
    { title: '备注', dataIndex: 'remark', hideInSearch: true, ellipsis: true },
    {
      title: '操作',
      valueType: 'option',
      width: 140,
      render: (_, record) => [
        <a
          key="edit"
          onClick={() => {
            setCurrentData(record);
            setDataModalOpen(true);
          }}
        >
          编辑
        </a>,
        <Popconfirm
          key="del"
          title="确认删除该字典数据？"
          onConfirm={async () => {
            try {
              await deleteDictData({ id: record.id! });
              message.success('删除成功');
              dataActionRefInner.current?.reload();
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
      <ProTable<API.DictTypeVO>
        rowKey="id"
        actionRef={typeActionRef}
        columns={typeColumns}
        search={{ labelWidth: 'auto' }}
        onRow={(record) => ({
          onClick: () => openDataDrawer(record),
          style: { cursor: 'pointer' },
        })}
        rowClassName={(record) =>
          record.id === selectedType?.id ? 'dict-row-selected' : ''
        }
        request={async (params) => {
          const { current, pageSize, ...rest } = params;
          try {
            const res = await pageDictType({
              dto: { page: current, pageSize, ...rest } as API.DictTypeDTO,
            } as any);
            const data: API.PageResultDictTypeVO = (res as any) || {};
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
              setCurrentType(undefined);
              setTypeModalOpen(true);
            }}
          >
            新增类型
          </AddButton>,
        ]}
      />

      {/* 字典数据抽屉 */}
      <Drawer
        title={selectedType ? `字典数据 - ${selectedType.dictType}` : '字典数据'}
        width={960}
        open={dataDrawerOpen}
        onClose={() => setDataDrawerOpen(false)}
        destroyOnClose
      >
        <ProTable<API.DictDataVO>
          rowKey="id"
          actionRef={dataActionRefInner}
          columns={dataColumns}
          search={false}
          options={false}
          pagination={false}
          request={async () => {
            if (!selectedType) return { data: [], total: 0, success: true };
            try {
              const res: any = await listByDictType({
                dictType: selectedType.dictType,
              } as any);
              const list: API.DictDataVO[] = res?.data ?? res ?? [];
              return { data: list, success: true };
            } catch {
              return { data: [], success: false };
            }
          }}
          locale={{
            emptyText: '暂无数据',
          }}
          toolBarRender={() => [
            <AddButton
              key="add"
              onClick={() => {
                setCurrentData({
                  dictType: selectedType?.dictType,
                } as API.DictDataVO);
                setDataModalOpen(true);
              }}
            >
              新增数据
            </AddButton>,
          ]}
        />
      </Drawer>

      {/* 字典类型表单 */}
      <ModalForm
        title={currentType?.id ? '编辑字典类型' : '新增字典类型'}
        open={typeModalOpen}
        onOpenChange={setTypeModalOpen}
        initialValues={currentType}
        width={640}
        grid
        rowProps={{ gutter: 16 }}
        modalProps={{ destroyOnHidden: true }}
        onFinish={async (values) => {
          try {
            if (currentType?.id) {
              await updateDictType({ id: currentType.id }, values as API.DictTypeDTO);
              message.success('更新成功');
            } else {
              await createDictType(values as API.DictTypeDTO);
              message.success('创建成功');
            }
            setTypeModalOpen(false);
            typeActionRef.current?.reload();
            return true;
          } catch {
            return false;
          }
        }}
      >
        <ProFormText
          name="dictName"
          label="字典名称"
          colProps={{ span: 12 }}
          rules={[{ required: true, message: '请输入字典名称' }]}
        />
        <ProFormText
          name="dictType"
          label="字典类型"
          colProps={{ span: 12 }}
          rules={[{ required: true, message: '请输入字典类型标识' }]}
        />
        <ProFormSelect
          name="status"
          label="状态"
          colProps={{ span: 12 }}
          options={[
            { label: '启用', value: 1 },
            { label: '禁用', value: 0 },
          ]}
        />
        <ProFormTextArea name="remark" label="备注" colProps={{ span: 24 }} />
      </ModalForm>

      {/* 字典数据表单 */}
      <ModalForm
        title={currentData?.id ? '编辑字典数据' : '新增字典数据'}
        open={dataModalOpen}
        onOpenChange={setDataModalOpen}
        initialValues={currentData}
        width={720}
        grid
        rowProps={{ gutter: 16 }}
        modalProps={{ destroyOnHidden: true }}
        onFinish={async (values) => {
          const payload = { ...currentData, ...values } as API.DictDataDTO;
          try {
            if (currentData?.id) {
              await updateDictData({ id: currentData.id }, payload);
              message.success('更新成功');
            } else {
              await createDictData(payload);
              message.success('创建成功');
            }
            setDataModalOpen(false);
            dataActionRefInner.current?.reload();
            return true;
          } catch {
            return false;
          }
        }}
      >
        <ProFormText name="dictType" label="字典类型" colProps={{ span: 12 }} disabled />
        <ProFormText
          name="dictLabel"
          label="字典标签"
          colProps={{ span: 12 }}
          rules={[{ required: true, message: '请输入字典标签' }]}
        />
        <ProFormText
          name="dictValue"
          label="字典键值"
          colProps={{ span: 12 }}
          rules={[{ required: true, message: '请输入字典键值' }]}
        />
        <ProFormDigit name="sort" label="排序" colProps={{ span: 12 }} min={0} />
        <ProFormSelect
          name="status"
          label="状态"
          colProps={{ span: 12 }}
          options={[
            { label: '启用', value: 1 },
            { label: '禁用', value: 0 },
          ]}
        />
        <ProFormText name="listClass" label="样式属性" colProps={{ span: 12 }} placeholder="如 success / #ff5500" />
        <ProFormTextArea name="remark" label="备注" colProps={{ span: 24 }} />
      </ModalForm>
    </>
  );
};

export default Dict;
