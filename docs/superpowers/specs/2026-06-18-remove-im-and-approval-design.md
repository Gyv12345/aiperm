# 设计文档：去除第三方 IM 对接与审批功能

- **日期**：2026-06-18
- **状态**：待复核
- **关联**：取代 `docs/plans/2026-03-02-im-approval-integration-design.md` 等三份历史设计（后者保留作历史记录）

---

## 1. 背景与动机

aiperm 项目中存在一整套与第三方 IM（企业微信 / 钉钉 / 飞书）对接的功能，分散在两个后端模块：

- `modules/approval` —— IM 审批桥接模块（约 37 个 Java 文件）
- `modules/auth/oauth` —— 第三方 OAuth 登录 / 绑定模块（约 14 个 Java 文件）

经代码与数据核实，这些功能均为**未接入的半成品**：

| 事实 | 说明 |
|------|------|
| 三个 IM 审批适配器全是模拟实现 | `WeworkApprovalAdapter` / `DingtalkApprovalAdapter` / `FeishuApprovalAdapter` 均继承 `BaseSimulationApprovalAdapter`，需 `simulationMode=true` 才能运行，未集成任何真实 SDK |
| OAuth 仅企微半成品 | `WeworkOAuthProvider.fetchUserInfo` 抛 `UnsupportedOperationException`；钉钉 / 飞书无 Provider 实现 |
| 无业务模块实际使用 | 全仓库无 `ApprovalHandler` 实现类，无任何模块注入 `ApprovalClient` |

结论：这是一套"框架搭建完成、等待业务接入"的基础设施，从未真正投用。为保持系统精简、降低维护负担，决定**彻底移除**这两个模块及其所有连带代码、数据库表、菜单与前端实现。

## 2. 目标与非目标

### 目标

1. 删除 `modules/approval` 与 `modules/auth/oauth` 两个后端模块及所有连带引用
2. 新建 Flyway `V4.13.0` 迁移，删除 8 张表、15 条菜单、4 个配置项
3. 删除 Vue3 与 React 两个前端的相关页面、API、路由、类型定义
4. 不破坏既有功能：登录、监控中心、站内消息、2FA、核心 RBAC

### 非目标（明确保留）

- `modules/enterprise` 站内消息（`sys_message`）与公告（`sys_notice`）
- `auth/core` 的多端登录、2FA、验证码（`sys_mfa_policy` / `sys_captcha_*`）
- 全部历史 Flyway 脚本（`V3.1.0` ~ `V4.12.0`，不改 checksum）
- `docs/plans/` 下三份历史设计文档（保留作历史）

## 3. 现状摸底结论

| 维度 | 数量 / 说明 |
|------|-------------|
| 后端整删文件 | approval ~37 + auth/oauth ~14 |
| 后端连带清理文件 | 4 个（LoginConfigVO / AuthService / WebMvcConfig / SaTokenConfig） |
| 删除数据库表 | 8 张（审批 6 + OAuth 2） |
| 删除菜单 | 15 条（按 id） |
| 删除配置项 | 4 个（`oauth.*.enabled` ×3 + `approval.module.enabled`） |
| Vue3 删除 | 3 目录 + 4 API 文件 + 2 文件局部清理 |
| React 删除 | 2 目录 + 7 service 文件 + 3 文件局部清理 |
| 第三方流程引擎 | 无（未引入 Activiti / Flowable / Camunda） |
| 外部调用方 | 无（除两个模块自身外，全局 0 引用） |
| 前端 OAuth UI 入口 | 无（两个登录页均无第三方登录按钮） |

**硬约束**：菜单 id `40 / 41 / 42 / 411 / 412` 已被 `V4.8.0` 覆写为"监控中心"，**绝不可删**。

**安全约束**：菜单只能按 id 删除，**禁止用 `path` / `permission` 作条件**（`V4.8.0` 后这些 path 已变更为 monitor 值）。

## 4. 设计

### 4.1 删除清单与边界

#### 4.1.1 后端 · 整模块删除（删目录）

| 模块 | 路径 | 文件数 |
|------|------|--------|
| IM 审批桥接 | `modules/approval/` | ~37 |
| 第三方 OAuth 登录 | `modules/auth/oauth/` | ~14 |

#### 4.1.2 后端 · 局部清理（改文件）

| 文件 | 清理内容 | 保留内容 |
|------|----------|----------|
| `auth/core/vo/LoginConfigVO.java` | 删 `oauthConfigs` 字段 + `OAuthConfig` 内部类 + 未用的 `import java.util.List` | password / sms / email 三开关 |
| `auth/core/service/AuthService.java` | `getLoginConfig()` 中读取 `oauth.*.enabled` 的三段逻辑 + `.oauthConfigs()`；删未用的 `import ArrayList` | sms / email 开关读取 |
| `config/WebMvcConfig.java` | 拦截器 `excludePathPatterns` 移除 `/oauth/login/**`、`/approval/callback/**`；更新 RestTemplate Bean 注释（Bean 本身按 Step 1 核查结果决定去留） | 其余排除路径 |
| `config/SaTokenConfig.java` | 移除 `/oauth/login/**`、`/approval/callback/**` 两行 | 其余登录放行路径 |

#### 4.1.3 数据库 · 新建 `V4.13.0__remove_im_approval_oauth.sql`

| 操作 | 对象 |
|------|------|
| DROP TABLE | `sys_im_config`、`sys_approval_scene`、`sys_approval_instance`、`sys_approval_callback_log`、`sys_message_template`、`sys_message_log`（审批 6）+ `sys_user_oauth`、`sys_oauth_config`（OAuth 2）= **8 张** |
| DELETE sys_menu | 按 id：`18,181,182,19,191,192,193,194,25,251,252,253,254,26,261`（**绝不碰 40/41/42/411/412**） |
| DELETE sys_role_menu | 同上 id 列表（清角色-菜单关联） |
| DELETE sys_config | `oauth.wework.enabled`、`oauth.dingtalk.enabled`、`oauth.feishu.enabled`、`approval.module.enabled` |

#### 4.1.4 前端 Vue3（`frontend/`）

| 类型 | 对象 |
|------|------|
| 删目录 | `views/approval/`（todo+my）、`views/system/im-config/`、`views/system/approval-scene/` |
| 删文件 | `api/oauth.ts`、`api/system/oauthConfig.ts`、`api/approval.ts`、`api/system/imConfig.ts` |
| 局部清理 | `api/auth.ts`（删 `'OAUTH'` loginType、`oauthConfigs` 字段、`OAuthConfig` 接口）；`views/login/index.vue`（删 `oauthConfigs: []` 初始值） |

#### 4.1.5 前端 React（`frontend-react/`）

| 类型 | 对象 |
|------|------|
| 删目录 | `pages/System/ImConfig/`、`pages/System/ApprovalScene/` |
| 删 services | `oauth.ts`、`oauthConfig.ts`、`imConfig.ts`、`approvalScene.ts`、`approvalInstance.ts`、`approvalCallback.ts`、`approvalTodo.ts` |
| 局部清理 | `services/aiperm/index.ts`（移除 re-export）、`services/aiperm/typings.d.ts`（删 `OAuthConfig` 类型）、`config/routes.ts`（删 IM 配置 + 审批场景路由） |

#### 4.1.6 保留边界（不动）

- `modules/enterprise`（`sys_message` + `sys_notice`，经核实完全不引用 `sys_message_template` / `sys_message_log`）
- `auth/core` 多端登录 / 2FA / 验证码
- 全部历史 Flyway 脚本
- `docs/plans/` 三份历史设计文档

#### 4.1.7 待确认项（实现第 1 步先排查）

| 项 | 排查方式 | 处理 |
|----|----------|------|
| Vue3 `views/enterprise/message-template/`、`views/enterprise/message-log/`、`views/system/oauth-config/` 是否存在 | 找文件 | 存在则删 |
| React `pages/Approval/`、`pages/System/OauthConfig/` 是否存在 | 找文件 | 存在则删 |
| `sys_menu` 是否还有 OAuth 配置菜单（`system/oauth-config`）遗漏 | 全脚本 grep `oauth-config` | 找到则把 id 补入迁移脚本 |
| `RestTemplate` Bean 是否被别处使用 | grep `RestTemplate` | 决定只改注释或删 Bean |

### 4.2 执行顺序与依赖处理

方案 A：一次性彻底删除，内部按依赖有序推进，每步设验证 gate。

| Step | 动作 | Gate |
|------|------|------|
| 1 | 排查 4.1.7 待确认项 | 文件清单确认 |
| 2 | 后端整删 `modules/approval/` + `modules/auth/oauth/`（**两模块同一步删**） | — |
| 3 | 后端局部清理 4 个文件 | — |
| 4 | 后端编译 `./gradlew compileJava` | **0 错误才继续** |
| 5 | 新建 Flyway `V4.13.0` | 迁移可执行、checksum 一致 |
| 6 | Vue3 删除 + 局部清理 | `pnpm run build` 通过 |
| 7 | React 删除 + 局部清理 + 重新生成 API | `pnpm run build` 通过 |
| 8 | 端到端验证（宿主启动 + Swagger + 回归） | 全绿 |

**关键依赖**：

- **approval ↔ oauth 必须同 Step 删除**：`ApprovalBridgeService` 注入 `UserOauthRepository`（来自 oauth 模块），先删任一个都会令另一个编译断裂。Step 2-3-4 构成不可分割的"后端清理单元"。
- **后端编译是硬 gate**：Step 4 不通过绝不进入数据库 / 前端步骤。
- **数据库 DROP 顺序**：8 张表预期无 DB 级外键约束；脚本按逻辑从属顺序 DROP 并用 `IF EXISTS`，若实际存在 FK 则用 `SET FOREIGN_KEY_CHECKS=0/1` 包裹。

**React services 处理**（推荐路径）：后端删除并启动 → `pnpm run generate:api` → 7 个 service 文件 + `typings.d.ts` 的 `OAuthConfig` 自动消失 → 手动清理 `services/aiperm/index.ts` re-export、`config/routes.ts`、`pages/System/*` 目录 → 复查残留 → `pnpm run build`。备选（后端无法启动）：手动删 7 个 service 文件 + 手清 index / typings / routes / pages。

### 4.3 迁移脚本设计

文件：`backend/src/main/resources/db/migration/V4.13.0__remove_im_approval_oauth.sql`

```sql
-- =====================================================================
-- V4.13.0  移除第三方 IM 对接（企微/钉钉/飞书）与审批桥接模块
-- 配合后端删除 modules/approval 与 modules/auth/oauth 两个模块
-- =====================================================================

-- ① 物理删除 8 张表（按逻辑从属顺序；均无 DB 级外键约束）
--   审批 / IM 推送体系
DROP TABLE IF EXISTS `sys_message_log`;
DROP TABLE IF EXISTS `sys_message_template`;
DROP TABLE IF EXISTS `sys_approval_callback_log`;
DROP TABLE IF EXISTS `sys_approval_instance`;
DROP TABLE IF EXISTS `sys_approval_scene`;
DROP TABLE IF EXISTS `sys_im_config`;
--   第三方 OAuth 登录体系
DROP TABLE IF EXISTS `sys_user_oauth`;
DROP TABLE IF EXISTS `sys_oauth_config`;

-- ② 删除审批 / IM 相关菜单（硬删：与 DROP 表保持一致）
--   ⚠️ 仅按以下 id 删除！id 40/41/42/411/412 已被 V4.8.0 覆写为"监控中心"，
--      绝不可删；禁止用 path / permission 作删除条件
DELETE FROM `sys_menu`
WHERE `id` IN (18,181,182,19,191,192,193,194,25,251,252,253,254,26,261);

-- ③ 清理角色-菜单关联
DELETE FROM `sys_role_menu`
WHERE `menu_id` IN (18,181,182,19,191,192,193,194,25,251,252,253,254,26,261);

-- ④ 删除 sys_config 中相关开关
DELETE FROM `sys_config`
WHERE `config_key` IN (
    'oauth.wework.enabled',
    'oauth.dingtalk.enabled',
    'oauth.feishu.enabled',
    'approval.module.enabled'
);
--   （若 Step 1 排查发现 OAuth 配置菜单 id，在此补一条 DELETE sys_menu）
```

| 决策 | 选择 | 理由 |
|------|------|------|
| 菜单删除方式 | 硬删 `DELETE` | 表已物理 DROP，软删菜单记录无意义 |
| 表删除 | `DROP TABLE IF EXISTS` | 幂等、彻底、无其他模块引用 |
| 菜单定位 | 精确 id 列表 | V4.8.0 已改写 path，只能靠 id |
| 配置定位 | 精确 `config_key IN (...)` | 比 `LIKE` 更安全，不误伤新增项 |

### 4.4 前端删除细节

#### Vue3（`frontend/`）

- **整删**：`views/approval/`、`views/system/im-config/`、`views/system/approval-scene/`、`api/oauth.ts`、`api/system/oauthConfig.ts`、`api/approval.ts`、`api/system/imConfig.ts`
- **局部清理**：`api/auth.ts`（L22 移除 `'OAUTH'` loginType；L63 移除 `oauthConfigs` 字段；L66-71 删 `OAuthConfig` 接口）；`views/login/index.vue`（L24 删 `oauthConfigs: []`）
- **路由**：后端菜单动态驱动，DB 删菜单后前端入口自动消失；仅需保证删文件后无静态 import 残留
- **验证**：`pnpm run build`

#### React（`frontend-react/`）

- **整删**：`pages/System/ImConfig/`、`pages/System/ApprovalScene/`、7 个 service 文件
- **局部清理**：`services/aiperm/index.ts`（移除 7 模块 re-export）；`services/aiperm/typings.d.ts`（删 `OAuthConfig` 类型 L981 及引用 L719-720）；`config/routes.ts`（删 L44-45 IM 配置 + 审批场景路由）
- **services 处理**：见 4.2 推荐路径（重新 generate:api + 手清）
- **验证**：`pnpm run build`（含 type-check）

### 4.5 验证策略与完成标准

#### 分层验证

| 层 | 验证 | 通过标准 | 执行者 |
|----|------|----------|--------|
| 编译 | 后端 `./gradlew compileJava` | 0 错误 | 我 |
| 编译 | Vue3 / React `pnpm run build` | 通过 | 我 |
| 迁移 | Flyway V4.13.0 | 执行成功、checksum 一致 | 宿主 |
| 运行时 | 后端启动 | 无异常、无 Bean 缺失 | 宿主 |
| 接口 | Swagger | 无 `/approval/*`、`/oauth/*`、`/system/im-config`、`/system/oauth-config` | 宿主 |
| 回归 | 核心功能 | 见下 | 宿主 |

#### 残留扫描（删除后预期全部 0 命中）

```bash
# 后端：无引用残留
grep -rniE "ApprovalClient|ApprovalHandler|ImApprovalAdapter|OAuthService|OAuthProvider|UserOauthRepository|ImConfigService" backend/src
# 后端：无残留 API 路径
grep -rniE "/approval/|/oauth/login|/system/im-config|/system/oauth-config" backend/src
# 数据库（db-query 技能）
SHOW TABLES LIKE 'sys_approval_%';        -- 空
SHOW TABLES LIKE 'sys_im_config';         -- 空
SHOW TABLES LIKE 'sys_user_oauth';        -- 空
SELECT id,name,path FROM sys_menu WHERE id IN (40,41,42);  -- 仍是监控中心
SELECT COUNT(*) FROM sys_config WHERE config_key LIKE 'oauth.%' OR config_key='approval.module.enabled';  -- 0
```

#### 回归清单（确保没删坏）

| 回归项 | 必查原因 |
|--------|----------|
| 登录（密码 / 短信 / 邮箱三种） | `AuthService.getLoginConfig()` 被改动 |
| `/auth/login-config` 接口返回 | `LoginConfigVO` 删了 `oauthConfigs`，前端不应报字段缺失 |
| 监控中心菜单（id 40/41/42） | 确认未误删 |
| 站内消息 / 公告（enterprise） | 确认 `sys_message` / `sys_notice` 未受影响 |
| 2FA / 验证码 | 确认保留的多因素认证正常 |
| 核心 RBAC（用户/角色/菜单/部门/字典）CRUD | 基线回归 |

#### 完成标准（Definition of Done）

- [ ] 后端 `./gradlew compileJava` 0 错误
- [ ] `V4.13.0` Flyway 执行成功
- [ ] 残留扫描全部 0 命中
- [ ] Vue3 `pnpm run build` 通过
- [ ] React `pnpm run build` 通过
- [ ] 后端启动成功（宿主）
- [ ] Swagger 无 approval / oauth / im-config 接口（宿主）
- [ ] 回归清单全部通过（宿主）
- [ ] 监控中心菜单完好（DB id 40/41/42 仍在）

## 5. 风险

| 风险 | 等级 | 防护 |
|------|------|------|
| 误删监控中心菜单（id 40/41/42） | 高 | 迁移脚本只写经核实的 15 个 id，保留警告注释；执行前先 `SELECT` 核对 |
| 漏删引用导致编译断裂 | 中 | Step 4 编译硬 gate |
| `enterprise` 站内消息受牵连 | 低 | 已核实 enterprise 用 `sys_message` / `sys_notice`，不引用审批消息表 |
| 前端类型残留致 build 失败 | 中 | React 重新 generate:api + 复查 index / typings；Vue3 build 暴露残留 |
| 重新生成 API 依赖后端启动 | 低 | 备选手动删除路径 |

## 6. 执行分工

| 执行者 | 负责 |
|--------|------|
| 我 | Step 1-7 删除与构建验证、迁移脚本编写、残留扫描、重新生成 API |
| 宿主 | 后端启动、Flyway 运行时执行、Swagger 检查、功能回归 |

> 遵循项目约定：我不重启服务，宿主负责启动与端到端测试；我只调用编译 / 构建查看报错。
