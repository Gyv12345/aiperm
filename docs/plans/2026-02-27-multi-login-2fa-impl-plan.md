# 多方式登录与2FA 实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为 aiperm 系统实现多方式登录（手机号/邮箱验证码）、TOTP 双因素认证（2FA）、第三方 OAuth 登录（企业微信/钉钉/飞书）及对应的系统配置管理功能。

**Architecture:** 后端采用策略模式处理多种登录方式，所有登录类型通过 `LoginStrategyFactory` 路由到对应策略；2FA 通过拦截器检查敏感操作，验证状态存储在 Redis（30分钟有效期）；第三方 OAuth 使用模板方法模式抽象统一接口。

**Tech Stack:** Java 21 + Spring Boot 3.5.11 + Sa-Token + JdbcClient + Redis + Hutool(TOTP内置) + sms4j(短信) + JavaMail(邮件) + Vue 3 + Element Plus

**设计文档:** `docs/plans/2026-02-27-multi-login-2fa-design.md`

---

## 实现阶段索引

| 阶段 | 文件 | 说明 |
|------|------|------|
| Phase 1-2 | [phase1-2-infrastructure-captcha.md](./2026-02-27-phase1-2-infrastructure-captcha.md) | 数据库建表 + 依赖 + 验证码服务 |
| Phase 3 | [phase3-multi-login.md](./2026-02-27-phase3-multi-login.md) | 多方式登录策略 + 前端改造 |
| Phase 4 | [phase4-2fa.md](./2026-02-27-phase4-2fa.md) | TOTP 2FA 绑定/验证/拦截器 |
| Phase 5 | [phase5-oauth.md](./2026-02-27-phase5-oauth.md) | 第三方 OAuth 登录（企微/钉钉/飞书）|
| Phase 6 | [phase6-config-ui.md](./2026-02-27-phase6-config-ui.md) | 系统配置管理界面 |

## 关键架构决策

### 1. 不引入新的 OAuth 库
第三方 OAuth 采用直接 HTTP 调用方式实现，避免引入额外依赖。每个平台实现 `AbstractOAuthProvider` 抽象类。

### 2. TOTP 使用 Hutool 内置实现
`hutool-all` 已包含 `GoogleAuthenticator` 实现，无需引入 `dev.samstevens.totp`：
```java
import cn.hutool.extra.otp.GoogleAuthenticator;
```

### 3. sms4j 短信服务
引入 `org.dromara.sms4j:sms4j-spring-boot-starter:3.3.3`，支持阿里云/腾讯云等多服务商。

### 4. 邮件使用 Spring Boot Starter
引入 `spring-boot-starter-mail`，配置动态从数据库读取。

### 5. 最新 Flyway 版本
当前最新迁移：`V3.7.0`，新建表使用 `V4.0.0`（按功能分文件）。

### 6. 前端 API 命名规范
参照现有 `frontend/src/api/auth.ts` 手写 API（使用 `aiperm-api-generator` 技能规范）。

## 模块目录规划

```
backend/src/main/java/com/devlovecode/aiperm/modules/
├── auth/
│   ├── controller/AuthController.java          # 已有，需扩展
│   ├── service/AuthService.java                # 已有，需扩展
│   ├── strategy/                               # 新增：登录策略
│   │   ├── LoginStrategy.java                  # 接口
│   │   ├── LoginStrategyFactory.java           # 工厂
│   │   ├── PasswordLoginStrategy.java          # 密码登录
│   │   ├── SmsLoginStrategy.java               # 短信登录
│   │   └── EmailLoginStrategy.java             # 邮箱登录
│   ├── dto/request/
│   │   ├── LoginRequest.java                   # 已有
│   │   └── UnifiedLoginRequest.java            # 新增：统一登录请求
│   └── vo/
│       ├── LoginConfigVO.java                  # 新增：登录配置
│       └── ...
├── captcha/                                    # 新增：验证码模块
│   ├── controller/CaptchaController.java
│   ├── service/
│   │   ├── CaptchaService.java                 # 接口
│   │   ├── SmsCaptchaService.java
│   │   └── EmailCaptchaService.java
│   ├── entity/SysCaptchaConfig.java
│   ├── entity/SysCaptchaLog.java
│   ├── repository/CaptchaConfigRepository.java
│   ├── repository/CaptchaLogRepository.java
│   ├── dto/SendCaptchaDTO.java
│   └── enums/CaptchaScene.java
├── mfa/                                        # 新增：2FA 模块
│   ├── controller/MfaController.java
│   ├── service/MfaService.java
│   ├── entity/SysUserMfa.java
│   ├── entity/SysMfaPolicy.java
│   ├── repository/UserMfaRepository.java
│   ├── repository/MfaPolicyRepository.java
│   ├── interceptor/MfaInterceptor.java
│   ├── dto/MfaDTO.java
│   └── vo/MfaVO.java
└── oauth/                                      # 新增：OAuth 模块
    ├── controller/OAuthController.java
    ├── service/OAuthService.java
    ├── provider/
    │   ├── OAuthProvider.java                  # 接口
    │   ├── AbstractOAuthProvider.java          # 抽象基类
    │   ├── WeworkOAuthProvider.java
    │   ├── DingTalkOAuthProvider.java
    │   └── FeishuOAuthProvider.java
    ├── entity/SysUserOauth.java
    ├── entity/SysOauthConfig.java
    ├── repository/UserOauthRepository.java
    ├── repository/OauthConfigRepository.java
    ├── dto/OauthConfigDTO.java
    └── vo/OauthVO.java

frontend/src/
├── api/
│   ├── auth.ts                                 # 已有，需扩展
│   ├── captcha.ts                              # 新增
│   ├── mfa.ts                                  # 新增
│   └── oauth.ts                               # 新增
└── views/
    ├── login/index.vue                         # 已有，需改造
    └── profile/
        ├── Mfa.vue                             # 新增：2FA 绑定页
        └── OauthBindings.vue                  # 新增：第三方绑定
```

## 开发顺序说明

**推荐按阶段顺序执行**，每个阶段可独立部署和测试：
1. Phase 1-2（基础设施 + 验证码）是所有后续阶段的基础
2. Phase 3（多方式登录）依赖 Phase 1-2
3. Phase 4（2FA）可与 Phase 3 并行，但建议顺序执行
4. Phase 5（OAuth）可选，优先级最低
5. Phase 6（配置管理 UI）可在 Phase 2-4 后随时进行
