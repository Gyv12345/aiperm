/**
 * 个人中心
 * - 基本信息：ProForm 展示与编辑（/profile/info GET/PUT）。
 * - 修改密码：/profile/password PUT。
 * - 登录日志：ProTable 分页（/profile/logs）。
 */
import {
  getLoginLogs,
  getProfile,
  updatePassword,
  updateProfile,
} from '@/services/aiperm/profile';
import {
  ProCard,
  ProForm,
  ProFormSelect,
  ProFormText,
  ProTable,
} from '@ant-design/pro-components';
import { PageContainer } from '@ant-design/pro-components';
import { Button, Form, message } from 'antd';
import type { ProColumns } from '@ant-design/pro-components';
import React, { useEffect, useRef, useState } from 'react';

const Profile: React.FC = () => {
  const [infoForm] = ProForm.useForm();
  const [pwdForm] = Form.useForm();
  const [profile, setProfile] = useState<API.ProfileVO>({});
  const logActionRef = useRef<any>();

  const loadProfile = async () => {
    try {
      const res = await getProfile();
      setProfile(res || {});
      infoForm.setFieldsValue(res || {});
    } catch {
      // 业务失败已在拦截器统一提示，这里静默兜底避免崩溃
    }
  };

  useEffect(() => {
    loadProfile();
  }, []);

  const logColumns: ProColumns<API.LoginLogVO>[] = [
    { title: 'IP', dataIndex: 'ip', width: 130 },
    { title: '登录地点', dataIndex: 'location' },
    { title: '浏览器', dataIndex: 'browser', hideInSearch: true },
    { title: '操作系统', dataIndex: 'os', hideInSearch: true },
    {
      title: '状态',
      dataIndex: 'status',
      hideInSearch: true,
      render: (_, r) =>
        r.status === 0 ? '成功' : <span style={{ color: '#ff4d4f' }}>失败</span>,
    },
    { title: '消息', dataIndex: 'msg', hideInSearch: true, ellipsis: true },
    { title: '登录时间', dataIndex: 'loginTime', valueType: 'dateTime', hideInSearch: true },
  ];

  return (
    <PageContainer>
      <ProCard title="基本信息" style={{ marginBottom: 16 }}>
        <ProForm
          form={infoForm}
          onFinish={async (values) => {
            await updateProfile(values as API.ProfileDTO);
            message.success('信息已更新');
            loadProfile();
            return true;
          }}
          submitter={{ searchConfig: { submitText: '保存修改' } }}
        >
          <ProFormText name="nickname" label="昵称" />
          <ProFormText name="realName" label="真实姓名" />
          <ProFormText
            name="email"
            label="邮箱"
            rules={[{ type: 'email', message: '邮箱格式不正确' }]}
          />
          <ProFormText name="phone" label="手机号" />
          <ProFormSelect
            name="gender"
            label="性别"
            options={[
              { label: '未知', value: 0 },
              { label: '男', value: 1 },
              { label: '女', value: 2 },
            ]}
          />
        </ProForm>
      </ProCard>

      <ProCard title="修改密码" style={{ marginBottom: 16 }}>
        <Form
          form={pwdForm}
          layout="vertical"
          style={{ maxWidth: 400 }}
          onFinish={async (values) => {
            await updatePassword(values as API.PasswordDTO);
            message.success('密码已修改，请重新登录');
            pwdForm.resetFields();
            return true;
          }}
        >
          <Form.Item
            name="oldPassword"
            label="旧密码"
            rules={[{ required: true, message: '请输入旧密码' }]}
          >
            <ProFormText.Password />
          </Form.Item>
          <Form.Item
            name="newPassword"
            label="新密码"
            rules={[{ required: true, message: '请输入新密码' }]}
          >
            <ProFormText.Password />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">
              确认修改
            </Button>
          </Form.Item>
        </Form>
      </ProCard>

      <ProCard title="登录日志">
        <ProTable<API.LoginLogVO>
          rowKey="id"
          actionRef={logActionRef}
          columns={logColumns}
          search={false}
          request={async (params) => {
            const { current, pageSize } = params;
            try {
              const res = await getLoginLogs({
                pageNum: current,
                pageSize,
              } as any);
              const data: API.PageResultLoginLogVO = (res as any) || {};
              return {
                data: data.list || [],
                total: data.total || 0,
                success: true,
              };
            } catch {
              return { data: [], total: 0, success: false };
            }
          }}
        />
      </ProCard>
    </PageContainer>
  );
};

export default Profile;
