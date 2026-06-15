/**
 * 参数配置管理
 *
 * - 分页查询 /enterprise/config（list7）。
 * - 新增/编辑：ModalForm（configKey 必填）。
 * - 操作：删除。
 */
import { AddButton } from '@/components/AccessButton';
import {
  create11 as createConfig,
  delete11 as deleteConfig,
  list7 as pageConfig,
  update12 as updateConfig,
} from '@/services/aiperm/config';
import {
  ModalForm,
  ProFormText,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-components';
import { Popconfirm, message } from 'antd';
import React, { useRef, useState } from 'react';

const ConfigList: React.FC = () => {
  const actionRef = useRef<any>();
  const [modalOpen, setModalOpen] = useState(false);
  const [current, setCurrent] = useState<API.ConfigVO | undefined>();

  const columns: ProColumns<API.ConfigVO>[] = [
    { title: 'ID', dataIndex: 'id', width: 70, hideInSearch: true },
    { title: '配置键', dataIndex: 'configKey' },
    { title: '配置值', dataIndex: 'configValue', hideInSearch: true, ellipsis: true },
    { title: '配置类型', dataIndex: 'configType', hideInSearch: true },
    { title: '备注', dataIndex: 'remark', hideInSearch: true, ellipsis: true },
    { title: '创建时间', dataIndex: 'createTime', valueType: 'dateTime', hideInSearch: true },
    {
      title: '操作',
      valueType: 'option',
      width: 140,
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
          title="确认删除？"
          onConfirm={async () => {
            await deleteConfig({ id: record.id! } as any);
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
      <ProTable<API.ConfigVO>
        rowKey="id"
        actionRef={actionRef}
        columns={columns}
        search={{ labelWidth: 'auto' }}
        request={async (params) => {
          const { current, pageSize, ...rest } = params;
          try {
            const res = await pageConfig({
              dto: { page: current, pageSize, ...rest } as API.ConfigDTO,
            } as any);
            const data: API.PageResultConfigVO = (res as any) || {};
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
            新增配置
          </AddButton>,
        ]}
      />

      <ModalForm
        title={current?.id ? '编辑配置' : '新增配置'}
        open={modalOpen}
        onOpenChange={setModalOpen}
        initialValues={current}
        modalProps={{ destroyOnHidden: true }}
        onFinish={async (values) => {
          if (current?.id) {
            await updateConfig({ id: current.id } as any, values as API.ConfigDTO);
            message.success('更新成功');
          } else {
            await createConfig(values as API.ConfigDTO);
            message.success('创建成功');
          }
          setModalOpen(false);
          actionRef.current?.reload();
          return true;
        }}
      >
        <ProFormText
          name="configKey"
          label="配置键"
          rules={[{ required: true, message: '请输入配置键' }]}
          disabled={!!current?.id}
        />
        <ProFormText name="configValue" label="配置值" />
        <ProFormText name="configType" label="配置类型" />
        <ProFormTextArea name="remark" label="备注" />
      </ModalForm>
    </>
  );
};

export default ConfigList;
