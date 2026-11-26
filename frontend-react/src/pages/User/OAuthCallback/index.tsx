/**
 * 第三方登录/绑定 回调中转页
 *
 * 后端 /auth/social/callback 处理完 OAuth 后，302 重定向到本页并携带 result 参数：
 * - result=login_success&token=xxx  → setToken → 跳首页
 * - result=unbound&platform=X       → 提示"未绑定，请先登录后在个人中心绑定" → 跳登录页
 * - result=bind_success             → 跳个人中心「账号绑定」
 * - result=error&msg=               → 错误提示 → 跳登录页
 */
import { Spin } from 'antd';
import { history } from '@umijs/max';
import React, { useEffect, useState } from 'react';
import { setToken } from '@/requestErrorConfig';

const PLATFORM_LABEL: Record<string, string> = {
  GITHUB: 'GitHub',
  GITEE: 'Gitee',
};

const OAuthCallback: React.FC = () => {
  const [tip, setTip] = useState('正在处理第三方登录，请稍候...');

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const result = params.get('result');
    const platform = params.get('platform') || '';
    const token = params.get('token');
    const msg = params.get('msg');

    const platformLabel = PLATFORM_LABEL[platform] || platform;

    switch (result) {
      case 'login_success':
        if (token) {
          setToken(token);
        }
        setTip('登录成功，正在进入系统...');
        // 整页跳转：触发 getInitialState 重新拉取用户信息与菜单
        window.location.href = '/welcome';
        break;
      case 'unbound':
        import('antd').then(({ message }) => {
          message.warning(
            `该${platformLabel}账号未绑定系统账号，请先用账号密码登录，再在「个人中心-账号绑定」完成绑定`,
          );
          history.replace('/user/login');
        });
        break;
      case 'bind_success':
        import('antd').then(({ message }) => {
          message.success(`${platformLabel} 账号绑定成功`);
          // 整页刷新：确保 Profile 重新拉取绑定列表，并按 ?tab= 定位到「账号绑定」
          window.location.href = '/profile?tab=binding';
        });
        break;
      default:
        import('antd').then(({ message }) => {
          const text = msg ? decodeURIComponent(msg) : '第三方授权失败，请重试';
          message.error(text);
          history.replace('/user/login');
        });
    }
  }, []);

  return (
    <div
      style={{
        height: '100vh',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        gap: 16,
      }}
    >
      <Spin size="large" />
      <span style={{ color: 'rgba(0,0,0,0.45)', fontSize: 14 }}>{tip}</span>
    </div>
  );
};

export default OAuthCallback;
