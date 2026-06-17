/**
 * 登录页
 *
 * - 使用 ProComponents 的 LoginForm。
 * - 密码登录：用户名 + 密码 + 图形验证码。
 * - 调用 /auth/unified-login（loginType=PASSWORD）。
 * - 成功后存 token，刷新 initialState，跳转首页或 redirect 参数指向的地址。
 *
 * 说明：认证接口用生成的 services（src/services/aiperm/auth.ts），
 * 但 /auth/info、/auth/menus 的树形菜单类型用手写 services/auth.ts（更贴合业务）。
 */
import { LoginForm, ProFormText } from '@ant-design/pro-components';
import { Alert, message, Space, Tabs } from 'antd';
import React, { useEffect, useState } from 'react';
import {
  LockOutlined,
  SafetyCertificateOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { captcha as fetchCaptcha, unifiedLogin } from '@/services/aiperm/auth';
import { setToken } from '@/requestErrorConfig';

const LoginMessage: React.FC<{ content: string }> = ({ content }) => (
  <Alert
    type="error"
    showIcon
    message={content}
    style={{ marginBottom: 24 }}
  />
);

const Login: React.FC = () => {
  const [loginType, setLoginType] = useState<API.UnifiedLoginDTO['loginType']>('PASSWORD');
  const [errorMsg, setErrorMsg] = useState<string>('');
  const [captchaKey, setCaptchaKey] = useState<string>('');
  const [captchaImg, setCaptchaImg] = useState<string>('');

  // 拉取图形验证码
  const refreshCaptcha = async () => {
    try {
      const res = await fetchCaptcha();
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
    try {
      setErrorMsg('');
      const res = await unifiedLogin({
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
      message.success('登录成功');

      // 整页跳转：触发 getInitialState 重新执行（拉取用户信息 + 菜单树），
      // 比 setInitialState 局部更新更可靠地重建侧边栏与权限。
      const urlParams = new URL(window.location.href).searchParams;
      const redirect = urlParams.get('redirect') || '/welcome';
      window.location.href = redirect;
      return;
    } catch (e: any) {
      // 业务错误（拦截器已 message.error 提示）取原始消息；网络错误显示友好提示
      const raw = e?.message || '';
      const msg = !raw || raw === 'Network Error' || raw.includes('timeout')
        ? '网络异常，请检查后端服务是否启动'
        : raw;
      setErrorMsg(msg);
      refreshCaptcha();
    }
  };

  return (
    <LoginForm
      contentStyle={{ minWidth: 280, maxWidth: '75vw' }}
      logo="/logo.svg"
      title="AIPerm"
      subTitle="权限结构总览"
      onFinish={handleSubmit}
    >
      {errorMsg && <LoginMessage content={errorMsg} />}
      <Tabs
        activeKey={loginType}
        onChange={(k) => setLoginType(k as any)}
        centered
        items={[
          { key: 'PASSWORD', label: '账户密码登录' },
        ]}
      />
      {loginType === 'PASSWORD' && (
        <>
          <ProFormText
            name="username"
            fieldProps={{
              size: 'large',
              prefix: <UserOutlined />,
            }}
            placeholder="用户名"
            rules={[{ required: true, message: '请输入用户名' }]}
          />
          <ProFormText.Password
            name="password"
            fieldProps={{
              size: 'large',
              prefix: <LockOutlined />,
            }}
            placeholder="密码"
            rules={[{ required: true, message: '请输入密码' }]}
          />
          <ProFormText
            name="captcha"
            fieldProps={{
              size: 'large',
              prefix: <SafetyCertificateOutlined />,
              suffix: captchaImg ? (
                <img
                  src={captchaImg}
                  alt="验证码"
                  title="点击刷新"
                  onClick={refreshCaptcha}
                  style={{ height: 32, cursor: 'pointer', borderRadius: 4 }}
                />
              ) : null,
            }}
            placeholder="图形验证码"
            rules={[{ required: true, message: '请输入验证码' }]}
          />
        </>
      )}
      <div
        style={{
          marginBottom: 24,
          textAlign: 'center',
          color: 'rgba(0,0,0,0.45)',
          fontSize: 12,
        }}
      >
        <Space>
          登录即代表同意《AIPerm 用户协议》
        </Space>
      </div>
    </LoginForm>
  );
};

export default Login;
