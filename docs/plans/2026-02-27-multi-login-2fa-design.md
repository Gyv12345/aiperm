# 多方式登录与2FA设计文档

> 创建日期：2026-02-27

## 一、需求概述

### 1.1 多方式登录

| 登录方式 | 说明 |
|----------|------|
| 账号密码 | 用户名 + 密码 + 图形验证码（已有） |
| 手机号 | 手机号 + 短信验证码 |
| 邮箱 | 邮箱 + 邮件验证码 |
| 第三方OAuth | 企业微信、钉钉、飞书 |

### 1.2 双因素认证（2FA）

- **方式**：TOTP（Google Authenticator / Microsoft Authenticator）
- **强制策略**：仅超级管理员（userId=1）强制绑定
- **验证时机**：敏感操作时验证，非登录时验证
- **状态管理**：Redis存储验证状态，30分钟有效期
- **策略配置**：数据库配置哪些操作需要2FA验证

### 1.3 第三方登录绑定

- **绑定模式**：主动绑定（用户先账号密码登录，后在个人中心绑定）
- **多平台支持**：一个账号可绑定多个第三方平台
- **开关控制**：企业配置中控制开启哪些第三方登录方式

---

## 二、整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        登录入口 (Tab切换)                        │
├─────────────┬─────────────┬─────────────┬─────────────────────┤
│  账号密码   │   手机号    │    邮箱     │   第三方(企微/钉钉/飞书)  │
└──────┬──────┴──────┬──────┴──────┬──────┴──────────┬──────────┘
       │             │             │                  │
       ▼             ▼             ▼                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                      统一认证服务 (AuthService)                  │
│  - 验证凭证（密码/验证码/OAuth）                                   │
│  - 生成 Token (Sa-Token)                                         │
└─────────────────────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│                      敏感操作拦截器                               │
│  - 检查操作是否需要2FA（查数据库配置）                              │
│  - 需要2FA → 返回特殊状态码 → 前端弹出2FA验证框                     │
└─────────────────────────────────────────────────────────────────┘
```

**核心模块划分：**

| 模块 | 职责 |
|------|------|
| AuthModule | 登录/登出/Token管理 |
| MfaModule | TOTP绑定/验证/管理 |
| OAuthModule | 第三方登录绑定/解绑/回调 |
| NotifyModule | 短信/邮件验证码发送 |
| SystemConfig | 第三方登录开关、2FA策略配置 |

---

## 三、设计模式应用

### 3.1 登录策略模式

**使用策略模式处理多种登录方式，便于后续扩展：**

```
┌─────────────────────────────────────────────────────────────┐
│                 LoginStrategy (接口)                         │
│  - login(credentials): LoginResult                          │
│  - getType(): LoginType                                     │
└─────────────────────────────────────────────────────────────┘
                              △
         ┌────────────────────┼────────────────────┐
         │                    │                    │
┌────────┴────────┐  ┌───────┴───────┐  ┌────────┴────────┐
│ PasswordStrategy│  │ SmsStrategy   │  │ EmailStrategy   │
│  (账号密码登录)   │  │  (手机号登录)  │  │  (邮箱登录)     │
└─────────────────┘  └───────────────┘  └─────────────────┘
                              │
                    ┌─────────┴─────────┐
                    │                   │
            ┌───────┴───────┐   ┌───────┴───────┐
            │ OAuthStrategy │   │  OAuthProvider │
            │  (第三方登录)   │   │    (抽象工厂)   │
            └───────────────┘   └───────┬───────┘
                                        △
                         ┌──────────────┼──────────────┐
                         │              │              │
                  ┌──────┴──────┐┌──────┴──────┐┌──────┴──────┐
                  │ WeworkOAuth ││ DingTalkOAuth││  FeishuOAuth │
                  │ (企业微信)   ││  (钉钉)      ││  (飞书)      │
                  └─────────────┘└─────────────┘└─────────────┘
```

**核心代码结构：**

```java
// 1. 登录策略接口
public interface LoginStrategy {
    LoginResult login(LoginCredentials credentials);
    LoginType getType();
}

// 2. 策略工厂 - 根据类型自动选择策略
@Component
public class LoginStrategyFactory {
    private final Map<LoginType, LoginStrategy> strategies;

    public LoginStrategy getStrategy(LoginType type) {
        return strategies.get(type);
    }
}

// 3. 第三方OAuth抽象 - 模板方法模式
public abstract class AbstractOAuthProvider implements OAuthProvider {
    public final OAuthUserInfo authorize(String code) {
        String accessToken = getAccessToken(code);
        OAuthUserInfo userInfo = getUserInfo(accessToken);
        return userInfo;
    }

    protected abstract String getAccessToken(String code);
    protected abstract OAuthUserInfo getUserInfo(String accessToken);
}
```

### 3.2 验证码策略模式

```java
public interface CaptchaService {
    void send(String target, CaptchaScene scene);
    boolean verify(String target, String code, CaptchaScene scene);
}

@Component
public class SmsCaptchaService implements CaptchaService { ... }

@Component
public class EmailCaptchaService implements CaptchaService { ... }
```

---

## 四、数据库设计

### 4.1 用户2FA绑定表

```sql
CREATE TABLE sys_user_mfa (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    mfa_type VARCHAR(20) NOT NULL DEFAULT 'TOTP' COMMENT 'MFA类型',
    secret_key VARCHAR(100) NOT NULL COMMENT 'TOTP密钥(加密存储)',
    bind_time DATETIME COMMENT '绑定时间',
    status TINYINT DEFAULT 1 COMMENT '状态:1已启用,0已禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_id (user_id)
) COMMENT '用户MFA绑定表';
```

### 4.2 2FA策略配置表

```sql
CREATE TABLE sys_mfa_policy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '策略名称',
    perm_pattern VARCHAR(200) COMMENT '权限标识匹配(支持通配符)',
    api_pattern VARCHAR(200) COMMENT 'API路径匹配(支持通配符)',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '2FA策略配置表';

-- 示例数据
INSERT INTO sys_mfa_policy (name, perm_pattern, api_pattern) VALUES
('修改密码', 'system:user:password', '/system/user/password'),
('删除用户', 'system:user:delete', '/system/user/*'),
('导出数据', '*:export', '*/export');
```

### 4.3 用户第三方账号绑定表

```sql
CREATE TABLE sys_user_oauth (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    platform VARCHAR(20) NOT NULL COMMENT '平台:WEWORK/DINGTALK/FEISHU',
    open_id VARCHAR(100) NOT NULL COMMENT '第三方平台用户标识',
    union_id VARCHAR(100) COMMENT '企业统一标识(可选)',
    nickname VARCHAR(100) COMMENT '第三方昵称',
    avatar VARCHAR(500) COMMENT '第三方头像',
    bind_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    status TINYINT DEFAULT 1 COMMENT '状态:1正常,0已解绑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_platform_openid (platform, open_id),
    INDEX idx_user_id (user_id)
) COMMENT '用户第三方账号绑定表';
```

### 4.4 OAuth配置表

```sql
CREATE TABLE sys_oauth_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    platform VARCHAR(20) NOT NULL COMMENT '平台',
    enabled TINYINT DEFAULT 0 COMMENT '是否启用',
    corp_id VARCHAR(100) COMMENT '企业ID',
    agent_id VARCHAR(100) COMMENT '应用ID',
    app_key VARCHAR(200) COMMENT 'AppKey(加密存储)',
    app_secret VARCHAR(200) COMMENT 'AppSecret(加密存储)',
    callback_url VARCHAR(500) COMMENT '回调地址',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_platform (platform)
) COMMENT 'OAuth配置表';
```

### 4.5 验证码配置表

```sql
CREATE TABLE sys_captcha_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(20) NOT NULL COMMENT '类型:SMS/EMAIL',
    enabled TINYINT DEFAULT 0 COMMENT '是否启用',

    -- 短信配置（sms4j支持多服务商）
    sms_provider VARCHAR(50) COMMENT '短信服务商:ALIYUN/TENCENT',
    sms_access_key VARCHAR(200) COMMENT 'AccessKey(加密)',
    sms_secret_key VARCHAR(200) COMMENT 'SecretKey(加密)',
    sms_sign_name VARCHAR(100) COMMENT '签名名称',
    sms_template_code VARCHAR(100) COMMENT '模板ID',

    -- 邮件配置
    email_host VARCHAR(100) COMMENT 'SMTP服务器',
    email_port INT COMMENT '端口',
    email_username VARCHAR(100) COMMENT '邮箱账号',
    email_password VARCHAR(200) COMMENT '密码/授权码(加密)',
    email_from VARCHAR(100) COMMENT '发件人地址',
    email_from_name VARCHAR(100) COMMENT '发件人名称',

    -- 通用配置
    code_length INT DEFAULT 6 COMMENT '验证码长度',
    expire_minutes INT DEFAULT 5 COMMENT '过期时间(分钟)',
    daily_limit INT DEFAULT 10 COMMENT '每日发送上限',

    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_type (type)
) COMMENT '验证码配置表';
```

### 4.6 验证码发送记录表

```sql
CREATE TABLE sys_captcha_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(20) NOT NULL COMMENT '类型:SMS/EMAIL',
    target VARCHAR(100) NOT NULL COMMENT '目标(手机号/邮箱)',
    code VARCHAR(10) NOT NULL COMMENT '验证码',
    scene VARCHAR(50) COMMENT '场景:LOGIN/BIND/RESET',
    status TINYINT COMMENT '状态:1成功,0失败',
    fail_reason VARCHAR(200) COMMENT '失败原因',
    ip VARCHAR(50) COMMENT '请求IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_target_time (target, create_time)
) COMMENT '验证码发送记录';
```

### 4.7 系统配置扩展

```sql
-- 在 sys_config 表中新增配置项
INSERT INTO sys_config (config_key, config_value, config_type, remark) VALUES
-- 第三方登录开关
('oauth.wework.enabled', '0', 'system', '企业微信登录开关:0关闭,1开启'),
('oauth.dingtalk.enabled', '0', 'system', '钉钉登录开关:0关闭,1开启'),
('oauth.feishu.enabled', '0', 'system', '飞书登录开关:0关闭,1开启'),

-- 登录方式开关
('login.sms.enabled', '0', 'system', '短信验证码登录开关'),
('login.email.enabled', '0', 'system', '邮箱验证码登录开关'),

-- 2FA配置
('mfa.enabled', '1', 'system', '2FA功能总开关'),
('mfa.verify_expire_minutes', '30', 'system', '2FA验证有效期(分钟)'),

-- 短信/邮件功能开关
('notify.sms.enabled', '0', 'system', '短信功能开关'),
('notify.email.enabled', '0', 'system', '邮件功能开关');
```

---

## 五、Redis存储设计

### 5.1 验证码存储

```
Key:   captcha:{type}:{target}:{scene}
Value: 验证码
TTL:   5分钟

示例: captcha:sms:13800138000:login
```

### 5.2 2FA验证状态

```
Key:   mfa:verified:{userId}
Value: {
    "verifiedAt": "2026-02-27T10:30:00",
    "expireAt": "2026-02-27T11:00:00",
    "ip": "192.168.1.100"
}
TTL:   30分钟（可配置）
```

---

## 六、接口设计

### 6.1 认证相关接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/auth/captcha` | GET | 获取图形验证码 |
| `/auth/login` | POST | 账号密码登录 |
| `/auth/login/sms` | POST | 手机号验证码登录 |
| `/auth/login/email` | POST | 邮箱验证码登录 |
| `/auth/login/oauth/{platform}` | GET | 第三方登录跳转 |
| `/auth/login/oauth/callback/{platform}` | GET | 第三方登录回调 |
| `/auth/logout` | POST | 登出 |
| `/auth/config` | GET | 获取登录配置（哪些方式可用） |

### 6.2 验证码接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/captcha/sms` | POST | 发送短信验证码 |
| `/captcha/email` | POST | 发送邮件验证码 |

### 6.3 2FA接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/mfa/status` | GET | 获取2FA绑定状态 |
| `/mfa/bind/qrcode` | GET | 获取绑定二维码 |
| `/mfa/bind/verify` | POST | 确认绑定（验证TOTP码） |
| `/mfa/unbind` | POST | 解绑2FA |
| `/mfa/verify` | POST | 验证2FA（敏感操作时调用） |

### 6.4 第三方账号绑定接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/oauth/bindings` | GET | 获取已绑定的第三方账号列表 |
| `/oauth/bind/{platform}` | GET | 跳转绑定第三方账号 |
| `/oauth/bind/callback/{platform}` | GET | 绑定回调 |
| `/oauth/unbind/{platform}` | DELETE | 解绑第三方账号 |

### 6.5 系统配置接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/system/oauth-config` | GET/PUT | 第三方登录配置管理 |
| `/system/captcha-config/{type}` | GET/PUT | 短信/邮件配置管理 |
| `/system/mfa-policy` | GET/POST/PUT/DELETE | 2FA策略配置管理 |

---

## 七、前端页面设计

### 7.1 登录页面

```
┌─────────────────────────────────────────────────────────────┐
│                        aiperm                                │
│                   RBAC 权限管理系统                           │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │ 账号密码  │ │  手机号  │ │   邮箱   │ │  第三方   │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
│─────────────────────────────────────────────────────────────│
│                                                             │
│   ┌─────────────────────────────────────────────────────┐  │
│   │  用户名 / 手机号 / 邮箱                              │  │
│   └─────────────────────────────────────────────────────┘  │
│   ┌─────────────────────────────────────────────────────┐  │
│   │  密码 / 验证码                          [获取验证码] │  │
│   └─────────────────────────────────────────────────────┘  │
│   ┌─────────────────────────────────────────────────────┐  │
│   │  验证码                            [图形验证码图片]  │  │
│   └─────────────────────────────────────────────────────┘  │
│                                                             │
│   ┌─────────────────────────────────────────────────────┐  │
│   │                      登  录                          │  │
│   └─────────────────────────────────────────────────────┘  │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                    ─────── 第三方登录 ───────                │
│                                                             │
│     [企业微信]        [钉钉]         [飞书]                  │
│     (根据配置显示)    (根据配置显示)   (根据配置显示)          │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**动态加载逻辑：**
1. 页面加载时调用 `/auth/config` 获取登录配置
2. 根据返回配置显示对应的Tab和第三方登录按钮

### 7.2 2FA绑定页面

```
┌─────────────────────────────────────────────┐
│         绑定双因素认证                [×]   │
├─────────────────────────────────────────────┤
│                                             │
│   1. 下载 Google Authenticator 或          │
│      Microsoft Authenticator App            │
│                                             │
│   2. 扫描下方二维码                          │
│      ┌───────────────┐                     │
│      │   [QR CODE]   │                     │
│      └───────────────┘                     │
│                                             │
│   3. 输入App显示的6位验证码                  │
│      ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐
│      └─────┘ └─────┘ └─────┘ └─────┘ └─────┘ └─────┘
│                                             │
│              [取消]        [确认绑定]        │
└─────────────────────────────────────────────┘
```

### 7.3 2FA验证弹窗（敏感操作触发）

```
┌─────────────────────────────────────────────┐
│           安全验证                    [×]   │
├─────────────────────────────────────────────┤
│                                             │
│   此操作需要二次验证，请输入验证码            │
│                                             │
│   ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐
│   │     │ │     │ │     │ │     │ │     │ │     │
│   └─────┘ └─────┘ └─────┘ └─────┘ └─────┘ └─────┘
│                                             │
│              [取消]        [确认]           │
│                                             │
└─────────────────────────────────────────────┘
```

---

## 八、核心业务逻辑

### 8.1 2FA验证流程（拦截器）

```
请求 → 2FA拦截器 → 检查API是否需要2FA →
                              │
                    ┌─────────┴─────────┐
                    │不需要              │需要
                    ▼                   ▼
              放行请求           检查Redis中2FA状态
                                        │
                              ┌─────────┴─────────┐
                              │已验证              │未验证
                              ▼                   ▼
                        放行请求          返回 423 + 需要验证提示
```

### 8.2 超级管理员2FA强制逻辑

```java
private static final Long SUPER_ADMIN_ID = 1L;

// 检查用户是否需要强制绑定2FA
public boolean isRequireMfa(Long userId) {
    return SUPER_ADMIN_ID.equals(userId);
}

// 检查是否允许解绑
public boolean canUnbind(Long userId) {
    return !SUPER_ADMIN_ID.equals(userId);
}
```

### 8.3 验证码发送限流

| 限流维度 | 限制规则 |
|----------|----------|
| 单个手机号/邮箱 | 60秒内只能发1次 |
| 单个IP | 1小时内最多10次 |
| 单个手机号/邮箱 | 每天最多10次 |

---

## 九、技术依赖

| 依赖 | 用途 |
|------|------|
| sms4j | 短信发送（支持阿里云、腾讯云等） |
| JavaMail | 邮件发送 |
| Google Authenticator | TOTP算法实现（可使用 `totp-authenticator` 库） |
| Sa-Token | 认证框架（已有） |
| Redis | 验证码/2FA状态存储（已有） |

---

## 十、开发计划建议

### Phase 1: 基础设施
1. 集成 sms4j 短信服务
2. 集成邮件发送服务
3. 创建数据库表结构

### Phase 2: 验证码服务
1. 实现验证码策略模式
2. 验证码发送/验证接口
3. 限流控制

### Phase 3: 多方式登录
1. 登录策略模式实现
2. 手机号/邮箱登录接口
3. 前端登录页面改造

### Phase 4: 第三方登录
1. OAuth抽象框架
2. 企业微信/钉钉/飞书对接
3. 绑定/解绑功能

### Phase 5: 2FA功能
1. TOTP绑定/验证
2. 2FA策略配置
3. 拦截器实现
4. 前端集成

### Phase 6: 系统配置管理
1. OAuth配置管理页面
2. 短信/邮件配置页面
3. 2FA策略配置页面
