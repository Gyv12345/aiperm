/**
 * 个人中心 - 两步验证（2FA / MFA）管理
 *
 * 对接后端 /mfa 接口（src/services/aiperm/mfa.ts，已由 generate:api 生成）：
 * - status：查询绑定状态（bound/required/verified）
 * - bind/qrcode：获取 TOTP 二维码与密钥
 * - bind/confirm：输入验证码确认绑定
 * - unbind：输入验证码解绑
 *
 * 与全局 MfaVerifyModal（423 拦截用）不同，本组件是「个人中心主动管理」2FA 的入口。
 */
import { confirmBind, qrcode, status, unbind } from '@/services/aiperm/mfa';
import { Button, Divider, Input, Modal, QRCode, Space, Tag, Typography, message } from 'antd';
import React, { useEffect, useState } from 'react';

const { Paragraph, Text } = Typography;

const ProfileMfa: React.FC = () => {
  const [mfaStatus, setMfaStatus] = useState<API.MfaStatusVO>({});
  const [qrcodeData, setQrcodeData] = useState<API.MfaQrcodeVO>({});
  const [bindOpen, setBindOpen] = useState(false);
  const [unbindOpen, setUnbindOpen] = useState(false);
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);

  const refreshStatus = async () => {
    try {
      setMfaStatus(await status());
    } catch {
      // 拦截器已统一提示
    }
  };

  useEffect(() => {
    refreshStatus();
  }, []);

  const startBind = async () => {
    try {
      const data = await qrcode();
      setQrcodeData(data || {});
      setCode('');
      setBindOpen(true);
    } catch {
      // 拦截器已统一提示
    }
  };

  const doBind = async () => {
    if (code.length !== 6) {
      message.warning('请输入 6 位验证码');
      return;
    }
    setLoading(true);
    try {
      await confirmBind({ code });
      message.success('两步验证已启用');
      setBindOpen(false);
      refreshStatus();
    } catch {
      // 拦截器已统一提示
    } finally {
      setLoading(false);
    }
  };

  const doUnbind = async () => {
    if (code.length !== 6) {
      message.warning('请输入 6 位验证码');
      return;
    }
    setLoading(true);
    try {
      await unbind({ code });
      message.success('两步验证已关闭');
      setUnbindOpen(false);
      refreshStatus();
    } catch {
      // 拦截器已统一提示
    } finally {
      setLoading(false);
    }
  };

  const bound = !!mfaStatus.bound;

  return (
    <>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Space>
          <Text strong>两步验证（2FA）</Text>
          {bound ? <Tag color="success">已启用</Tag> : <Tag>未启用</Tag>}
          {mfaStatus.required && !bound && <Tag color="warning">需要启用</Tag>}
        </Space>
        {bound ? (
          <Button
            danger
            onClick={() => {
              setCode('');
              setUnbindOpen(true);
            }}
          >
            关闭两步验证
          </Button>
        ) : (
          <Button type="primary" onClick={startBind}>
            启用两步验证
          </Button>
        )}
      </div>
      <Paragraph type="secondary" style={{ marginTop: 8, marginBottom: 0, fontSize: 13 }}>
        启用后，在登录或敏感操作时需输入验证器中的动态验证码，可显著提升账号安全性。
      </Paragraph>

      <Divider style={{ margin: '12px 0' }} />

      {/* 绑定 Modal：扫码 + 验证码 */}
      <Modal
        title="启用两步验证"
        open={bindOpen}
        onOk={doBind}
        onCancel={() => setBindOpen(false)}
        confirmLoading={loading}
        okText="确认启用"
        cancelText="取消"
        destroyOnHidden
      >
        <div style={{ textAlign: 'center', margin: '8px 0 16px' }}>
          {qrcodeData.totpUri ? (
            <QRCode value={qrcodeData.totpUri} size={160} />
          ) : (
            <Text type="secondary">二维码加载中…</Text>
          )}
        </div>
        <Paragraph type="secondary" style={{ fontSize: 13, marginBottom: 8 }}>
          用验证器（Google Authenticator / 微软身份验证器等）扫描上方二维码；
          若无法扫码，可手动添加密钥：<Text copyable code>{qrcodeData.secretKey}</Text>
        </Paragraph>
        <div style={{ marginBottom: 8 }}>请输入验证器显示的 6 位验证码：</div>
        <Input.OTP length={6} value={code} onChange={(v) => setCode(v)} autoFocus />
      </Modal>

      {/* 解绑 Modal：验证码 */}
      <Modal
        title="关闭两步验证"
        open={unbindOpen}
        onOk={doUnbind}
        onCancel={() => setUnbindOpen(false)}
        confirmLoading={loading}
        okText="确认关闭"
        cancelText="取消"
        destroyOnHidden
      >
        <Paragraph type="secondary">为确认是本人操作，请输入验证器中的 6 位验证码。</Paragraph>
        <Input.OTP length={6} value={code} onChange={(v) => setCode(v)} autoFocus />
      </Modal>
    </>
  );
};

export default ProfileMfa;
