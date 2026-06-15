/**
 * 定时任务管理
 *
 * - 分页查询 /enterprise/job（list6）。
 * - 新增/编辑：ModalForm（cronExpression、beanClass 必填）。
 * - 操作：暂停（pause）、恢复（resume）、立即执行（runOnce）、删除。
 * - status：0=暂停 1=运行。
 */
import { AddButton } from '@/components/AccessButton';
import {
  create10 as createJob,
  delete10 as deleteJob,
  list6 as pageJob,
  pause as pauseJob,
  resume as resumeJob,
  runOnce as runJob,
  update11 as updateJob,
} from '@/services/aiperm/job';
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

const JobList: React.FC = () => {
  const actionRef = useRef<any>();
  const [modalOpen, setModalOpen] = useState(false);
  const [current, setCurrent] = useState<API.JobVO | undefined>();

  const columns: ProColumns<API.JobVO>[] = [
    { title: 'ID', dataIndex: 'id', width: 70, hideInSearch: true },
    { title: '任务名称', dataIndex: 'jobName' },
    { title: '任务分组', dataIndex: 'jobGroup', hideInSearch: true },
    { title: 'Cron 表达式', dataIndex: 'cronExpression', hideInSearch: true },
    { title: '执行类', dataIndex: 'beanClass', hideInSearch: true, ellipsis: true },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: {
        0: { text: '暂停', status: 'Default' },
        1: { text: '运行', status: 'Success' },
      },
      render: (_, r) =>
        r.status === 1 ? <Tag color="success">运行</Tag> : <Tag>暂停</Tag>,
    },
    { title: '备注', dataIndex: 'remark', hideInSearch: true, ellipsis: true },
    {
      title: '操作',
      valueType: 'option',
      width: 280,
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
        record.status === 1 ? (
          <Popconfirm
            key="pause"
            title="确认暂停？"
            onConfirm={async () => {
              await pauseJob({ id: record.id! } as any);
              message.success('已暂停');
              actionRef.current?.reload();
            }}
          >
            <a>暂停</a>
          </Popconfirm>
        ) : (
          <Popconfirm
            key="resume"
            title="确认恢复？"
            onConfirm={async () => {
              await resumeJob({ id: record.id! } as any);
              message.success('已恢复');
              actionRef.current?.reload();
            }}
          >
            <a>恢复</a>
          </Popconfirm>
        ),
        <Popconfirm
          key="run"
          title="确认立即执行一次？"
          onConfirm={async () => {
            await runJob({ id: record.id! } as any);
            message.success('已触发执行');
            actionRef.current?.reload();
          }}
        >
          <a>执行</a>
        </Popconfirm>,
        <Popconfirm
          key="del"
          title="确认删除？"
          onConfirm={async () => {
            await deleteJob({ id: record.id! } as any);
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
      <ProTable<API.JobVO>
        rowKey="id"
        actionRef={actionRef}
        columns={columns}
        search={{ labelWidth: 'auto' }}
        request={async (params) => {
          const { current, pageSize, ...rest } = params;
          try {
            const res = await pageJob({
              dto: { page: current, pageSize, ...rest } as API.JobDTO,
            } as any);
            const data: API.PageResultJobVO = (res as any) || {};
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
            新增任务
          </AddButton>,
        ]}
      />

      <ModalForm
        title={current?.id ? '编辑任务' : '新增任务'}
        open={modalOpen}
        onOpenChange={setModalOpen}
        initialValues={current}
        modalProps={{ destroyOnHidden: true }}
        onFinish={async (values) => {
          if (current?.id) {
            await updateJob({ id: current.id } as any, values as API.JobDTO);
            message.success('更新成功');
          } else {
            await createJob(values as API.JobDTO);
            message.success('创建成功');
          }
          setModalOpen(false);
          actionRef.current?.reload();
          return true;
        }}
      >
        <ProFormText
          name="jobName"
          label="任务名称"
          rules={[{ required: true, message: '请输入任务名称' }]}
        />
        <ProFormText name="jobGroup" label="任务分组" />
        <ProFormText
          name="cronExpression"
          label="Cron 表达式"
          rules={[{ required: true, message: '请输入 Cron 表达式' }]}
          placeholder="如 0 0/5 * * * ?"
        />
        <ProFormText
          name="beanClass"
          label="执行类"
          rules={[{ required: true, message: '请输入执行类全限定名' }]}
        />
        <ProFormSelect
          name="status"
          label="状态"
          options={[
            { label: '运行', value: 1 },
            { label: '暂停', value: 0 },
          ]}
        />
        <ProFormTextArea name="remark" label="备注" />
      </ModalForm>
    </>
  );
};

export default JobList;
