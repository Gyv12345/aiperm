/**
 * 移动端登录页
 *
 * - 账号密码 + 图形验证码。
 * - 调用 /auth/unified-login（loginType=PASSWORD），验证码取 /auth/captcha。
 * - 成功后存 token，整页跳转 redirect 或 /dashboard（触发 getInitialState 重建）。
 *
 * 说明：unifiedLogin / captcha 用 OpenAPI 生成的 services/aiperm/auth（类型同源）。
 */
import { Button, Form, Image, Input, Toast } from 'antd-mobile';
import React, { useEffect, useState } from 'react';
import {
  captcha as fetchCaptcha,
  unifiedLogin,
} from '@/services/aiperm/auth';
import { setToken } from '@/requestErrorConfig';

const Login: React.FC = () => {
  const [form] = Form.useForm();
  const [captchaKey, setCaptchaKey] = useState('');
  const [captchaImg, setCaptchaImg] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const refreshCaptcha = async () => {
    try {
      const res: any = await fetchCaptcha();
      setCaptchaKey(res?.captchaKey ?? '');
      setCaptchaImg(res?.captchaImage ?? '');
    } catch {
      // 静默
    }
  };

  useEffect(() => {
    refreshCaptcha();
  }, []);

  const handleSubmit = async (values: any) => {
    setSubmitting(true);
    try {
      const res: any = await unifiedLogin({
        loginType: 'PASSWORD',
        identifier: values.username,
        credential: values.password,
        imageCaptcha: values.captcha,
        imageCaptchaKey: captchaKey,
      });
      if (!res?.token) {
        throw new Error('登录失败：未返回 token');
      }
      setToken(res.token);
      Toast.show({ content: '登录成功', icon: 'success' });
      const urlParams = new URL(window.location.href).searchParams;
      const redirect = urlParams.get('redirect') || '/dashboard';
      window.location.href = redirect;
    } catch (e: any) {
      const raw = e?.message || '';
      const msg =
        !raw || raw === 'Network Error' || raw.includes('timeout')
          ? '网络异常，请检查后端服务是否启动'
          : raw;
      Toast.show({ content: msg, icon: 'fail' });
      refreshCaptcha();
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        padding: '0 24px',
        background: '#f5f7fa',
      }}
    >
      <div style={{ textAlign: 'center', marginBottom: 32 }}>
        <img src="/logo.svg" alt="logo" style={{ width: 64, height: 64 }} />
        <div style={{ fontSize: 22, fontWeight: 600, marginTop: 12 }}>AIPerm</div>
        <div style={{ color: '#999', fontSize: 13, marginTop: 4 }}>
          权限结构总览 · 移动端
        </div>
      </div>
      <Form
        form={form}
        layout="horizontal"
        onFinish={handleSubmit}
        footer={
          <Button
            block
            color="primary"
            loading={submitting}
            onClick={() => form.submit()}
          >
            登录
          </Button>
        }
      >
        <Form.Item
          name="username"
          label="用户名"
          rules={[{ required: true, message: '请输入用户名' }]}
        >
          <Input placeholder="请输入用户名" clearable />
        </Form.Item>
        <Form.Item
          name="password"
          label="密码"
          rules={[{ required: true, message: '请输入密码' }]}
        >
          <Input type="password" placeholder="请输入密码" clearable />
        </Form.Item>
        <Form.Item
          name="captcha"
          label="验证码"
          rules={[{ required: true, message: '请输入验证码' }]}
          extra={
            captchaImg ? (
              <Image
                src={captchaImg}
                width={80}
                height={32}
                fit="cover"
                onClick={refreshCaptcha}
                style={{ cursor: 'pointer', borderRadius: 4 }}
              />
            ) : null
          }
        >
          <Input placeholder="图形验证码" clearable />
        </Form.Item>
      </Form>
      <div style={{ textAlign: 'center', color: '#bbb', fontSize: 12, marginTop: 24 }}>
        登录即代表同意《AIPerm 用户协议》
      </div>
    </div>
  );
};

export default Login;
