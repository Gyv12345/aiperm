/**
 * 全局 MFA（双因素认证）校验弹窗
 *
 * 触发机制：后端在需要二次验证时返回 HTTP 423，
 * requestErrorConfig 的 errorHandler 派发全局 'mfa-required' 事件，
 * 本组件监听后弹出，调用 /mfa/verify 完成校验，成功后关闭。
 *
 * 文档：Sa-Token MFA 拦截器，HTTP 423。
 */
import { verify as mfaVerify } from '@/services/aiperm/mfa';
import { Form, Input, message, Modal } from 'antd';
import React, { useEffect, useState } from 'react';

interface MfaForm {
  code: string;
}

const MfaVerifyModal: React.FC = () => {
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm<MfaForm>();

  useEffect(() => {
    const handler = () => {
      setOpen(true);
      form.resetFields();
    };
    window.addEventListener('mfa-required', handler);
    return () => window.removeEventListener('mfa-required', handler);
  }, [form]);

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);
      await mfaVerify({ code: values.code });
      message.success('验证成功');
      setOpen(false);
      form.resetFields();
    } catch (e: any) {
      // 校验失败或接口错误：保留弹窗
      if (e?.errorFields) return; // 表单校验未通过
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    // MFA 不能轻易关闭，但允许用户取消（后续操作会再次触发）
    setOpen(false);
  };

  return (
    <Modal
      title="双因素认证"
      open={open}
      onOk={handleOk}
      onCancel={handleCancel}
      confirmLoading={loading}
      okText="验证"
      cancelText="取消"
      maskClosable={false}
      destroyOnHidden
    >
      <p style={{ color: '#666', marginBottom: 16 }}>
        该操作需要二次验证，请输入您验证器中的 6 位动态码。
      </p>
      <Form form={form} layout="vertical">
        <Form.Item
          name="code"
          label="动态验证码"
          rules={[
            { required: true, message: '请输入验证码' },
            { len: 6, message: '请输入 6 位验证码' },
          ]}
        >
          <Input.OTP length={6} autoFocus />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default MfaVerifyModal;
