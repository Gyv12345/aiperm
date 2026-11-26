/**
 * 个人中心
 *
 * 重构说明（借鉴现代账户中心视觉，数据对接 RBAC 真实字段）：
 * - 顶部用户卡：头像 + 昵称/@用户名 + 角色标签 + 部门/岗位
 *   + 关键统计（拥有角色 / 拥有权限 / 账号状态）
 *   + 账号元信息（上次登录时间/IP、创建时间）。
 * - 内容 Tab：基本信息（只读账号信息 + 可编辑资料 ProForm）
 *   / 安全设置（修改密码）/ 登录日志。
 * - 数据：getProfile(/profile/info) + initialState(/auth/info 的 roles/permissions)。
 *
 * 设计语言与 Dashboard 对齐：Flat、主色 #0060a9、ProComponents 卡片化。
 */
import {
  getLoginLogs,
  getProfile,
  updatePassword,
  updateProfile,
} from '@/services/aiperm/profile';
import {
  getSocialBindings,
  socialAuthorize,
  unbindSocial,
  type SocialBindingVO,
} from '@/services/social';
import ProfileMfa from '@/components/ProfileMfa';
import {
  PageContainer,
  ProCard,
  ProForm,
  ProFormSelect,
  ProFormText,
  ProTable,
  StatisticCard,
} from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-components';
import { ClockCircleOutlined, IdcardOutlined, LinkOutlined, LockOutlined } from '@ant-design/icons';
import { useModel } from '@umijs/max';
import {
  Avatar,
  Button,
  Col,
  Descriptions,
  Divider,
  List,
  Popconfirm,
  Row,
  Space,
  Tag,
  Typography,
  message,
} from 'antd';
import React, { useEffect, useRef, useState } from 'react';

const { Title, Text } = Typography;

/** 时间字符串截短到「年-月-日 时:分」，空值返回占位 */
const fmtTime = (t?: string): string => {
  if (!t) return '—';
  return t.replace('T', ' ').substring(0, 16);
};

const Profile: React.FC = () => {
  const { initialState } = useModel('@@initialState');
  const currentUser = initialState?.currentUser;

  const [infoForm] = ProForm.useForm();
  const [pwdForm] = ProForm.useForm();
  const [profile, setProfile] = useState<API.ProfileVO>({});
  const logActionRef = useRef<any>();

  // 第三方账号绑定
  const SUPPORTED_PLATFORMS = [
    { source: 'GITHUB', label: 'GitHub' },
    { source: 'GITEE', label: 'Gitee' },
  ];
  const [bindings, setBindings] = useState<SocialBindingVO[]>([]);
  const [bindingLoading, setBindingLoading] = useState(false);

  // Tab 受控：支持从 URL ?tab= 定位（如绑定成功后跳回并自动选中）
  const [activeTab, setActiveTab] = useState<string>(() => {
    if (typeof window === 'undefined') return 'info';
    return new URLSearchParams(window.location.search).get('tab') || 'info';
  });

  const loadBindings = async () => {
    setBindingLoading(true);
    try {
      const res = await getSocialBindings();
      setBindings(res || []);
    } catch {
      // 拦截器已统一提示
    } finally {
      setBindingLoading(false);
    }
  };

  const handleBind = async (platform: string) => {
    try {
      const res = await socialAuthorize(platform, 'bind');
      if (res?.authorizeUrl) {
        window.location.href = res.authorizeUrl;
      }
    } catch {
      // 拦截器已统一提示
    }
  };

  const handleUnbind = async (platform: string) => {
    try {
      await unbindSocial(platform);
      message.success('已解绑');
      loadBindings();
    } catch {
      // 拦截器已统一提示
    }
  };

  // 角色展示：优先用 profile.roleNames，兜底 initialState.roles
  const roleNames = profile.roleNames ?? [];
  const roles = currentUser?.roles ?? [];
  const isSuperAdmin = roles.includes('admin');
  const roleCount = roleNames.length || roles.length;
  const permCount = currentUser?.permissions?.length ?? 0;

  const displayName = profile.nickname || profile.username || '用户';
  const firstChar = displayName.charAt(0).toUpperCase();
  const statusNormal = profile.status !== 1; // 0=正常，1=停用

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
    loadBindings();
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
    {
      title: '登录时间',
      dataIndex: 'loginTime',
      valueType: 'dateTime',
      hideInSearch: true,
    },
  ];

  return (
    <PageContainer>
      {/* ===================== 顶部用户卡 ===================== */}
      <ProCard bordered style={{ marginBottom: 16 }} bodyStyle={{ padding: 24 }}>
        {/* 身份区 */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 20, flexWrap: 'wrap' }}>
          {profile.avatar ? (
            <Avatar size={88} src={profile.avatar} />
          ) : (
            <Avatar size={88} style={{ backgroundColor: '#0060a9', fontSize: 32 }}>
              {firstChar}
            </Avatar>
          )}
          <div style={{ minWidth: 0 }}>
            <Space align="center" size={8} wrap>
              <Title level={4} style={{ margin: 0 }}>
                {displayName}
              </Title>
              {profile.username && <Text type="secondary">@{profile.username}</Text>}
            </Space>
            <div style={{ marginTop: 10 }}>
              <Space size={[4, 4]} wrap>
                {isSuperAdmin && <Tag color="gold">超级管理员</Tag>}
                {roleNames
                  .filter((r) => r !== 'admin')
                  .map((r) => (
                    <Tag color="blue" key={r}>
                      {r}
                    </Tag>
                  ))}
                {!roleNames.length && !isSuperAdmin && <Tag>暂无角色</Tag>}
              </Space>
            </div>
            <div style={{ marginTop: 10, color: 'rgba(0,0,0,0.45)' }}>
              <Space size={16} wrap>
                {profile.deptName && (
                  <span>
                    <IdcardOutlined style={{ marginRight: 6 }} />
                    {profile.deptName}
                  </span>
                )}
                {profile.postName && <span>{profile.postName}</span>}
              </Space>
            </div>
          </div>
        </div>

        {/* 统计区 */}
        <Row gutter={16} style={{ marginTop: 24 }}>
          <Col xs={24} sm={8}>
            <StatisticCard
              statistic={{ title: '拥有角色', value: roleCount, suffix: '个' }}
            />
          </Col>
          <Col xs={24} sm={8}>
            <StatisticCard
              statistic={{ title: '拥有权限', value: permCount, suffix: '项' }}
            />
          </Col>
          <Col xs={24} sm={8}>
            <StatisticCard
              statistic={{
                title: '账号状态',
                value: statusNormal ? '正常' : '停用',
                valueStyle: statusNormal ? undefined : { color: '#ff4d4f' },
              }}
            />
          </Col>
        </Row>

        <Divider style={{ margin: '16px 0' }} />

        {/* 账号元信息 */}
        <Descriptions column={{ xs: 1, sm: 2, md: 3 }} size="small">
          <Descriptions.Item label="上次登录时间">
            {fmtTime(profile.lastLoginTime)}
          </Descriptions.Item>
          <Descriptions.Item label="上次登录IP">
            {profile.lastLoginIp || '—'}
          </Descriptions.Item>
          <Descriptions.Item label="账号创建时间">
            {fmtTime(profile.createTime)}
          </Descriptions.Item>
        </Descriptions>
      </ProCard>

      {/* ===================== 内容 Tab ===================== */}
      <ProCard
        tabs={{
          activeKey: activeTab,
          onChange: (key) => setActiveTab(key),
          items: [
            {
              key: 'info',
              label: (
                <span>
                  <IdcardOutlined /> 基本信息
                </span>
              ),
              children: (
                <>
                  {/* 只读账号信息（系统字段，不可编辑） */}
                  <Descriptions
                    title="账号信息"
                    column={{ xs: 1, sm: 3 }}
                    size="small"
                  >
                    <Descriptions.Item label="用户名">
                      {profile.username || '—'}
                    </Descriptions.Item>
                    <Descriptions.Item label="部门">
                      {profile.deptName || '—'}
                    </Descriptions.Item>
                    <Descriptions.Item label="岗位">
                      {profile.postName || '—'}
                    </Descriptions.Item>
                  </Descriptions>
                  <Divider style={{ margin: '12px 0 24px' }} />

                  {/* 可编辑资料 */}
                  <ProForm
                    form={infoForm}
                    style={{ maxWidth: 600 }}
                    onFinish={async (values) => {
                      await updateProfile(values as API.ProfileDTO);
                      message.success('信息已更新');
                      loadProfile();
                      return true;
                    }}
                    submitter={{ searchConfig: { submitText: '保存修改' } }}
                  >
                    <ProFormText name="nickname" label="昵称" placeholder="请输入昵称" />
                    <ProFormText
                      name="realName"
                      label="真实姓名"
                      placeholder="请输入真实姓名"
                    />
                    <ProFormText
                      name="email"
                      label="邮箱"
                      placeholder="请输入邮箱"
                      rules={[{ type: 'email', message: '邮箱格式不正确' }]}
                    />
                    <ProFormText name="phone" label="手机号" placeholder="请输入手机号" />
                    <ProFormSelect
                      name="gender"
                      label="性别"
                      placeholder="请选择性别"
                      options={[
                        { label: '未知', value: 0 },
                        { label: '男', value: 1 },
                        { label: '女', value: 2 },
                      ]}
                    />
                  </ProForm>
                </>
              ),
            },
            {
              key: 'security',
              label: (
                <span>
                  <LockOutlined /> 安全设置
                </span>
              ),
              children: (
                <>
                <ProForm
                  form={pwdForm}
                  style={{ maxWidth: 420 }}
                  onFinish={async (values) => {
                    await updatePassword(values as API.PasswordDTO);
                    message.success('密码已修改，请重新登录');
                    pwdForm.resetFields();
                    return true;
                  }}
                  submitter={{ searchConfig: { submitText: '确认修改' } }}
                >
                  <ProFormText.Password
                    name="oldPassword"
                    label="旧密码"
                    placeholder="请输入旧密码"
                    rules={[{ required: true, message: '请输入旧密码' }]}
                  />
                  <ProFormText.Password
                    name="newPassword"
                    label="新密码"
                    placeholder="请输入新密码"
                    rules={[{ required: true, message: '请输入新密码' }]}
                  />
                </ProForm>
                <Divider style={{ margin: '24px 0 16px' }}>两步验证</Divider>
                <ProfileMfa />
                </>
              ),
            },
            {
              key: 'logs',
              label: (
                <span>
                  <ClockCircleOutlined /> 登录日志
                </span>
              ),
              children: (
                <ProTable<API.LoginLogVO>
                  rowKey="id"
                  actionRef={logActionRef}
                  columns={logColumns}
                  search={false}
                  options={false}
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
              ),
            },
            {
              key: 'binding',
              label: (
                <span>
                  <LinkOutlined /> 账号绑定
                </span>
              ),
              children: (
                <List
                  rowKey="source"
                  loading={bindingLoading}
                  dataSource={SUPPORTED_PLATFORMS}
                  renderItem={(item) => {
                    const bound = bindings.find((b) => b.source === item.source);
                    return (
                      <List.Item
                        actions={[
                          bound ? (
                            <Popconfirm
                              key="unbind"
                              title={`确定解绑 ${item.label} 账号？`}
                              onConfirm={() => handleUnbind(item.source)}
                            >
                              <Button danger size="small">
                                解绑
                              </Button>
                            </Popconfirm>
                          ) : (
                            <Button
                              key="bind"
                              type="primary"
                              size="small"
                              onClick={() => handleBind(item.source)}
                            >
                              绑定
                            </Button>
                          ),
                        ]}
                      >
                        <List.Item.Meta
                          avatar={
                            bound?.avatar ? (
                              <Avatar size={40} src={bound.avatar} />
                            ) : (
                              <Avatar size={40} style={{ backgroundColor: '#0060a9' }}>
                                {item.label.charAt(0)}
                              </Avatar>
                            )
                          }
                          title={item.label}
                          description={
                            bound
                              ? `已绑定${bound.nickname ? `：${bound.nickname}` : ''}${
                                  bound.email ? `（${bound.email}）` : ''
                                }`
                              : '未绑定'
                          }
                        />
                      </List.Item>
                    );
                  }}
                />
              ),
            },
          ],
        }}
      />
    </PageContainer>
  );
};

export default Profile;
