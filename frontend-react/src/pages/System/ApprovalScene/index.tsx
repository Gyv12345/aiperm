/**
 * 审批场景管理
 *
 * - ProTable 分页查询 /system/approval-scene（page3，DTO 嵌套分页）。
 * - 新增/编辑：ModalForm；处理器下拉异步加载（/handlers）。
 * - 删除：Popconfirm。
 *
 * 状态语义：enabled 1=启用，0=禁用；autoSubmitEnabled 1=是，0=否；
 * allowDuplicatePending 1=允许，0=拦截。
 */
import { AddButton } from '@/components/AccessButton';
import {
  create8 as createScene,
  delete8 as deleteScene,
  detail2 as getScene,
  handlers as listHandlers,
  page3 as pageScene,
  update9 as updateScene,
} from '@/services/aiperm/approvalScene';
import {
  ModalForm,
  ProFormGroup,
  ProFormSelect,
  ProFormText,
  ProFormDigit,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-components';
import { Form, Popconfirm, Tag, message } from 'antd';
import React, { useEffect, useRef, useState } from 'react';

/** 平台可选项 */
const PLATFORM_OPTIONS = [
  { label: '飞书 FEISHU', value: 'FEISHU' },
  { label: '企微 WEWORK', value: 'WEWORK' },
  { label: '钉钉 DINGTALK', value: 'DINGTALK' },
];

/** 超时动作可选项 */
const TIMEOUT_ACTION_OPTIONS = [
  { label: '通知', value: 'NOTIFY' },
  { label: '自动通过', value: 'AUTO_PASS' },
  { label: '自动拒绝', value: 'AUTO_REJECT' },
];

const SceneList: React.FC = () => {
  const actionRef = useRef<any>();
  const [modalOpen, setModalOpen] = useState(false);
  const [current, setCurrent] = useState<API.ApprovalSceneVO>();
  const [handlerOptions, setHandlerOptions] = useState<{ label: string; value: string }[]>([]);
  const [form] = Form.useForm<API.ApprovalSceneDTO>();

  // 加载处理器下拉
  useEffect(() => {
    (async () => {
      try {
        const res: any = await listHandlers();
        // RListApprovalHandlerVO 数组包装，取 data
        const list: API.ApprovalHandlerVO[] = Array.isArray(res) ? res : (res?.data ?? []);
        setHandlerOptions(
          list.map((h) => ({
            label: h.displayName ? `${h.beanName}（${h.displayName}）` : (h.beanName ?? ''),
            value: h.beanName ?? '',
          })),
        );
      } catch {
        // 业务失败已在拦截器统一提示，这里静默兜底
      }
    })();
  }, []);

  const openEdit = async (record: API.ApprovalSceneVO) => {
    setCurrent(record);
    try {
      const detail: any = await getScene({ id: record.id! } as any);
      const d = (detail as API.ApprovalSceneVO) || record;
      // 异步详情需手动回填
      form.setFieldsValue({
        ...d,
        timeoutAction: d.timeoutAction || 'NOTIFY',
      });
    } catch {
      form.setFieldsValue(record);
    }
    setModalOpen(true);
  };

  const openCreate = () => {
    setCurrent(undefined);
    form.setFieldsValue({
      platform: 'FEISHU',
      enabled: 1,
      autoSubmitEnabled: 1,
      allowDuplicatePending: 0,
      timeoutHours: 72,
      timeoutAction: 'NOTIFY',
    });
    setModalOpen(true);
  };

  const columns: ProColumns<API.ApprovalSceneVO>[] = [
    { title: '场景编码', dataIndex: 'sceneCode' },
    { title: '场景名称', dataIndex: 'sceneName' },
    { title: '业务类型', dataIndex: 'businessType', hideInSearch: true },
    {
      title: '平台',
      dataIndex: 'platform',
      valueType: 'select',
      valueEnum: {
        FEISHU: { text: '飞书' },
        WEWORK: { text: '企微' },
        DINGTALK: { text: '钉钉' },
      },
    },
    { title: '模板ID', dataIndex: 'templateId', hideInSearch: true, ellipsis: true },
    { title: '处理器', dataIndex: 'handlerBeanName', hideInSearch: true, ellipsis: true },
    {
      title: '启用',
      dataIndex: 'enabled',
      valueType: 'select',
      valueEnum: {
        1: { text: '启用', status: 'Success' },
        0: { text: '停用', status: 'Error' },
      },
      render: (_, r) =>
        r.enabled === 1 ? <Tag color="success">启用</Tag> : <Tag color="error">停用</Tag>,
    },
    {
      title: '自动提交',
      dataIndex: 'autoSubmitEnabled',
      hideInSearch: true,
      render: (_, r) =>
        r.autoSubmitEnabled === 1 ? (
          <Tag color="success">是</Tag>
        ) : (
          <Tag color="warning">否</Tag>
        ),
    },
    {
      title: '重复待审',
      dataIndex: 'allowDuplicatePending',
      hideInSearch: true,
      render: (_, r) =>
        r.allowDuplicatePending === 1 ? (
          <Tag color="warning">允许</Tag>
        ) : (
          <Tag color="default">拦截</Tag>
        ),
    },
    { title: '备注', dataIndex: 'remark', hideInSearch: true, ellipsis: true },
    {
      title: '操作',
      valueType: 'option',
      width: 140,
      render: (_, record) => [
        <a key="edit" onClick={() => openEdit(record)}>
          编辑
        </a>,
        <Popconfirm
          key="del"
          title="确认删除该场景？"
          onConfirm={async () => {
            try {
              await deleteScene({ id: record.id! } as any);
              message.success('删除成功');
              actionRef.current?.reload();
            } catch {
              // 业务失败已在拦截器统一提示
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
      <ProTable<API.ApprovalSceneVO>
        rowKey="id"
        actionRef={actionRef}
        columns={columns}
        search={{ labelWidth: 'auto' }}
        request={async (params) => {
          const { current, pageSize, ...rest } = params;
          try {
            const res: any = await pageScene({
              dto: { page: current, pageSize, ...rest } as API.ApprovalSceneDTO,
            } as any);
            const data: API.PageResultApprovalSceneVO = res || {};
            return { data: data.list || [], total: data.total || 0, success: true };
          } catch {
            return { data: [], total: 0, success: false };
          }
        }}
        toolBarRender={() => [
          <AddButton key="add" perm="system:approval-scene:create" onClick={openCreate}>
            新增场景
          </AddButton>,
        ]}
      />

      <ModalForm
        form={form}
        title={current?.id ? '编辑场景' : '新增场景'}
        width={760}
        open={modalOpen}
        onOpenChange={setModalOpen}
        modalProps={{ destroyOnClose: true }}
        onFinish={async (values) => {
          try {
            // 必填字段保持原值；可选字段空字符串转 undefined，避免覆盖后端已有值
            const payload: API.ApprovalSceneDTO = {
              sceneCode: values.sceneCode,
              sceneName: values.sceneName,
              businessType: values.businessType,
              platform: values.platform,
              templateId: values.templateId,
              handlerBeanName: values.handlerBeanName,
              enabled: values.enabled,
              autoSubmitEnabled: values.autoSubmitEnabled,
              allowDuplicatePending: values.allowDuplicatePending,
              timeoutHours: values.timeoutHours,
              timeoutAction: values.timeoutAction || undefined,
              notifyTemplateCode: values.notifyTemplateCode || undefined,
              remark: values.remark || undefined,
            };
            if (current?.id) {
              await updateScene({ id: current.id } as any, payload);
              message.success('更新成功');
            } else {
              await createScene(payload);
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
        <ProFormGroup>
          <ProFormText
            name="sceneCode"
            label="场景编码"
            width="md"
            placeholder="如 ORDER_APPROVAL"
            rules={[{ required: true, message: '请输入场景编码' }]}
          />
          <ProFormText
            name="sceneName"
            label="场景名称"
            width="md"
            rules={[{ required: true, message: '请输入场景名称' }]}
          />
        </ProFormGroup>
        <ProFormGroup>
          <ProFormText
            name="businessType"
            label="业务类型"
            width="md"
            placeholder="如 ORDER"
            rules={[{ required: true, message: '请输入业务类型' }]}
          />
          <ProFormSelect
            name="platform"
            label="平台"
            width="md"
            options={PLATFORM_OPTIONS}
            rules={[{ required: true, message: '请选择平台' }]}
          />
        </ProFormGroup>
        <ProFormGroup>
          <ProFormText
            name="templateId"
            label="模板ID"
            width="md"
            rules={[{ required: true, message: '请输入模板ID' }]}
          />
          <ProFormSelect
            name="handlerBeanName"
            label="处理器"
            width="md"
            options={handlerOptions}
            showSearch
            rules={[{ required: true, message: '请选择处理器' }]}
          />
        </ProFormGroup>
        <ProFormGroup>
          <ProFormSelect
            name="enabled"
            label="启用状态"
            width="sm"
            options={[
              { label: '启用', value: 1 },
              { label: '停用', value: 0 },
            ]}
          />
          <ProFormSelect
            name="autoSubmitEnabled"
            label="自动提交"
            width="sm"
            options={[
              { label: '是', value: 1 },
              { label: '否', value: 0 },
            ]}
          />
          <ProFormSelect
            name="allowDuplicatePending"
            label="重复待审"
            width="sm"
            options={[
              { label: '拦截', value: 0 },
              { label: '允许', value: 1 },
            ]}
          />
        </ProFormGroup>
        <ProFormGroup>
          <ProFormDigit
            name="timeoutHours"
            label="超时(小时)"
            width="sm"
            min={1}
            max={720}
            fieldProps={{ precision: 0 }}
          />
          <ProFormSelect
            name="timeoutAction"
            label="超时动作"
            width="md"
            options={TIMEOUT_ACTION_OPTIONS}
          />
        </ProFormGroup>
        <ProFormText
          name="notifyTemplateCode"
          label="通知模板编码"
          width="md"
          placeholder="如 APPROVAL_SUBMIT"
        />
        <ProFormTextArea
          name="remark"
          label="备注"
          fieldProps={{ rows: 4 }}
        />
      </ModalForm>
    </>
  );
};

export default SceneList;
