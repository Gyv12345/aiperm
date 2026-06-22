# React 移动端 Phase 0（项目骨架）实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 repo 内新建独立移动端项目 `frontend-react-mobile`（UmiJS Max + antd-mobile），打通「登录 → 首页 → 应用宫格 → 用户列表」全链路，验证复用后端 API / Sa-Token / OpenAPI / 请求拦截 / 鉴权模型的骨架可跑。

**Architecture:** 与桌面端 `frontend-react` 平级的兄弟项目，共享后端 API 与鉴权，UI 独立（antd-mobile）。**不开 umi 的 `antd` / `layout` 插件**（避免打包桌面端 antd + ProLayout），改用 umi core 内置的 `onRouteChange` 做登录守卫，用约定式 `src/layouts/index.tsx` 自搭 TabBar 布局。请求拦截层从桌面端搬来，仅把 antd `message` 换成 antd-mobile `Toast`。

**Tech Stack:** UmiJS Max 4 · React 18 · antd-mobile 5 · antd-mobile-icons · postcss-px-to-viewport-8-plugin（375 设计稿 vw 适配）· TypeScript 5 · @umijs/openapi（API 生成）。

**关联 spec:** `docs/superpowers/specs/2026-06-22-react-mobile-design.md`

---

## 验证策略（脚手架阶段）

Phase 0 是项目骨架搭建，绝大部分是配置与脚手架代码，**不引入 Vitest**（测试基础设施留到 Phase 1 通用组件时再加，遵循 YAGNI）。每个任务的验证手段为：

- **类型检查**：`pnpm type-check`（= `max setup && tsc --noEmit`），预期零错误
- **构建**：`pnpm build`（= `max build`），预期成功产出 `dist/`
- **运行时**：`pnpm dev`（端口 8001）+ 浏览器手动验证（末尾有 E2E 清单）

凡步骤涉及"运行命令"，都给出**精确命令**与**预期输出**。

> **umi setup 约束（执行时关键）**：`max setup`（生成 `src/.umi/tsconfig.json`）在 setup 阶段会 `resolve.sync` `config/routes.ts` 引用的**每个组件文件**，任一缺失即抛 `MODULE_NOT_FOUND` 终止（而非 tsc 阶段才报"找不到模块"）。因此实际执行时，Task 2–10 应**连续创建所有源文件并各自 commit**，**中间步骤不跑** `max setup` / `type-check` / `build`；统一在 Task 11（全部文件就绪后）跑 `max setup` + `pnpm tsc` + `pnpm build` 做一次性验证。各 Task 内的 type-check 步骤描述的是「该 Task 代码本身无误」的验证意图，实际执行时机以本约束为准。

---

## 文件结构总览

**原样复用**（`cp` 自 `frontend-react/`，不改动）：

| 目标文件 | 源文件 | 职责 |
|---------|--------|------|
| `frontend-react-mobile/.npmrc` | `frontend-react/.npmrc` | npmmirror 镜像 |
| `frontend-react-mobile/.prettierrc` | `frontend-react/.prettierrc` | 格式化 |
| `frontend-react-mobile/.eslintrc.js` | `frontend-react/.eslintrc.js` | umi eslint |
| `frontend-react-mobile/.stylelintrc.js` | `frontend-react/.stylelintrc.js` | umi stylelint |
| `frontend-react-mobile/.gitignore` | `frontend-react/.gitignore` | 忽略 node_modules/dist/.umi |
| `frontend-react-mobile/typings.d.ts` | `frontend-react/typings.d.ts` | `@umijs/max/typings` |
| `frontend-react-mobile/public/logo.svg` | `frontend-react/public/logo.svg` | 站点 logo |
| `frontend-react-mobile/openapi.json` | `frontend-react/openapi.json` | 后端 OpenAPI（同源） |
| `frontend-react-mobile/scripts/gen-openapi.mjs` | `frontend-react/scripts/gen-openapi.mjs` | OpenAPI→services 生成脚本（含 R\<T\> 解包 hook） |
| `frontend-react-mobile/config/proxy.ts` | `frontend-react/config/proxy.ts` | `/api` 代理到 8080 |
| `frontend-react-mobile/src/common.d.ts` | `frontend-react/src/common.d.ts` | `API<T>` / `PageResult<T>` / `PageParams` 类型 |
| `frontend-react-mobile/src/global.ts` | `frontend-react/src/global.ts` | 业务静默错误拦截 |
| `frontend-react-mobile/src/access.ts` | `frontend-react/src/access.ts` | 权限模型（Phase 1 用，Phase 0 先建好） |

**改/新写**（完整代码见各 Task）：

| 文件 | 处理 | 职责 |
|------|------|------|
| `frontend-react-mobile/package.json` | 新 | 依赖：umi max + antd-mobile，无 antd/pro-components |
| `frontend-react-mobile/tsconfig.json` | 新 | extends `./src/.umi/tsconfig.json`（umi 生成） |
| `frontend-react-mobile/config/config.ts` | 新 | plugins：access/model/initialState/request；vw 适配；viewport meta |
| `frontend-react-mobile/config/routes.ts` | 新 | 移动端路由 |
| `frontend-react-mobile/src/global.css` | 新 | 移动端全局样式 |
| `frontend-react-mobile/src/requestErrorConfig.ts` | 改 | 桌面端搬来，`message`→`Toast` |
| `frontend-react-mobile/src/services/auth.ts` | 改 | 手写认证 service（getUserInfo/logout/getLoginConfig，去掉 getMenus） |
| `frontend-react-mobile/src/app.tsx` | 新 | getInitialState（只拉 userInfo）+ request + onRouteChange 守卫 + rootContainer |
| `frontend-react-mobile/src/layouts/index.tsx` | 新 | MobileLayout：TabBar + 容器（按路径显隐 TabBar） |
| `frontend-react-mobile/src/pages/User/Login/index.tsx` | 新 | antd-mobile 登录页 |
| `frontend-react-mobile/src/pages/Dashboard/index.tsx` | 新 | 首页占位 |
| `frontend-react-mobile/src/pages/Apps/index.tsx` | 新 | 功能宫格（含用户管理入口） |
| `frontend-react-mobile/src/pages/Message/index.tsx` | 新 | 消息占位 |
| `frontend-react-mobile/src/pages/Profile/index.tsx` | 新 | 个人中心（含退出） |
| `frontend-react-mobile/src/pages/System/User/index.tsx` | 新 | 示范列表页（卡片列表 + 下拉刷新） |
| `frontend-react-mobile/src/pages/404.tsx` | 新 | 404 |

---

## Task 1: 初始化项目骨架与依赖

**Files:**
- Create: `frontend-react-mobile/`（整个目录树）
- Create: `frontend-react-mobile/package.json`
- Create: `frontend-react-mobile/tsconfig.json`
- Copy: 13 个原样复用文件（见上表）

- [ ] **Step 1: 创建目录结构**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
mkdir -p frontend-react-mobile/{config,scripts,public,src/{layouts,components,pages/{User/Login,Dashboard,Apps,Message,Profile,System/User},services}}
```

- [ ] **Step 2: 原样复制 13 个复用文件**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile
cp ../frontend-react/.npmrc .npmrc
cp ../frontend-react/.prettierrc .prettierrc
cp ../frontend-react/.eslintrc.js .eslintrc.js
cp ../frontend-react/.stylelintrc.js .stylelintrc.js
cp ../frontend-react/.gitignore .gitignore
cp ../frontend-react/typings.d.ts typings.d.ts
cp ../frontend-react/public/logo.svg public/logo.svg
cp ../frontend-react/openapi.json openapi.json
cp ../frontend-react/scripts/gen-openapi.mjs scripts/gen-openapi.mjs
cp ../frontend-react/config/proxy.ts config/proxy.ts
cp ../frontend-react/src/common.d.ts src/common.d.ts
cp ../frontend-react/src/global.ts src/global.ts
cp ../frontend-react/src/access.ts src/access.ts
```

- [ ] **Step 3: 写 `package.json`**

Create `frontend-react-mobile/package.json`:

```json
{
  "private": true,
  "name": "aiperm-react-mobile",
  "version": "0.1.0",
  "author": "shichenyang <gyv12345@163.com>",
  "scripts": {
    "dev": "max dev --port 8001",
    "build": "max build",
    "format": "prettier --cache --write .",
    "setup": "max setup",
    "start": "max dev --port 8001",
    "tsc": "tsc --noEmit",
    "type-check": "max setup && tsc --noEmit",
    "openapi": "node scripts/gen-openapi.mjs",
    "openapi:fetch": "curl -s http://localhost:8080/v3/api-docs -o openapi.json && node scripts/gen-openapi.mjs"
  },
  "dependencies": {
    "@umijs/max": "^4.6.62",
    "antd-mobile": "^5.37.1",
    "antd-mobile-icons": "^0.3.0",
    "react": "^18.3.1",
    "react-dom": "^18.3.1"
  },
  "devDependencies": {
    "@types/react": "^18.0.33",
    "@types/react-dom": "^18.0.11",
    "@umijs/openapi": "^1.13.0",
    "postcss-px-to-viewport-8-plugin": "^1.2.5",
    "prettier": "^2.8.7",
    "prettier-plugin-organize-imports": "^3.2.2",
    "prettier-plugin-packagejson": "^2.4.3",
    "typescript": "^5.0.3"
  }
}
```

> 与桌面端对比：去掉 `@ant-design/icons` / `@ant-design/pro-components` / `antd`；新增 `antd-mobile` / `antd-mobile-icons` / `postcss-px-to-viewport-8-plugin`。

- [ ] **Step 4: 写 `tsconfig.json`**

Create `frontend-react-mobile/tsconfig.json`:

```json
{
  "extends": "./src/.umi/tsconfig.json"
}
```

> `src/.umi/tsconfig.json` 由 `max setup` 生成（Task 2 执行）。

- [ ] **Step 5: 安装依赖**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm install`
Expected: 安装完成，生成 `node_modules/` 与 `pnpm-lock.yaml`，无 peer dependency 报错（antd-mobile 与 react 18 兼容）。

- [ ] **Step 6: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend-react-mobile/
git commit -m "feat(mobile): 初始化 frontend-react-mobile 项目骨架与依赖"
```

---

## Task 2: umi 配置与移动端适配

**Files:**
- Create: `frontend-react-mobile/config/config.ts`
- Create: `frontend-react-mobile/config/routes.ts`
- Create: `frontend-react-mobile/src/global.css`

- [ ] **Step 1: 写 `config/config.ts`**

Create `frontend-react-mobile/config/config.ts`:

```typescript
/**
 * Umi Max 主配置（移动端）
 *
 * 关键差异（vs 桌面端）：
 * - 不开 antd / layout 插件 → 不打包 antd 与 ProLayout，UI 全用 antd-mobile。
 * - 登录守卫用 umi core 的 onRouteChange（见 src/app.tsx），不依赖 layout 插件。
 * - postcss-px-to-viewport 做 vw 适配（375 设计稿基准）。
 *
 * 文档：https://umijs.org/docs/api/config
 */
import { defineConfig } from '@umijs/max';
import pxToViewport from 'postcss-px-to-viewport-8-plugin';
import proxy from './proxy';
import routes from './routes';

export default defineConfig({
  // 运行时插件：鉴权模型 / 共享状态 / 初始状态 / 请求。不开 antd、layout。
  access: {},
  model: {},
  initialState: {},
  request: {},

  routes,

  proxy,

  npmClient: 'pnpm',

  outputPath: 'dist',

  hash: true,

  favicons: ['/logo.svg'],

  title: 'AIPerm',

  // 移动端 viewport：禁止缩放，viewport-fit=cover 适配刘海屏
  metas: [
    {
      name: 'viewport',
      content:
        'width=device-width, initial-scale=1, maximum-scale=1, minimum-scale=1, user-scalable=no, viewport-fit=cover',
    },
  ],

  // vw 适配：设计稿宽 375，1px 不会被转换（minPixelValue:1）
  extraPostCSSPlugins: [
    pxToViewport({
      viewportWidth: 375,
      unitPrecision: 5,
      viewportUnit: 'vw',
      selectorBlackList: [],
      minPixelValue: 1,
      mediaQuery: false,
    }),
  ],
});
```

- [ ] **Step 2: 写 `config/routes.ts`**

Create `frontend-react-mobile/config/routes.ts`:

```typescript
/**
 * 移动端路由
 *
 * - 登录守卫由 src/app.tsx 的 onRouteChange 处理（未登录访问非 /user/* → 跳登录）。
 * - TabBar 显隐由 src/layouts/index.tsx 按路径决定（仅 /dashboard /apps /message /profile 显示 TabBar）。
 * - 不使用 layout: false（那是 layout 插件概念，本项目不开该插件）。
 * - access 字段留到 Phase 1 细粒度权限时启用。
 *
 * 文档：https://umijs.org/docs/guides/routes
 */
export default [
  { path: '/user/login', name: '登录', component: './User/Login' },
  { path: '/dashboard', name: '首页', component: './Dashboard' },
  { path: '/apps', name: '应用', component: './Apps' },
  { path: '/message', name: '消息', component: './Message' },
  { path: '/profile', name: '我的', component: './Profile' },
  { path: '/system/user', name: '用户管理', component: './System/User' },
  { path: '/', redirect: '/dashboard' },
  { path: '*', component: './404' },
];
```

- [ ] **Step 3: 写 `src/global.css`**

Create `frontend-react-mobile/src/global.css`:

```css
/**
 * 全局样式（移动端）
 * umi 自动加载 src/global.css。
 */

html,
body,
#root {
  height: 100%;
  margin: 0;
  padding: 0;
}

html {
  -webkit-text-size-adjust: 100%;
}

body {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: #f5f7fa;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
    'Helvetica Neue', Arial, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 移动端去除点击高亮、长按选择 */
* {
  -webkit-tap-highlight-color: transparent;
}

/* 安全区适配（刘海屏顶部 / 底部 home 条）由组件层 SafeArea 处理，这里提供 env 变量备用 */
:root {
  --safe-area-top: env(safe-area-inset-top);
  --safe-area-bottom: env(safe-area-inset-bottom);
}
```

- [ ] **Step 4: 生成 .umi 并类型检查**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm type-check`
Expected:
- `max setup` 生成 `src/.umi/`（含 `tsconfig.json`）
- `tsc --noEmit` 因此时 `src/app.tsx` 等尚未创建，可能报「找不到模块」——**这是预期的**（后续 Task 补齐文件）。只要 `max setup` 成功生成 `.umi/` 即通过本步。

> 若 `tsc` 报错仅为「找不到 './app'、'@/services/...' 等未创建模块」，记下继续；这些模块在 Task 3-9 创建。

- [ ] **Step 5: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend-react-mobile/config/ frontend-react-mobile/src/global.css
git commit -m "feat(mobile): umi 配置与移动端 vw 适配"
```

---

## Task 3: 请求拦截层（搬来 + Toast 替换）

**Files:**
- Create: `frontend-react-mobile/src/requestErrorConfig.ts`

- [ ] **Step 1: 写 `src/requestErrorConfig.ts`**

Create `frontend-react-mobile/src/requestErrorConfig.ts`（基于桌面端，唯一改动：antd `message` → antd-mobile `Toast`）:

```typescript
/**
 * umi/max request 错误处理与响应/请求拦截器（移动端）
 *
 * 与桌面端 frontend-react/src/requestErrorConfig.ts 逻辑一致，唯一差异：
 * 提示组件由 antd 的 message 换成 antd-mobile 的 Toast。
 *
 * 后端约定：
 * - 鉴权：Sa-Token，请求头 Authorization 直接放裸 token（无 Bearer 前缀）。
 * - 统一响应 R<T> = { code, message, data }，code===200 为成功（兼容 0）。
 * - 分页 PageResult<T> = { total, list, pageNum, pageSize, pages }。
 * - HTTP 401：未登录 → 清 token，跳 /user/login。
 * - HTTP 423：需要二次验证（MFA）→ 派发 'mfa-required' 事件（Phase 4 由 MfaVerifyDialog 处理）。
 * - 业务 code 非 200：Toast 提示 message 字段。
 *
 * 文档：https://umijs.org/docs/max/request
 */
import { Toast } from 'antd-mobile';
import type { RequestInterceptor, ResponseInterceptor } from '@umijs/max';

/** localStorage 中 token 的 key */
export const TOKEN_KEY = 'token';
/** 业务成功码（200 为标准，0 兼容） */
const SUCCESS_CODES = new Set([0, 200]);
/** 防止 401 重复跳转 */
let isRedirecting = false;

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string | null) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}

/** 跳转登录页（带防重入） */
function redirectToLogin() {
  if (isRedirecting) return;
  isRedirecting = true;
  Toast.show({ content: '登录状态已失效，请重新登录', icon: 'fail' });
  setTimeout(() => {
    isRedirecting = false;
    const redirect = encodeURIComponent(
      window.location.pathname + window.location.search,
    );
    window.location.href = `/user/login?redirect=${redirect}`;
  }, 600);
}

/** 请求拦截器：注入 Authorization（无 Bearer 前缀） */
export const requestInterceptor: RequestInterceptor = (config: any) => {
  const token = getToken();
  if (token) {
    config.headers = {
      ...config.headers,
      Authorization: token,
    };
  }
  return config;
};

/**
 * 业务失败的静默错误标记：携带该标记的 Error 用于避免 errorHandler 重复提示。
 */
export const SILENT_BUSINESS_ERROR = '__silentBusinessError';

/**
 * 响应拦截器：解包 R<T>.data
 * - 文件流（blob/arraybuffer/非 JSON）原样放行。
 * - JSON：业务码非 200 → Toast 提示 + reject 带标记的错误；成功 → 改写为业务数据。
 */
export const responseInterceptor: ResponseInterceptor = (response: any) => {
  const responseType = response.config?.responseType;
  const headers = response.headers || {};
  const contentType: string =
    (typeof headers.get === 'function'
      ? headers.get('Content-Type')
      : headers['Content-Type']) || '';

  if (
    responseType === 'blob' ||
    responseType === 'arraybuffer' ||
    contentType.includes('application/octet-stream') ||
    contentType.includes('application/vnd.ms-excel')
  ) {
    return response;
  }

  if (!contentType.includes('application/json')) {
    return response;
  }

  const data: API = response.data;
  if (data == null || typeof data !== 'object') return response;

  if (SUCCESS_CODES.has(data.code)) {
    response.data = data.data;
    return response;
  }

  const reqUrl = `${response.config?.method?.toUpperCase() || ''} ${response.config?.url || ''}`;
  // eslint-disable-next-line no-console
  console.warn('[request] 业务失败:', reqUrl, data);
  const msg = data.message || data.msg || '请求失败';
  Toast.show({ content: msg, icon: 'fail' });
  const err = new Error(msg) as Error & { [SILENT_BUSINESS_ERROR]?: boolean };
  err[SILENT_BUSINESS_ERROR] = true;
  return Promise.reject(err);
};

/** umi request errorConfig：统一错误处理（HTTP 层） */
export const errorHandler = (error: any) => {
  if (error?.[SILENT_BUSINESS_ERROR]) {
    return;
  }
  const response = error?.response;
  if (!response) {
    const msg = error?.message || '网络异常，请稍后重试';
    if (msg.includes('timeout')) {
      Toast.show({ content: '请求超时，请稍后重试', icon: 'fail' });
    }
    return;
  }

  const status: number = response.status;

  if (status === 401) {
    setToken(null);
    redirectToLogin();
    return;
  }

  if (status === 423) {
    // MFA 二次验证：Phase 4 由 MfaVerifyDialog 监听
    window.dispatchEvent(new CustomEvent('mfa-required'));
    return;
  }

  if (status === 403) {
    Toast.show({ content: '没有访问权限', icon: 'fail' });
    return;
  }

  if (status === 404) {
    return;
  }

  response
    ?.clone?.()
    ?.json?.()
    ?.then?.((body: API) => {
      Toast.show({
        content: body?.message || body?.msg || `请求错误（${status}）`,
        icon: 'fail',
      });
    })
    ?.catch?.(() => {});
};

/** 透传给 config.request 的配置对象 */
export const requestConfig = {
  baseURL: '/api',
  timeout: 30000,
  requestInterceptors: [requestInterceptor],
  responseInterceptors: [responseInterceptor],
  errorConfig: {
    errorThrower: () => {},
    errorHandler,
  },
};
```

- [ ] **Step 2: 类型检查**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm tsc`
Expected: `src/requestErrorConfig.ts` 依赖的 `API` 类型（来自 `common.d.ts`，Task 1 已复制）存在；本文件无类型错误（其他未创建文件报错忽略）。

- [ ] **Step 3: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend-react-mobile/src/requestErrorConfig.ts
git commit -m "feat(mobile): 请求拦截层（复用桌面端，message→Toast）"
```

---

## Task 4: 手写认证 service（去掉 getMenus）

**Files:**
- Create: `frontend-react-mobile/src/services/auth.ts`

> `access.ts` 已在 Task 1 原样复制，无需改动。

- [ ] **Step 1: 写 `src/services/auth.ts`**

Create `frontend-react-mobile/src/services/auth.ts`（基于桌面端 `frontend-react/src/services/auth.ts`，**移除 getMenus**——移动端不依赖菜单树）:

```typescript
/**
 * 认证相关接口（手写，移动端）
 *
 * 与桌面端 services/auth.ts 一致，差异：移除 getMenus（移动端硬编码路由，
 * getInitialState 只拉 userInfo）。登录页用的 unifiedLogin/captcha 取自
 * OpenAPI 生成的 services/aiperm/auth.ts（类型同源）。
 *
 * 响应已被 src/requestErrorConfig.ts 自动解包（R<T>.data）。
 */
import { request } from '@umijs/max';

/** 登录方式 */
export type LoginType = 'PASSWORD' | 'SMS' | 'EMAIL';

/** 用户信息（/auth/info 返回） */
export interface UserInfo {
  id: number;
  username: string;
  nickname?: string;
  avatar?: string;
  roles: string[];
  permissions: string[];
}

/** 统一登录请求体 */
export interface UnifiedLoginParams {
  loginType: LoginType;
  identifier: string;
  credential: string;
  imageCaptcha?: string;
  imageCaptchaKey?: string;
}

/** 登录页可用方式配置 */
export interface LoginConfig {
  loginTypes?: LoginType[];
  [k: string]: any;
}

/** 当前用户信息（含 roles / permissions） */
export async function getUserInfo() {
  return request<UserInfo>('/auth/info', { method: 'GET' });
}

/** 登出 */
export async function logout() {
  return request<void>('/auth/logout', { method: 'POST' });
}

/** 登录页可用方式 */
export async function getLoginConfig() {
  return request<LoginConfig>('/auth/login-config', { method: 'GET' });
}
```

- [ ] **Step 2: 类型检查**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm tsc`
Expected: 本文件无类型错误。

- [ ] **Step 3: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend-react-mobile/src/services/auth.ts
git commit -m "feat(mobile): 手写认证 service（getUserInfo/logout，去掉 getMenus）"
```

---

## Task 5: OpenAPI 生成 services/aiperm

**Files:**
- Generate: `frontend-react-mobile/src/services/aiperm/*.ts`（含 `user.ts`、`auth.ts`、`typings.d.ts` 等）

> `openapi.json` 与 `scripts/gen-openapi.mjs` 已在 Task 1 复制。脚本会把响应里的 R\<T\> 解包为内层 T，并按中文 Tag 映射英文文件名。

- [ ] **Step 1: 运行生成脚本**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm openapi`
Expected: 控制台输出 `✓ services 已生成到 src/services/aiperm`，可能伴随若干 `[openapi] 未映射的 Tag: xxx` 警告（无害，对应已移除的 IM/审批模块）。

- [ ] **Step 2: 验证产物**

Run: `ls /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile/src/services/aiperm/`
Expected: 列表含 `user.ts`、`auth.ts`、`typings.d.ts`、`index.ts` 等。

Run: `head -20 /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile/src/services/aiperm/user.ts`
Expected: 能看到 `export async function page(...)` 与 `request<API.PageResultUserVO>("/system/user", ...)`，与桌面端产物一致。

- [ ] **Step 3: 类型检查**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm tsc`
Expected: 生成产物自带 `// @ts-ignore` + `/* eslint-disable */`，无类型错误。

- [ ] **Step 4: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend-react-mobile/src/services/aiperm/
git commit -m "feat(mobile): OpenAPI 生成 services/aiperm（同源后端，类型与桌面端一致）"
```

---

## Task 6: 运行时配置 app.tsx

**Files:**
- Create: `frontend-react-mobile/src/app.tsx`

- [ ] **Step 1: 写 `src/app.tsx`**

Create `frontend-react-mobile/src/app.tsx`:

```typescript
/**
 * umi/max 运行时配置（移动端）
 *
 * - getInitialState：从 localStorage 读 token，已登录则拉用户信息（仅 userInfo，
 *   不拉菜单树——移动端硬编码路由）。
 * - request：接入 src/requestErrorConfig.ts。
 * - onRouteChange：登录守卫。替代桌面端 layout 插件的 onPageChange（本项目不开
 *   layout 插件，故用 umi core 的 onRouteChange）。
 * - rootContainer：注入 antd-mobile ConfigProvider（中文 locale）。
 *
 * 文档：https://umijs.org/docs/max/runtime-config
 */
import { history } from '@umijs/max';
import { ConfigProvider } from 'antd-mobile';
import zhCN from 'antd-mobile/es/locales/zh-CN';
import type { ReactNode } from 'react';
import {
  getToken,
  requestConfig,
  setToken,
  SILENT_BUSINESS_ERROR,
} from './requestErrorConfig';
import {
  getUserInfo,
  logout as doLogout,
  type UserInfo,
} from './services/auth';

const loginPath = '/user/login';

/** 全局吞掉「已被提示的业务错误」的未捕获 rejection（同桌面端） */
if (typeof window !== 'undefined') {
  window.addEventListener('unhandledrejection', (event) => {
    const reason = event.reason;
    if (
      reason &&
      typeof reason === 'object' &&
      (reason as any)[SILENT_BUSINESS_ERROR]
    ) {
      event.preventDefault();
    }
  });
}

/** 全局初始状态类型 */
export type InitialState = {
  currentUser?: UserInfo;
  fetchUserInfo?: () => Promise<UserInfo | undefined>;
  logout?: () => Promise<void>;
};

async function fetchUserInfo(): Promise<UserInfo | undefined> {
  try {
    return await getUserInfo();
  } catch {
    return undefined;
  }
}

/** 全局初始状态：有 token 才拉用户信息（不拉菜单） */
export async function getInitialState(): Promise<InitialState> {
  const logoutFn = async () => {
    try {
      await doLogout();
    } catch {
      // 忽略登出接口错误
    }
    setToken(null);
    history.push(loginPath);
  };

  const state: InitialState = {
    fetchUserInfo,
    logout: logoutFn,
  };

  if (getToken()) {
    state.currentUser = await fetchUserInfo();
  }

  return state;
}

/**
 * 登录守卫：未登录且访问非 /user/* 页面 → 跳登录页。
 * umi core 的 onRouteChange，不依赖 layout 插件。
 */
export function onRouteChange({
  location,
}: {
  location: { pathname: string };
}) {
  const pathname = location?.pathname;
  const onUserPage = !!pathname && pathname.startsWith('/user');
  if (!getToken() && !onUserPage) {
    history.replace(loginPath);
  }
}

/** 全局 request 配置（umi 运行时配置，类型由运行时匹配） */
export const request = {
  ...requestConfig,
};

/**
 * 根运行时：注入 antd-mobile ConfigProvider（中文 locale）。
 * 注：MFA 校验弹窗（MfaVerifyDialog）在 Phase 4 挂载。
 */
export function rootContainer(container: ReactNode): ReactNode {
  return <ConfigProvider locale={zhCN}>{container}</ConfigProvider>;
}
```

- [ ] **Step 2: 类型检查**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm tsc`
Expected: 本文件无类型错误。`onRouteChange` 的参数类型由 `@umijs/max` 的 `RuntimeConfigTypes` 提供。

- [ ] **Step 3: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend-react-mobile/src/app.tsx
git commit -m "feat(mobile): 运行时配置（getInitialState 简化 + onRouteChange 守卫 + ConfigProvider）"
```

---

## Task 7: 移动端布局 MobileLayout（TabBar）

**Files:**
- Create: `frontend-react-mobile/src/layouts/index.tsx`

> umi 约定：`src/layouts/index.tsx` 是全局布局，自动包裹所有路由（含登录页），不依赖 layout 插件。内部按路径决定 TabBar 显隐。

- [ ] **Step 1: 写 `src/layouts/index.tsx`**

Create `frontend-react-mobile/src/layouts/index.tsx`:

```typescript
/**
 * 移动端全局布局（umi 约定式 src/layouts/index.tsx）
 *
 * 关键点：umi 4 的 isLayout 全局 layout **不通过 children 传子路由**（children 为
 * undefined，用它会白屏），必须用 <Outlet/> 渲染当前匹配的路由组件。所有路由组件
 * 均为 React.lazy，<Outlet/> 须用 <Suspense> 包裹。
 * - 仅 4 个 TabBar 主路由显示底部 TabBar，其余全屏靠页面内 NavBar 返回。
 */
import {
  AppOutline,
  MessageOutline,
  UnorderedListOutline,
  UserOutline,
} from 'antd-mobile-icons';
import { TabBar } from 'antd-mobile';
import { Outlet, useLocation, history } from '@umijs/max';
import React, { Suspense } from 'react';

const TABS = [
  { key: '/dashboard', title: '首页', icon: <AppOutline /> },
  { key: '/apps', title: '应用', icon: <UnorderedListOutline /> },
  { key: '/message', title: '消息', icon: <MessageOutline /> },
  { key: '/profile', title: '我的', icon: <UserOutline /> },
];

const TAB_PATHS = new Set(TABS.map((t) => t.key));

const Layout: React.FC = () => {
  const location = useLocation();
  const showTabBar = TAB_PATHS.has(location.pathname);

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        height: '100vh',
        background: '#f5f7fa',
      }}
    >
      <div style={{ flex: 1, overflowY: 'auto', WebkitOverflowScrolling: 'touch' }}>
        <Suspense fallback={null}>
          <Outlet />
        </Suspense>
      </div>
      {showTabBar && (
        <div style={{ borderTop: '1px solid #eee', background: '#fff' }}>
          <TabBar
            activeKey={location.pathname}
            onChange={(key) => history.push(key)}
            safeArea
          >
            {TABS.map((t) => (
              <TabBar.Item key={t.key} icon={t.icon} title={t.title} />
            ))}
          </TabBar>
        </div>
      )}
    </div>
  );
};

export default Layout;
```

- [ ] **Step 2: 类型检查**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm tsc`
Expected: 本文件无类型错误（页面文件 Task 8 创建，届时可完整构建）。

- [ ] **Step 3: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend-react-mobile/src/layouts/index.tsx
git commit -m "feat(mobile): MobileLayout 布局（TabBar + 按路径显隐 + 安全区）"
```

---

## Task 8: 占位页面（Dashboard / Apps / Message / Profile / 404）

**Files:**
- Create: `frontend-react-mobile/src/pages/Dashboard/index.tsx`
- Create: `frontend-react-mobile/src/pages/Apps/index.tsx`
- Create: `frontend-react-mobile/src/pages/Message/index.tsx`
- Create: `frontend-react-mobile/src/pages/Profile/index.tsx`
- Create: `frontend-react-mobile/src/pages/404.tsx`

- [ ] **Step 1: 写 `src/pages/404.tsx`**

```typescript
import { Button, Result } from 'antd-mobile';
import { history } from '@umijs/max';
import React from 'react';

const NotFound: React.FC = () => (
  <Result
    status="error"
    title="页面不存在"
    description="你访问的页面走丢啦"
    style={{ padding: '64px 24px' }}
  />
);

export default NotFound;
```

> 注：`Result` 暂未配 `extra` 返回按钮；若用户直接访问 404 路径，靠浏览器后退。Phase 1 再完善。

- [ ] **Step 2: 写 `src/pages/Dashboard/index.tsx`**

```typescript
import { Card, NavBar } from 'antd-mobile';
import React from 'react';
import { useModel } from '@umijs/max';

const Dashboard: React.FC = () => {
  const { initialState } = useModel('@@initialState');
  const name = initialState?.currentUser?.nickname ||
    initialState?.currentUser?.username ||
    '用户';

  return (
    <>
      <NavBar back={null}>AIPerm</NavBar>
      <div style={{ padding: 12 }}>
        <Card title="工作台" style={{ marginBottom: 12 }}>
          <div style={{ fontSize: 16 }}>你好，{name} 👋</div>
          <div style={{ color: '#999', fontSize: 13, marginTop: 8 }}>
            移动端管理平台 · Phase 0 骨架
          </div>
        </Card>
        <Card title="提示">
          <div style={{ color: '#999', fontSize: 13 }}>
            工作台看板将在 Phase 4 完善统计数据。
          </div>
        </Card>
      </div>
    </>
  );
};

export default Dashboard;
```

- [ ] **Step 3: 写 `src/pages/Apps/index.tsx`**

```typescript
import { Grid, NavBar } from 'antd-mobile';
import { SetOutline, UserOutline } from 'antd-mobile-icons';
import { history } from '@umijs/max';
import React from 'react';

const APPS = [
  { key: '/system/user', text: '用户管理', icon: <UserOutline /> },
  // 其余模块入口随 Phase 2/3 增补
  { key: '/profile', text: '个人中心', icon: <SetOutline /> },
];

const Apps: React.FC = () => (
  <>
    <NavBar back={null}>全部应用</NavBar>
    <Grid columns={3} gap={8} style={{ padding: 12 }}>
      {APPS.map((a) => (
        <Grid.Item
          key={a.key}
          onClick={() => history.push(a.key)}
          style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '16px 0',
            background: '#fff',
            borderRadius: 8,
          }}
        >
          <div style={{ fontSize: 24, color: '#0060a9', marginBottom: 6 }}>
            {a.icon}
          </div>
          <div style={{ fontSize: 13 }}>{a.text}</div>
        </Grid.Item>
      ))}
    </Grid>
  </>
);

export default Apps;
```

- [ ] **Step 4: 写 `src/pages/Message/index.tsx`**

```typescript
import { Empty, NavBar } from 'antd-mobile';
import React from 'react';

const Message: React.FC = () => (
  <>
    <NavBar back={null}>消息中心</NavBar>
    <div style={{ padding: 48 }}>
      <Empty description="暂无消息（Phase 3 接入）" />
    </div>
  </>
);

export default Message;
```

- [ ] **Step 5: 写 `src/pages/Profile/index.tsx`**

```typescript
import { Avatar, Button, Dialog, List, NavBar, Toast } from 'antd-mobile';
import React from 'react';
import { useModel } from '@umijs/max';

const Profile: React.FC = () => {
  const { initialState, setInitialState } = useModel('@@initialState');
  const user = initialState?.currentUser;

  const handleLogout = async () => {
    const ok = await Dialog.confirm({
      content: '确定退出登录？',
      confirmText: '退出',
      cancelText: '取消',
    });
    if (!ok) return;
    try {
      await initialState?.logout?.();
      await setInitialState({ ...initialState, currentUser: undefined });
      Toast.show({ content: '已退出', icon: 'success' });
    } catch {
      // 忽略
    }
  };

  return (
    <>
      <NavBar back={null}>我的</NavBar>
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          padding: 24,
          background: '#fff',
        }}
      >
        <Avatar src={user?.avatar} style={{ '--size': '56px', marginRight: 16 }} />
        <div>
          <div style={{ fontSize: 17 }}>{user?.nickname || user?.username || '用户'}</div>
          <div style={{ color: '#999', fontSize: 13 }}>{user?.username}</div>
        </div>
      </div>
      <List style={{ marginTop: 12 }}>
        <List.Item description={user?.roles?.join('、') || '-'}>角色</List.Item>
        <List.Item description={`${user?.permissions?.length ?? 0} 项`}>权限数</List.Item>
      </List>
      <div style={{ padding: 24 }}>
        <Button block color="danger" onClick={handleLogout}>
          退出登录
        </Button>
      </div>
    </>
  );
};

export default Profile;
```

- [ ] **Step 6: 类型检查 + 构建验证**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm type-check`
Expected: 因登录页（Task 9）与用户列表页（Task 10）尚未创建，`config/routes.ts` 引用的 `./User/Login`、`./System/User` 会报「找不到模块」。**这两个模块在后续 Task 创建**。其余已创建文件本身无类型错误。

> 若想立即得到干净构建，可先创建 Task 9 / Task 10 的文件，再跑 type-check。本步只要已创建文件本身无错即可继续。

- [ ] **Step 7: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend-react-mobile/src/pages/
git commit -m "feat(mobile): 占位页面（工作台/应用宫格/消息/个人中心/404）"
```

---

## Task 9: 登录页（antd-mobile）

**Files:**
- Create: `frontend-react-mobile/src/pages/User/Login/index.tsx`

> 复用桌面端登录逻辑：拉图形验证码 → unifiedLogin（PASSWORD）→ 存 token → 整页跳转。验证码与 unifiedLogin 取自 OpenAPI 生成的 `services/aiperm/auth`（类型同源）。

- [ ] **Step 1: 写 `src/pages/User/Login/index.tsx`**

Create `frontend-react-mobile/src/pages/User/Login/index.tsx`:

```typescript
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
        layout="vertical"
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
                width={100}
                height={40}
                fit="contain"
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
```

- [ ] **Step 2: 确认生成的 auth service 导出**

Run: `grep -E "export (async )?function (captcha|unifiedLogin)" /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile/src/services/aiperm/auth.ts`
Expected: 输出包含 `unifiedLogin` 与 `captcha` 两个导出。若 `captcha` 函数名不同（如 `getCaptcha`），把 import 与调用处一并改为实际导出名。

- [ ] **Step 3: 类型检查**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm tsc`
Expected: 登录页无类型错误（只剩 Task 10 用户列表页未创建）。

- [ ] **Step 4: 运行时验证（首次端到端）**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm dev`
Expected:
- 服务起在 `http://localhost:8001`
- 浏览器访问 `http://localhost:8001/` → 自动重定向到 `/user/login`（onRouteChange 守卫生效）
- 登录页显示 logo、用户名/密码/验证码三项 + 验证码图片
- 用 `admin / admin123` + 正确验证码登录 → 跳转到 `/dashboard`，显示「你好，admin 👋」
- TabBar 四个入口可切换

> 后端需已启动（localhost:8080）。验证码图片正常加载说明 `/api/auth/captcha` 代理生效。

- [ ] **Step 5: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend-react-mobile/src/pages/User/
git commit -m "feat(mobile): antd-mobile 登录页（账号密码 + 图形验证码）"
```

---

## Task 10: 示范列表页（用户管理）

**Files:**
- Create: `frontend-react-mobile/src/pages/System/User/index.tsx`

> 用 OpenAPI 生成的 `page()` 拉用户列表，antd-mobile `List` 卡片化展示 + `PullToRefresh` 下拉刷新。验证 `requestErrorConfig` 的 R\<T\> 解包与 `PageResult.list/pageNum` 消费。完整 CRUD（筛选/详情/编辑）留到 Phase 1 通用组件。

- [ ] **Step 1: 写 `src/pages/System/User/index.tsx`**

Create `frontend-react-mobile/src/pages/System/User/index.tsx`:

```typescript
/**
 * 用户管理 - 移动端示范列表页（Phase 0）
 *
 * - 调用生成的 page() 拉用户分页，antd-mobile List 卡片化展示。
 * - PullToRefresh 下拉刷新。
 * - 完整筛选/详情/编辑/删除在 Phase 1（通用组件 MobileList/FilterPopup/FormPage）实现。
 *
 * 字段以生成的 API.UserVO 为准；status: 0=禁用 1=正常。
 */
import { List, NavBar, PullToRefresh, Tag, Toast } from 'antd-mobile';
import React, { useCallback, useEffect, useState } from 'react';
import { history } from '@umijs/max';
import { page as userPage } from '@/services/aiperm/user';

const UserList: React.FC = () => {
  const [users, setUsers] = useState<API.UserVO[]>([]);
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res: API.PageResultUserVO = await userPage({
        pageNum: 1,
        pageSize: 20,
      });
      setUsers(res?.list ?? []);
    } catch {
      // 业务错误已由请求拦截器 Toast 提示
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <>
      <NavBar onBack={() => history.back()}>用户管理</NavBar>
      <PullToRefresh onRefresh={load}>
        <List header={`共 ${users.length} 条（Phase 0 仅前 20 条）`}>
          {users.map((u) => (
            <List.Item
              key={u.id}
              description={
                <span style={{ fontSize: 12, color: '#999' }}>
                  {u.deptName || '未分配部门'}
                  {u.roleNames ? ` · ${u.roleNames}` : ''}
                </span>
              }
              extra={
                <Tag
                  color={u.status === 1 ? '#00b578' : '#ff3141'}
                  fill="outline"
                  style={{ '--border-radius': '4px' }}
                >
                  {u.status === 1 ? '正常' : '停用'}
                </Tag>
              }
            >
              {u.nickname || u.username}{' '}
              <span style={{ fontSize: 12, color: '#999' }}>{u.username}</span>
            </List.Item>
          ))}
        </List>
      </PullToRefresh>
    </>
  );
};

export default UserList;
```

- [ ] **Step 2: 确认生成的 user service 导出**

Run: `grep "export async function page" /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile/src/services/aiperm/user.ts`
Expected: 输出 `export async function page(`。返回类型为 `API.PageResultUserVO`。

- [ ] **Step 3: 类型检查**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm tsc`
Expected: 零错误。若 `u.status` / `u.deptName` / `u.roleNames` 报「属性不存在」，核对 `src/services/aiperm/typings.d.ts` 的 `UserVO` 定义并改用实际字段名（id/username/nickname 必然存在）。

- [ ] **Step 4: 运行时验证**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm dev`
Expected:
- 登录后 → TabBar「📱应用」→ 点「用户管理」→ 进入 `/system/user`
- NavBar 显示「用户管理」+ 返回箭头，无 TabBar
- 列表展示用户（昵称/用户名/部门/角色/状态 Tag）
- 下拉刷新触发重新加载

> 这验证了：OpenAPI 生成可用、`requestErrorConfig` 的 Authorization 注入与 R\<T\> 解包生效、`PageResult.list` 消费正确。

- [ ] **Step 5: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend-react-mobile/src/pages/System/
git commit -m "feat(mobile): 用户管理示范列表页（卡片列表 + 下拉刷新）"
```

---

## Task 11: 端到端验证与构建收尾

**Files:** 无新增（验证 + 构建）

- [ ] **Step 1: 全量类型检查**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm type-check`
Expected: 零错误。

- [ ] **Step 2: 生产构建**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm build`
Expected: 构建成功，产出 `dist/`，无 antd/pro-components 相关 chunk（确认未误打包桌面端 UI 库）。

验证未打包 antd：
Run: `ls /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile/dist/ | head`
Expected: 看到 `index.html` 与若干 `*.js`/`*.css`（带 hash）。

- [ ] **Step 3: E2E 手动验证清单**

启动后端（localhost:8080）后 `pnpm dev`，逐项验证：

- [ ] 访问 `http://localhost:8001/` → 重定向到 `/user/login`（未登录守卫）
- [ ] 登录页验证码图片正常加载（`/api/auth/captcha` 代理生效）
- [ ] `admin / admin123` + 正确验证码 → 跳转 `/dashboard`
- [ ] 首页显示「你好，admin 👋」
- [ ] TabBar 四入口（首页/应用/消息/我的）可切换，选中态高亮
- [ ] 「我的」显示用户信息，点「退出登录」→ 确认 → 回到登录页，localStorage token 被清
- [ ] 重新登录后，「应用」→「用户管理」→ 列表正常渲染
- [ ] 用户列表下拉刷新生效
- [ ] 浏览器 DevTools 切到移动设备视图（iPhone），布局无横向滚动、TabBar 固定底部、刘海屏无遮挡

- [ ] **Step 4: 收尾提交（若有遗漏文件）**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git status
# 若有未提交的改动（如 .umi 生成产物应已被 .gitignore 忽略）：
git add -A frontend-react-mobile/
git commit -m "chore(mobile): Phase 0 骨架收尾"
```

> `src/.umi/` 与 `dist/` 应被 `.gitignore` 忽略（Task 1 复制时已含）。若 `git status` 显示它们未被忽略，检查 `.gitignore` 是否正确复制。

---

## Phase 0 完成标准

✅ 独立项目 `frontend-react-mobile` 可 `pnpm dev` 起服务（端口 8001）
✅ 类型检查 `pnpm type-check` 零错误
✅ 生产构建 `pnpm build` 成功，未打包 antd/pro-components
✅ 登录 → 首页 → 应用宫格 → 用户列表 全链路跑通
✅ 登录守卫（onRouteChange）、请求拦截（R\<T\> 解包 + Authorization）、TabBar 导航、vw 适配均生效
✅ 每个 Task 有独立 commit，历史清晰

完成后进入 **Phase 1**：打磨通用组件 `<MobileList>`/`<FilterPopup>`/`<FormPage>`/`<DetailPanel>`，以用户管理跑通完整 CRUD（届时引入 Vitest 测试基础设施）。
