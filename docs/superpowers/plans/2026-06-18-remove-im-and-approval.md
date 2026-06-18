# 去除第三方 IM 对接与审批功能 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 彻底移除 aiperm 项目中未接入的第三方 IM 对接（企微/钉钉/飞书）与审批桥接功能，含后端两个模块、8 张数据库表、15 条菜单、4 个配置项，以及 Vue3 + React 双前端实现。

**Architecture:** 纯减法删除。后端删 `modules/approval` + `modules/auth/oauth` 两模块并连带清理 4 个引用文件；新建 Flyway `V4.13.0` 迁移 DROP 表 + DELETE 菜单/配置；双前端删页面/API/路由/类型。approval 与 oauth 互依，必须同批删除并以编译为 gate。

**Tech Stack:** Spring Boot 4 + Java 25 + Flyway（后端）；Vue3 + TS + Element Plus（frontend）；React + UmiJS + Ant Design Pro（frontend-react）

**依据设计**：`docs/superpowers/specs/2026-06-18-remove-im-and-approval-design.md`

---

## File Structure

### 整目录删除（`git rm -r --ignore-unmatch`）

| 路径 | 说明 |
|------|------|
| `backend/src/main/java/com/devlovecode/aiperm/modules/approval` | IM 审批桥接模块（main） |
| `backend/src/test/java/com/devlovecode/aiperm/modules/approval` | 审批模块测试（如存在） |
| `backend/src/main/java/com/devlovecode/aiperm/modules/auth/oauth` | 第三方 OAuth 登录模块（main） |
| `backend/src/test/java/com/devlovecode/aiperm/modules/auth/oauth` | OAuth 模块测试（如存在） |
| `frontend/src/views/approval` | Vue3 审批页面（todo + my） |
| `frontend/src/views/system/im-config` | Vue3 IM 配置页 |
| `frontend/src/views/system/approval-scene` | Vue3 审批场景页 |
| `frontend-react/src/pages/System/ImConfig` | React IM 配置页 |
| `frontend-react/src/pages/System/ApprovalScene` | React 审批场景页 |

### 单文件删除（`git rm`）

| 路径 |
|------|
| `frontend/src/api/oauth.ts` |
| `frontend/src/api/system/oauthConfig.ts` |
| `frontend/src/api/approval.ts` |
| `frontend/src/api/system/imConfig.ts` |
| `frontend-react/src/services/aiperm/oauth.ts` |
| `frontend-react/src/services/aiperm/oauthConfig.ts` |
| `frontend-react/src/services/aiperm/imConfig.ts` |
| `frontend-react/src/services/aiperm/approvalScene.ts` |
| `frontend-react/src/services/aiperm/approvalInstance.ts` |
| `frontend-react/src/services/aiperm/approvalCallback.ts` |
| `frontend-react/src/services/aiperm/approvalTodo.ts` |

### 局部修改（Edit）

| 文件 | 改动 |
|------|------|
| `backend/.../auth/core/vo/LoginConfigVO.java` | 删 `oauthConfigs` 字段 + `OAuthConfig` 内部类 + `import java.util.List` |
| `backend/.../auth/core/service/AuthService.java` | 删 `getLoginConfig()` 中 OAuth 构建逻辑（**不动 import**，ArrayList/List 仍被 buildMenuTree 使用） |
| `backend/.../config/WebMvcConfig.java` | 拦截器排除路径删 `/oauth/login/**`、`/approval/callback/**`；RestTemplate Bean 按 Task 1 排查结果处理 |
| `backend/.../config/SaTokenConfig.java` | 删 `/oauth/login/**`、`/approval/callback/**` 两行 |
| `frontend/src/api/auth.ts` | 删 `'OAUTH'` loginType、`oauthConfigs` 字段、`OAuthConfig` 接口 |
| `frontend/src/views/login/index.vue` | 删 `oauthConfigs: []` 初始值 |
| `frontend-react/config/routes.ts` | 删 L44-45 两条路由 |
| `frontend-react/src/services/aiperm/index.ts` | 删 7 个模块的 import + re-export |
| `frontend-react/src/services/aiperm/typings.d.ts` | 删 `OAuthConfig` 类型及其引用（grep 定位） |

### 新建

| 文件 |
|------|
| `backend/src/main/resources/db/migration/V4.13.0__remove_im_approval_oauth.sql` |

---

## Task 1: 排查待确认项

**目的**：确认 4 个待确认项的实际存在情况，结果决定后续 Task 的删除范围。本任务仅调研，不改动代码、不 commit。

**Files:** 无（仅运行排查命令）

- [ ] **Step 1: 排查 Vue3 待确认页面**

Run:
```bash
ls -d frontend/src/views/enterprise/message-template 2>/dev/null
ls -d frontend/src/views/enterprise/message-log 2>/dev/null
ls -d frontend/src/views/system/oauth-config 2>/dev/null
```
记录哪些目录存在（存在则需在 Task 4 一并删除）。

- [ ] **Step 2: 排查 React 待确认页面**

Run:
```bash
ls -d frontend-react/src/pages/Approval 2>/dev/null
ls -d frontend-react/src/pages/System/OauthConfig 2>/dev/null
```
记录存在情况（存在则需在 Task 5 一并删除）。

- [ ] **Step 3: 排查 sys_menu 中是否还有 OAuth 配置菜单遗漏**

Run（搜索所有迁移脚本中 oauth-config 相关菜单插入）:
```bash
grep -rniE "oauth-config|oauth_config|system:oauth-config" backend/src/main/resources/db/migration
```
预期：若命中，记录其 menu id，需补入 Task 3 的迁移脚本 DELETE 列表。审批/IM 的 15 个 id 已在设计中确认，此处只查 OAuth 配置菜单遗漏。

- [ ] **Step 4: 排查 RestTemplate 使用情况**

Run:
```bash
grep -rniE "RestTemplate" backend/src/main/java
```
判断标准：除 `WebMvcConfig.java` 的 Bean 定义外，是否还有其他类注入/使用 `RestTemplate`。
- 若**无其他使用** → Task 2 中删除整个 `restTemplate()` Bean + `import RestTemplate`
- 若**有其他使用** → Task 2 中仅更新注释，保留 Bean

**Step 5: 汇总排查结果**

把上述四步结果写成一个简短清单（本会话或 `.omc/notepad.md`），供 Task 2/3/4/5 参考。无需 commit。

---

## Task 2: 后端删除两模块 + 连带清理 + 编译验证

**Files:**
- Delete: `modules/approval/`、`modules/auth/oauth/`（main + test）
- Modify: `LoginConfigVO.java`、`AuthService.java`、`WebMvcConfig.java`、`SaTokenConfig.java`

> ⚠️ approval 与 oauth 互依（`ApprovalBridgeService` 注入 `UserOauthRepository`），必须同批删除；连带清理必须全部完成后才能编译通过。本任务所有 step 完成后才跑编译 gate。

- [ ] **Step 1: 删除 approval 模块（main + test）**

Run:
```bash
git rm -r --ignore-unmatch backend/src/main/java/com/devlovecode/aiperm/modules/approval
git rm -r --ignore-unmatch backend/src/test/java/com/devlovecode/aiperm/modules/approval
```

- [ ] **Step 2: 删除 auth/oauth 模块（main + test）**

Run:
```bash
git rm -r --ignore-unmatch backend/src/main/java/com/devlovecode/aiperm/modules/auth/oauth
git rm -r --ignore-unmatch backend/src/test/java/com/devlovecode/aiperm/modules/auth/oauth
```

- [ ] **Step 3: 清理 LoginConfigVO.java**

用 Edit 工具，将整个文件替换为（删除 `oauthConfigs` 字段、`OAuthConfig` 内部类、`import java.util.List`）：

文件：`backend/src/main/java/com/devlovecode/aiperm/modules/auth/core/vo/LoginConfigVO.java`

完整目标内容：
```java
package com.devlovecode.aiperm.modules.auth.core.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "登录配置响应")
public class LoginConfigVO {

	@Schema(description = "密码登录是否启用")
	private boolean passwordEnabled;

	@Schema(description = "短信验证码登录是否启用")
	private boolean smsEnabled;

	@Schema(description = "邮箱验证码登录是否启用")
	private boolean emailEnabled;

}
```

- [ ] **Step 4: 清理 AuthService.java 的 getLoginConfig()**

用 Edit 工具。`import` 不动（`ArrayList`/`List` 在 `buildMenuTree` 仍使用）。

old_string（精确匹配 L152-188）：
```java
	public LoginConfigVO getLoginConfig() {
		boolean smsEnabled = systemAccess.getBooleanConfig("login.sms.enabled", false);
		boolean emailEnabled = systemAccess.getBooleanConfig("login.email.enabled", false);

		List<LoginConfigVO.OAuthConfig> oauthConfigs = new ArrayList<>();
		if (systemAccess.getBooleanConfig("oauth.wework.enabled", false)) {
			oauthConfigs.add(LoginConfigVO.OAuthConfig.builder()
				.platform("WEWORK")
				.displayName("企业微信")
				.icon("/icons/wework.svg")
				.enabled(true)
				.build());
		}
		if (systemAccess.getBooleanConfig("oauth.dingtalk.enabled", false)) {
			oauthConfigs.add(LoginConfigVO.OAuthConfig.builder()
				.platform("DINGTALK")
				.displayName("钉钉")
				.icon("/icons/dingtalk.svg")
				.enabled(true)
				.build());
		}
		if (systemAccess.getBooleanConfig("oauth.feishu.enabled", false)) {
			oauthConfigs.add(LoginConfigVO.OAuthConfig.builder()
				.platform("FEISHU")
				.displayName("飞书")
				.icon("/icons/feishu.svg")
				.enabled(true)
				.build());
		}

		return LoginConfigVO.builder()
			.passwordEnabled(true)
			.smsEnabled(smsEnabled)
			.emailEnabled(emailEnabled)
			.oauthConfigs(oauthConfigs)
			.build();
	}
```

new_string：
```java
	public LoginConfigVO getLoginConfig() {
		boolean smsEnabled = systemAccess.getBooleanConfig("login.sms.enabled", false);
		boolean emailEnabled = systemAccess.getBooleanConfig("login.email.enabled", false);

		return LoginConfigVO.builder()
			.passwordEnabled(true)
			.smsEnabled(smsEnabled)
			.emailEnabled(emailEnabled)
			.build();
	}
```

- [ ] **Step 5: 清理 WebMvcConfig.java 拦截器排除路径**

用 Edit 工具。

old_string（L73-74）：
```java
			.excludePathPatterns("/auth/**", "/captcha/**", "/oauth/login/**", "/approval/callback/**", "/error",
					"/v3/api-docs/**", "/swagger-ui/**", "/webjars/**", "/favicon.ico");
```

new_string：
```java
			.excludePathPatterns("/auth/**", "/captcha/**", "/error",
					"/v3/api-docs/**", "/swagger-ui/**", "/webjars/**", "/favicon.ico");
```

- [ ] **Step 6: 处理 WebMvcConfig.java 的 RestTemplate Bean（依 Task 1 Step 4 结果）**

**若 Task 1 Step 4 判定 RestTemplate 无其他使用** —— 用 Edit 删除整个 Bean 及 import：

old_string：
```java
import org.springframework.web.client.RestTemplate;
```
new_string：（删除该行，即留空——实际操作时删除此 import 行）

old_string（L89-95）：
```java
	/**
	 * RestTemplate Bean（用于 OAuth HTTP 调用）
	 */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

```
new_string：（删除整段，注意保留前后空行结构）

**若 Task 1 Step 4 判定 RestTemplate 仍有其他使用** —— 仅更新注释：

old_string：
```java
	/**
	 * RestTemplate Bean（用于 OAuth HTTP 调用）
	 */
```
new_string：
```java
	/**
	 * RestTemplate Bean
	 */
```

- [ ] **Step 7: 清理 SaTokenConfig.java 排除路径**

用 Edit 工具。

old_string（L24-34）：
```java
				"/auth/login", "/auth/unified-login", "/auth/captcha", "/auth/login-config",
				// 排除验证码发送接口
				"/captcha/**",
				// 排除 OAuth 登录接口
				"/oauth/login/**",
				// 审批回调接口（来自企业IM服务端）
				"/approval/callback/**",
				// 排除Swagger相关
```

new_string：
```java
				"/auth/login", "/auth/unified-login", "/auth/captcha", "/auth/login-config",
				// 排除验证码发送接口
				"/captcha/**",
				// 排除Swagger相关
```

- [ ] **Step 8: 后端编译验证（硬 gate）**

Run:
```bash
cd backend && ./gradlew compileJava
```
Expected: `BUILD SUCCESSFUL`，0 错误。
若失败：根据报错定位遗漏的引用并修复（通常是漏删的 import 或残留引用），再次编译直到通过。

- [ ] **Step 9: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/auth/core/vo/LoginConfigVO.java \
        backend/src/main/java/com/devlovecode/aiperm/modules/auth/core/service/AuthService.java \
        backend/src/main/java/com/devlovecode/aiperm/config/WebMvcConfig.java \
        backend/src/main/java/com/devlovecode/aiperm/config/SaTokenConfig.java
git commit -m "refactor(backend): 移除 IM 审批桥接与第三方 OAuth 登录模块

- 删除 modules/approval 与 modules/auth/oauth 两模块
- 连带清理 LoginConfigVO/AuthService 的 OAuth 配置逻辑
- 清理 WebMvcConfig/SaTokenConfig 中 /oauth/login 与 /approval/callback 排除路径"
```
（`git rm` 已 stage 的目录删除会一并提交。）

---

## Task 3: 数据库迁移 V4.13.0

**Files:**
- Create: `backend/src/main/resources/db/migration/V4.13.0__remove_im_approval_oauth.sql`

> 本任务只创建并提交迁移脚本；实际 DB 执行由宿主启动后端时 Flyway 自动完成（见 Task 6 验证）。

- [ ] **Step 1: 创建迁移脚本**

创建文件 `backend/src/main/resources/db/migration/V4.13.0__remove_im_approval_oauth.sql`，内容如下。
（**注意**：若 Task 1 Step 3 排查到 OAuth 配置菜单 id，将其追加到 ②③ 两段的 `IN (...)` 列表末尾。）

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
```

- [ ] **Step 2: 校验菜单 id 安全性（执行前核对）**

此为执行迁移前的核对提示（由宿主在启动前/启动时执行）：在目标库运行以下查询，确认待删 id 当前确为审批/IM 菜单，且 40/41/42 确为监控中心：

```sql
SELECT id, name, path, permission FROM sys_menu
WHERE id IN (18,181,182,19,191,192,193,194,25,251,252,253,254,26,261,40,41,42)
ORDER BY id;
```
预期：18=IM平台配置、19=审批场景管理、25=消息模板、26=消息记录（及各自按钮）；40=监控中心、41=在线用户、42=服务监控。

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/resources/db/migration/V4.13.0__remove_im_approval_oauth.sql
git commit -m "chore(db): V4.13.0 移除 IM/审批/OAuth 相关表与菜单

- DROP 8 张表（审批6 + OAuth2）
- 按安全 id 删除 15 条菜单及角色关联（绝不碰 V4.8.0 覆写的监控中心 id 40/41/42）
- 删除 oauth.*.enabled 与 approval.module.enabled 配置项"
```

---

## Task 4: Vue3 前端删除

**Files:**
- Delete: `views/approval/`、`views/system/im-config/`、`views/system/approval-scene/`、`api/oauth.ts`、`api/system/oauthConfig.ts`、`api/approval.ts`、`api/system/imConfig.ts`（+ Task 1 Step 1 确认存在的 enterprise/message-template、enterprise/message-log、system/oauth-config）
- Modify: `api/auth.ts`、`views/login/index.vue`

- [ ] **Step 1: 删除 Vue3 页面目录**

Run:
```bash
git rm -r --ignore-unmatch frontend/src/views/approval
git rm -r --ignore-unmatch frontend/src/views/system/im-config
git rm -r --ignore-unmatch frontend/src/views/system/approval-scene
# Task 1 Step 1 若确认存在，则一并删除：
git rm -r --ignore-unmatch frontend/src/views/enterprise/message-template
git rm -r --ignore-unmatch frontend/src/views/enterprise/message-log
git rm -r --ignore-unmatch frontend/src/views/system/oauth-config
```

- [ ] **Step 2: 删除 Vue3 API 文件**

Run:
```bash
git rm frontend/src/api/oauth.ts
git rm frontend/src/api/system/oauthConfig.ts
git rm frontend/src/api/approval.ts
git rm frontend/src/api/system/imConfig.ts
```

- [ ] **Step 3: 清理 frontend/src/api/auth.ts**

用 Edit 工具，三处改动：

改动 a — `loginType` 联合类型移除 `'OAUTH'`：

old_string：
```typescript
  loginType: 'PASSWORD' | 'SMS' | 'EMAIL' | 'OAUTH'
```
new_string：
```typescript
  loginType: 'PASSWORD' | 'SMS' | 'EMAIL'
```

改动 b — `LoginConfigVO` 删 `oauthConfigs` 字段 + `OAuthConfig` 接口（连成一段替换）：

old_string：
```typescript
/** 登录配置 */
export interface LoginConfigVO {
  passwordEnabled: boolean
  smsEnabled: boolean
  emailEnabled: boolean
  oauthConfigs: OAuthConfig[]
}

interface OAuthConfig {
  platform: string
  displayName: string
  icon: string
  enabled: boolean
}
```
new_string：
```typescript
/** 登录配置 */
export interface LoginConfigVO {
  passwordEnabled: boolean
  smsEnabled: boolean
  emailEnabled: boolean
}
```

- [ ] **Step 4: 清理 frontend/src/views/login/index.vue 的 loginConfig 初始值**

用 Edit 工具。

old_string：
```typescript
const loginConfig = ref<LoginConfigVO>({
  passwordEnabled: true,
  smsEnabled: false,
  emailEnabled: false,
  oauthConfigs: [],
})
```
new_string：
```typescript
const loginConfig = ref<LoginConfigVO>({
  passwordEnabled: true,
  smsEnabled: false,
  emailEnabled: false,
})
```

- [ ] **Step 5: 检查并清理可能的静态 import 残留**

Run:
```bash
grep -rniE "views/approval|views/system/im-config|views/system/approval-scene|api/oauth|api/approval|api/system/imConfig|api/system/oauthConfig|oauthConfigs|OAuthConfig" frontend/src
```
预期：0 命中。若有命中（如某 router/组件仍 import 已删文件），按报错清理对应 import。

- [ ] **Step 6: Vue3 构建验证**

Run:
```bash
cd frontend && pnpm run build
```
Expected: 构建成功，无类型错误、无 "Cannot find module" 报错。

- [ ] **Step 7: Commit**

```bash
git add frontend/src/api/auth.ts frontend/src/views/login/index.vue
git commit -m "refactor(frontend): 移除 IM 配置/审批/OAuth 相关页面与 API

- 删除 approval/im-config/approval-scene 页面目录
- 删除 oauth/oauthConfig/approval/imConfig API 文件
- 清理 auth.ts 与 login.vue 中的 OAuth 类型与初始值"
```

---

## Task 5: React 前端删除

**Files:**
- Delete: `pages/System/ImConfig/`、`pages/System/ApprovalScene/`（+ Task 1 Step 2 确认存在的 `pages/Approval/`、`pages/System/OauthConfig/`）、7 个 service 文件
- Modify: `config/routes.ts`、`services/aiperm/index.ts`、`services/aiperm/typings.d.ts`

> services 与 typings 由 OpenAPI 生成。本任务走「手动删除」路径（执行者可独立完成）。若宿主后续运行 `pnpm run generate:api`（后端已删），生成器会自动同步 services/typings/index，届时复查即可，无需重复手动清理。

- [ ] **Step 1: 删除 React 页面目录**

Run:
```bash
git rm -r --ignore-unmatch frontend-react/src/pages/System/ImConfig
git rm -r --ignore-unmatch frontend-react/src/pages/System/ApprovalScene
# Task 1 Step 2 若确认存在，则一并删除：
git rm -r --ignore-unmatch frontend-react/src/pages/Approval
git rm -r --ignore-unmatch frontend-react/src/pages/System/OauthConfig
```

- [ ] **Step 2: 清理 config/routes.ts**

用 Edit 工具，删除 IM 配置与审批场景两条路由。

old_string：
```typescript
      { path: '/system/im-config', name: 'IM平台配置', component: './System/ImConfig', access: 'canUser' },
      { path: '/system/approval-scene', name: '审批场景管理', component: './System/ApprovalScene', access: 'canUser' },
```
new_string：（删除这两行，保留上下文）

即原 L43-46：
```typescript
      { path: '/system/dict', name: '字典管理', component: './System/Dict', access: 'canUser' },
      { path: '/system/im-config', name: 'IM平台配置', component: './System/ImConfig', access: 'canUser' },
      { path: '/system/approval-scene', name: '审批场景管理', component: './System/ApprovalScene', access: 'canUser' },
      { path: '/system/file', name: '文件管理', component: './System/File', access: 'canUser' },
```
改为：
```typescript
      { path: '/system/dict', name: '字典管理', component: './System/Dict', access: 'canUser' },
      { path: '/system/file', name: '文件管理', component: './System/File', access: 'canUser' },
```

- [ ] **Step 3: 删除 7 个 React service 文件**

Run:
```bash
git rm frontend-react/src/services/aiperm/oauth.ts
git rm frontend-react/src/services/aiperm/oauthConfig.ts
git rm frontend-react/src/services/aiperm/imConfig.ts
git rm frontend-react/src/services/aiperm/approvalScene.ts
git rm frontend-react/src/services/aiperm/approvalInstance.ts
git rm frontend-react/src/services/aiperm/approvalCallback.ts
git rm frontend-react/src/services/aiperm/approvalTodo.ts
```

- [ ] **Step 4: 清理 services/aiperm/index.ts**

用 Edit 工具，将整个文件替换为（删除 7 个 import 与 7 个 re-export：`oauthConfig`、`imConfig`、`approvalScene`、`approvalInstance`、`approvalCallback`、`oauth`、`approvalTodo`）。

文件：`frontend-react/src/services/aiperm/index.ts`

完整目标内容：
```typescript
// @ts-ignore
/* eslint-disable */
// API 更新时间：
// API 唯一标识：
import * as user from "./user";
import * as role from "./role";
import * as post from "./post";
import * as mfaPolicy from "./mfaPolicy";
import * as menu from "./menu";
import * as dictType from "./dictType";
import * as dictData from "./dictData";
import * as dept from "./dept";
import * as captchaConfig from "./captchaConfig";
import * as profile from "./profile";
import * as notice from "./notice";
import * as message from "./message";
import * as job from "./job";
import * as config from "./config";
import * as oss from "./oss";
import * as mfa from "./mfa";
import * as captcha from "./captcha";
import * as auth from "./auth";
import * as monitorServer from "./monitorServer";
import * as monitorOnline from "./monitorOnline";
import * as loginLog from "./loginLog";
import * as jobLog from "./jobLog";
import * as monitorCache from "./monitorCache";
import * as operLog from "./operLog";
import * as dashboard from "./dashboard";
export default {
  user,
  role,
  post,
  mfaPolicy,
  menu,
  dictType,
  dictData,
  dept,
  captchaConfig,
  profile,
  notice,
  message,
  job,
  config,
  oss,
  mfa,
  captcha,
  auth,
  monitorServer,
  monitorOnline,
  loginLog,
  jobLog,
  monitorCache,
  operLog,
  dashboard,
};
```

- [ ] **Step 5: 清理 services/aiperm/typings.d.ts 的 OAuthConfig**

先定位：
```bash
grep -nE "OAuthConfig" frontend-react/src/services/aiperm/typings.d.ts
```
预期命中两处：一处是 `type OAuthConfig = {...}` 定义（约 L981），一处是 `LoginConfigVO` 接口内 `oauthConfigs?: OAuthConfig[]` 字段引用（约 L719-720）。

用 Edit 工具删除这两处：

a) 删除 `LoginConfigVO` 中的 `oauthConfigs` 字段行（含其上方/下方保持结构）。用 grep 输出的行号定位该行，删除整行。

b) 删除 `type OAuthConfig = { ... }` 整个类型定义块。

> 若 grep 显示 `OAuthConfig` 还被其他接口引用，一并删除引用行。删除后再次 grep 确认 0 命中。

- [ ] **Step 6: 检查并清理可能的静态引用残留**

Run:
```bash
grep -rniE "System/ImConfig|System/ApprovalScene|pages/Approval|services/aiperm/oauth|services/aiperm/approval|services/aiperm/imConfig|OAuthConfig" frontend-react/src frontend-react/config
```
预期：0 命中。若有命中，按报错清理。

- [ ] **Step 7: React 构建验证**

Run:
```bash
cd frontend-react && pnpm run build
```
Expected: 构建成功（含 type-check），无类型错误、无模块解析失败。

- [ ] **Step 8: Commit**

```bash
git add frontend-react/config/routes.ts \
        frontend-react/src/services/aiperm/index.ts \
        frontend-react/src/services/aiperm/typings.d.ts
git commit -m "refactor(frontend-react): 移除 IM 配置/审批/OAuth 相关页面与 service

- 删除 ImConfig/ApprovalScene 页面与 7 个 service 文件
- 清理 routes.ts 静态路由、index.ts re-export、typings.d.ts OAuthConfig 类型"
```

---

## Task 6: 残留扫描与回归验证

**目的**：确认删除彻底、未破坏其他功能。本任务由执行者做静态/DB 校验，回归功能由宿主执行。

**Files:** 无（仅校验）

- [ ] **Step 1: 后端代码残留扫描（执行者）**

Run:
```bash
grep -rniE "ApprovalClient|ApprovalHandler|ImApprovalAdapter|OAuthService|OAuthProvider|UserOauthRepository|ImConfigService" backend/src
grep -rniE "/approval/|/oauth/login|/system/im-config|/system/oauth-config" backend/src
```
Expected: 两条命令均 0 命中。若有命中，说明有遗漏引用，回到对应 Task 补删。

- [ ] **Step 2: 前端残留扫描（执行者）**

Run:
```bash
grep -rniE "oauthConfigs|OAuthConfig|imConfig|approvalScene|approvalInstance|approvalTodo|approvalCallback" frontend/src frontend-react/src
```
Expected: 0 命中（services 目录内已删文件除外）。

- [ ] **Step 3: 数据库校验（宿主启动后端使 V4.13.0 执行后，或用 db-query 技能）**

```sql
SHOW TABLES LIKE 'sys_approval_%';        -- 预期空
SHOW TABLES LIKE 'sys_im_config';         -- 预期空
SHOW TABLES LIKE 'sys_message_template';  -- 预期空
SHOW TABLES LIKE 'sys_message_log';       -- 预期空
SHOW TABLES LIKE 'sys_user_oauth';        -- 预期空
SHOW TABLES LIKE 'sys_oauth_config';      -- 预期空
SELECT id,name,path FROM sys_menu WHERE id IN (40,41,42);  -- 仍是监控中心：监控中心/在线用户/服务监控
SELECT COUNT(*) FROM sys_config WHERE config_key LIKE 'oauth.%' OR config_key='approval.module.enabled';  -- 预期 0
```

- [ ] **Step 4: Swagger 接口校验（宿主）**

访问 `http://localhost:8080/api/swagger-ui.html`，确认无以下接口分组/路径：
- `/approval/*`（instance / todo / callback / scene）
- `/oauth/*`
- `/system/im-config`
- `/system/oauth-config`

- [ ] **Step 5: 功能回归（宿主）**

逐项验证：
- [ ] 登录（密码 / 短信 / 邮箱三种方式均正常）—— 因 `AuthService.getLoginConfig()` 被改动
- [ ] `/auth/login-config` 接口返回不含 `oauthConfigs` 字段，前端登录页不报错
- [ ] 监控中心菜单（在线用户 / 服务监控）仍可见可用 —— 确认未误删 id 40/41/42
- [ ] 站内消息（消息中心）、公告通知 CRUD 正常 —— 确认 enterprise 未受影响
- [ ] 2FA / 验证码功能正常
- [ ] 核心 RBAC（用户/角色/菜单/部门/字典）CRUD 正常

- [ ] **Step 6: 完成**

所有校验通过即达成 Definition of Done。无额外 commit（本任务为纯验证）。若 Step 1/2 发现残留并补删，则单独 commit：`chore: 清理 IM/审批/OAuth 残留引用`。

---

## Self-Review

- **Spec 覆盖**：设计 4.1（清单）→ Task 1-5 全覆盖；4.2（执行顺序）→ Task 顺序对应；4.3（迁移）→ Task 3；4.4（前端）→ Task 4-5；4.5（验证 + DoD）→ Task 6。无遗漏。
- **占位符**：无 TBD/TODO；Task 1 待确认项均有明确排查命令 + 处理分支；RestTemplate 双分支处理已写出两套完整代码。
- **一致性**：菜单 id 列表（15 个）在 Task 3 与设计一致；模块名/路径前后统一；`AuthService` import 处理已修正（不删 ArrayList/List，因 buildMenuTree 仍用）。
- **类型一致**：`LoginConfigVO`（后端）、`LoginConfigVO`（Vue3 auth.ts）、`OAuthConfig`（typings）三处字段名统一为 `oauthConfigs`，删除目标一致。
- **修正项**：设计文档 4.1.2 称"删 AuthService 未用的 import ArrayList"有误——`ArrayList` 在 `buildMenuTree`（L316）仍使用，本计划已改为只删 OAuth 逻辑、保留 import。
