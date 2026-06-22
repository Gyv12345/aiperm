/**
 * 表单页壳：NavBar（标题 + 返回 + 提交）+ Form（垂直布局）+ 底部固定提交按钮。
 *
 * - onFinish 成功后自动 history.back() 返回。
 * - initialValues 用于编辑页灌数据。
 * - children 是 Form.Item 列表。
 *
 * 用法：
 * <FormPage title="新增用户" onFinish={async (v) => { await create({dto:v}); }}>
 *   <Form.Item name="username" label="用户名"><Input /></Form.Item>
 * </FormPage>
 */
import { Button, Form, NavBar, Toast } from 'antd-mobile';
import { history } from '@umijs/max';
import React, { useState } from 'react';

export type FormPageProps = {
  title: string;
  onFinish: (values: any) => Promise<void>;
  children: React.ReactNode;
  initialValues?: Record<string, any>;
  submitText?: string;
};

export const FormPage: React.FC<FormPageProps> = ({
  title,
  onFinish,
  children,
  initialValues,
  submitText = '提交',
}) => {
  const [form] = Form.useForm();
  const [submitting, setSubmitting] = useState(false);

  const handleFinish = async (values: any) => {
    setSubmitting(true);
    try {
      await onFinish(values);
      Toast.show({ icon: 'success', content: '操作成功' });
      history.back();
    } catch {
      // 业务错误已由请求拦截器 Toast 提示
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <NavBar onBack={() => history.back()}>{title}</NavBar>
      <div style={{ padding: 12 }}>
        <Form
          form={form}
          layout="vertical"
          initialValues={initialValues}
          onFinish={handleFinish}
          footer={
            <Button
              block
              color="primary"
              loading={submitting}
              onClick={() => form.submit()}
              type="submit"
            >
              {submitText}
            </Button>
          }
        >
          {children}
        </Form>
      </div>
    </>
  );
};

export default FormPage;
