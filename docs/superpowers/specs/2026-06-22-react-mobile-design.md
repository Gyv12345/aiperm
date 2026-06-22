# React 移动端模块设计（管理平台手机端）

- **日期**：2026-06-22
- **状态**：待评审
- **作者**：shichenyang
- **关联项目**：aiperm（RBAC 权限管理系统）

## 1. 背景与目标

aiperm 现有 `frontend-react`（UmiJS Max + Ant Design 5 + Pro Components）是**纯桌面端中后台**（ProLayout `mix` 布局、侧边栏 230px，无任何移动端/响应式代码）。本设计新增一套**移动端实现**，把管理平台搬到手机，复用后端 API、Sa-Token 鉴权与业务逻辑，但不复用桌面端 UI 组件。

### 需求决策汇总

| 维度 | 决定 | 说明 |
|------|------|------|
| 技术形态 | 移动端 Web（H5/PWA）+ antd-mobile | 浏览器/微信内访问，可加桌面，落地最快 |
| 功能范围 | 功能对等的移动后台 | 覆盖系统/企业/监控/消息/个人中心/工作台全部模块 |
| 工程结构 | 独立项目，同 repo 新目录 `frontend-react-mobile/` | 与桌面端平级，依赖/构建/部署独立 |
| 构建框架 | UmiJS Max | 与桌面端同框架，最大化复用 openapi/请求/鉴权基建 |
| 复用边界 | 后端 API · Sa-Token · OpenAPI · `requestErrorConfig` · `getInitialState`/`access` 模式 | 不复用 antd 组件、ProLayout、桌面端页面代码 |

### 核心原则

1. **桌面与移动是两套 UI 库**（antd vs antd-mobile），绝不打进同一 bundle → 用独立项目规避。
2. **最大化复用**：请求拦截、鉴权、权限模型、OpenAPI 类型同源，避免重复造轮子。
3. **功能对等 ≠ 1:1 复刻**：移动端优先保证「查 + 轻操作」，复杂批量/表格导出保留桌面端入口。

---

## 2. 整体架构与项目骨架

### 在 repo 中的位置

```
aiperm/
├── backend/                  # 后端（Sa-Token，一套 API 服务两端）
├── frontend/                 # Vue 桌面端（存量）
├── frontend-react/           # React 桌面端（umi + antd + ProLayout）
└── frontend-react-mobile/    # 🆕 React 移动端（umi + antd-mobile）
```

移动端与桌面端是**兄弟项目**：共享后端 API 与鉴权机制，UI/依赖/构建完全独立。

### 复用边界

| 复用 ✅ | 不复用 ❌ |
|---------|-----------|
| 后端 API（同一套接口） | antd 组件、ProLayout |
| Sa-Token 鉴权机制（token/401/423/MFA） | 桌面端页面代码 |
| OpenAPI 生成的 services（mobile 项目重新生成一份） | 桌面端布局/交互 |
| `requestErrorConfig.ts` 拦截逻辑（R\<T\> 解包、Authorization 头） | |
| `getInitialState` / `access.ts` 鉴权模式 | |

### 目录骨架（沿用 umi 约定）

```
frontend-react-mobile/
├── config/
│   ├── config.ts            # layout: false（不用 ProLayout）+ antd-mobile 按需 + vw 适配 + PWA
│   ├── routes.ts            # 移动端路由（TabBar 扁平 + 子页面）
│   └── proxy.ts             # 复用桌面端 /api 代理
├── src/
│   ├── app.tsx              # 运行时配置：getInitialState / request / rootContainer
│   ├── requestErrorConfig.ts # 从桌面端搬来，移动端微调（Toast 替换 message）
│   ├── access.ts            # 权限模型（原样复用）
│   ├── layouts/MobileLayout.tsx  # TabBar 布局 + 顶部 NavBar
│   ├── components/          # 移动通用件（MobileList/FilterPopup/FormPage/...）
│   ├── pages/
│   │   ├── User/Login/      # 移动登录页
│   │   ├── Dashboard/       # 工作台（卡片化看板）
│   │   ├── Apps/            # 功能宫格入口
│   │   ├── Message/         # 消息中心（TabBar 入口，对应桌面 Enterprise/Message）
│   │   ├── System/{User,Role,Menu,Dept,Post,Dict,File}/
│   │   ├── Enterprise/{Notice,Job,Config}/
│   │   ├── Monitor/{Online,Server,Cache,LoginLog,JobLog,OperLog}/
│   │   ├── Profile/         # 个人中心
│   │   └── 404/
│   └── services/aiperm/     # OpenAPI 重新生成（与桌面端同源）
├── public/
│   ├── manifest.json        # PWA manifest
│   └── icons/               # 移动端图标/启动图
├── openapi.json             # 同一份后端 OpenAPI
└── package.json             # 独立依赖（antd-mobile、postcss-px-to-viewport 等）
```

### 构建配置要点（`config/config.ts`）

- `layout: false`（关闭 ProLayout，自己搭 antd-mobile 导航）
- `antd-mobile` 按需引入（通过 umi 或 unplugin）
- `postcss-px-to-viewport`：375 设计稿基准，输出 vw
- `publicPath` 与路由 `base` 均为 `/m/`（部署路径前缀，见第 6 节）
- `hash: true`，`outputPath: 'dist'`，`npmClient: 'pnpm'`

---

## 3. 移动端导航与路由

### 导航范式

底部 **TabBar**（4 个主入口）+ 顶部 **NavBar**（标题 + 返回 + 右侧操作）+ 页面栈（push/pop）。不使用 ProLayout。

### TabBar 四入口

| Tab | 路径 | 内容 |
|-----|------|------|
| 🏠 首页 | `/dashboard` | 工作台看板（卡片化统计 + 待办/公告流） |
| 📱 应用 | `/apps` | 功能宫格（系统/企业/监控模块入口聚合） |
| ✉ 消息 | `/message` | 消息中心（对应桌面端 Enterprise/Message） |
| 👤 我的 | `/profile` | 个人中心 / 设置 / 退出 |

### 路由表

| 层级 | 路径 | 说明 | 布局 |
|------|------|------|------|
| 登录 | `/user/login` | 移动登录页 | `layout: false` |
| TabBar | `/dashboard` | 首页看板（`/` 重定向到此） | MobileLayout |
| TabBar | `/apps` | 功能宫格 | MobileLayout |
| TabBar | `/message` | 消息中心 + 公告 | MobileLayout |
| TabBar | `/profile` | 我的 | MobileLayout |
| 系统管理 | `/system/{user,role,menu,dept,post,dict,file}` | 列表页 | MobileLayout（隐藏 TabBar） |
| 详情/编辑 | `/system/user/detail/:id`、`/edit/:id` | 二级页面 | MobileLayout |
| 企业管理 | `/enterprise/{notice,job,config}` | | MobileLayout |
| 监控 | `/monitor/{online,server,cache,login-log,job-log,oper-log}` | | MobileLayout |
| 404 | `*` | | `layout: false` |

**进入功能模块的路径**：TabBar「📱应用」→ 功能宫格点「👤用户」→ `/system/user` 列表 → 点行进 `/system/user/detail/:id`。列表/详情/编辑页**隐藏 TabBar**（沉浸式操作），靠 NavBar 返回。

### 登录守卫（复用桌面端模式）

- `getInitialState`：读 localStorage token → 已登录则拉用户信息 + 权限标识
- `onPageChange`：未登录且访问非 `/user/*` → 跳 `/user/login`
- `access.ts`：按 Sa-Token permission 标识控制入口/按钮可见性（复用 `system:user:list` 这套）

### 菜单适配策略（关键决策）

桌面端靠后端菜单树（`/auth/menus`）渲染 ProLayout 侧边栏。**移动端不依赖后端菜单树**，改为前端硬编码扁平路由（TabBar + 宫格），用 `access.ts` 按 permission 控制**每个宫格入口与操作按钮**的显隐。

理由：移动端导航是扁平 2 层结构，不需要桌面端那套多级菜单树；硬编码路由更可控，权限仍走后端 permission 体系（一致）。

### 列表页范式（ASCII）

```
┌───────────────────────────┐
│  ≡  用户管理         🔍筛选 │ ← NavBar
├───────────────────────────┤
│  ↓ 下拉刷新                │
│ ┌───────────────────────┐ │
│ │张三  admin        ⋯    │ │ 点行 → /detail/:id
│ │普通用户 · 正常          │ │ 左滑 → [编辑][删除]
│ └───────────────────────┘ │
│ ┌───────────────────────┐ │
│ │李四  lisi   · 停用      │ │
│ └───────────────────────┘ │
│        ↑ 上拉加载更多      │
├───────────────────────────┤
│ 🏠首页 📱应用 ✉消息 👤我的  │ ← TabBar
└───────────────────────────┘
```

---

## 4. 鉴权 / 请求层 / API 生成

### 复用清单

| 文件 | 处理 | 说明 |
|------|------|------|
| `src/access.ts` | ✅ **100% 原样搬** | `canUser` / `canPermission('system:user:create')` / `canAdmin` 等模型零改动 |
| `src/services/auth.ts` | ✅ **原样搬** | `unifiedLogin`/`login`/`logout`/`getUserInfo`/`getCaptcha`/`getLoginConfig` 全复用 |
| `src/services/aiperm/*` | ♻️ **OpenAPI 重新生成** | 复用 `scripts/gen-openapi.mjs` + 同一份 `openapi.json`，mobile 项目生成一份 |
| `src/requestErrorConfig.ts` | 🔧 **搬来 + 2 处改** | 见下 |
| `src/app.tsx` 的 `getInitialState` | 🔧 **搬来 + 1 处简化** | 见下 |

### `requestErrorConfig.ts` 的两处改动

桌面端逻辑（R\<T\> 解包 / Authorization 裸 token / 401 跳登录 / 423 MFA / 403 / 业务码提示 / `SILENT_BUSINESS_ERROR` / `baseURL:'/api'` timeout 30000）**全部保留**，只动两处：

1. **提示组件**：`import { message } from 'antd'` → antd-mobile 的 `Toast.show({ content, icon })`。`message.error/warning/loading` 对应替换为 `Toast`。这是唯一的库替换。
2. **MFA 弹窗载体**：401/423 的处理逻辑不变（`window.dispatchEvent('mfa-required')`），监听端从桌面的 `MfaVerifyModal`(antd) 换成移动端新写的 `MfaVerifyDialog`(antd-mobile `Dialog`)。事件协议复用。

> `redirectToLogin` 跳 `/user/login?redirect=...`、防重入、`TOKEN_KEY='token'`、localStorage 读写——全部原样。

### `getInitialState` 的简化（重要优化）

桌面端拉 `getUserInfo()` + `getMenus()`。**移动端只拉 `getUserInfo()`，不拉 `getMenus()`**——因为移动端硬编码路由、不依赖后端菜单树。少一次请求，更快进入首屏。`UserInfo.permissions` 仍是 `access.ts` 的数据源，权限控制不受影响。

### Token / 登录态

- 移动端是**独立 origin**（部署路径 `/m/`，见第 6 节），localStorage 天然隔离 → 移动端**独立登录**，符合移动端 UX 预期。
- 登录接口、token 机制、MFA 流程与桌面端**完全一致**，后端零改动。
- 登录页支持方式复用 `getLoginConfig` 返回的 `loginTypes`（PASSWORD/SMS/EMAIL）。移动端可将 SMS（短信验证码）做成更显眼入口（UX 细节，实现时定）。

### 数据流

```
移动端登录 → POST /auth/unified-login → token 存 localStorage
                                          │
getInitialState ──读 token──> GET /auth/info ──> { permissions } ──> access.ts
                                          │
request 拦截器: Authorization: <token> ──> 后端校验 ──> R<T> 解包
  401 → 跳 /user/login     423 → mfa-required 事件 → MfaVerifyDialog
```

---

## 5. 移动端交互体系

### 范式映射：桌面 → 移动

| 桌面端（antd） | 移动端（antd-mobile） | 说明 |
|----------------|----------------------|------|
| `Table` + 分页器 | 卡片化 `List` + `InfiniteScroll` + `PullToRefresh` | 行变卡片，下拉刷新、上拉加载更多替代分页 |
| 顶部 `Form` 筛选条 | NavBar 右侧「筛选」→ `Popup` 弹层 | 表单进抽屉，底部重置/确定 |
| `Modal` 详情/编辑 | **独立路由页**（push）+ NavBar 返回 | 沉浸式，不叠浮层 |
| `Modal` 表单 | 独立 `Form` 页 + 底部固定提交 | 卡片分组输入项 |
| `Popconfirm` 删除 | `Dialog.confirm` | 模态确认 |
| 行操作按钮 | `SwipeAction`（左滑出操作）+ `⋮` 更多 | 手势优先 |
| 多列复杂表格 | 关键字段卡片化 / 横向滚动 | 监控指标、日志类用卡片 + 标签 |

### 页面范式（ASCII）

**筛选弹层 / 表单页 / 详情页**：

```
筛选 Popup            新增/编辑表单页         详情页
┌──────────────┐    ┌──────────────────┐    ┌──────────────────┐
│ 筛选      ✕   │    │ ✕ 新增用户   保存  │    │ ≡ 用户详情  [编辑]│
│ 用户名[__]   │    ├──────────────────┤    ├──────────────────┤
│ 状态  [正常▾]│    │ ┌基本信息────────┐│    │ 👤 张三           │
│ 部门  [研发▾]│    │ │用户名*[______] ││    │   admin·普通用户  │
│ 时间 [— 至 —]│    │ │昵称  [______]  ││    │ ┌──────────────┐ │
│              │    │ └───────────────┘│    │ │手机 138****  │ │
│ [重置][确定] │    │ ┌角色权限────────┐│    │ │部门 研发部    │ │
└──────────────┘    │ │角色 [管理员 ▾] ││    │ │状态 正常      │ │
                    │ └───────────────┘│    │ └──────────────┘ │
                    │    [  提 交  ]    │    │ [重置密码][删除]  │
                    └──────────────────┘    └──────────────────┘
```

### 抽象通用组件（`src/components/`）

「功能对等」不失控的关键——一次写好，几十个模块复用：

| 组件 | 职责 |
|------|------|
| `<MobileList>` | 卡片列表 + 下拉刷新 + 无限滚动 + 空态/加载态，props 透传 service 与卡片渲染 |
| `<FilterPopup>` | 筛选弹层壳，底部重置/确定，受控 form |
| `<FormPage>` | 表单页壳，卡片分组 + 底部固定提交 + 校验 |
| `<DetailPanel>` | 详情页字段展示（List + 键值对） |
| `<SwipeActions>` | 左滑操作（编辑/删除 + 权限包裹） |
| `<PermButton>` / `<Access>` | 权限包裹，复用 `access.canPermission` |
| `<PageResult>` 消费 | 统一用 `result.list` / `result.pageNum`，内置到 `<MobileList>`，杜绝 records/page 错误 |

### 状态管理

开启 umi 自带 `model` 插件，用 `useModel('@@initialState')` 共享 `currentUser`/权限。**不引入 zustand/redux**（与桌面端一致，复用最大化）。

---

## 6. PWA / 部署 / 测试 / 实施分期

### PWA 策略（轻量起步）

第一版做**轻量 PWA**，不追求离线（后台数据实时性要求高）：

| 项 | 第一版 | 后续 |
|----|--------|------|
| `manifest.json`（图标/启动图/全屏/主题色） | ✅ 做 | |
| 静态资源 Service Worker（网络优先 + fallback 缓存） | ✅ 做 | |
| 「加到主屏幕」 | ✅ 支持 | |
| 业务数据离线缓存 | ❌ 不做 | 按需补 |

> 定位是「可安装的 H5」，给原生感的启动体验，但不做离线业务。

### 部署（与桌面端共存）

- **独立部署、同域路径前缀**（最省事，无需额外域名/证书）：
  - 桌面端：`/` → `frontend-react/dist`
  - 移动端：`/m/` → `frontend-react-mobile/dist`
- 移动端 `publicPath` + 路由 `base` 均为 `/m/`，`/m/user/login` 为入口。
- 后端 API 同一套（`/api` 代理不变），Sa-Token 跨域已支持。
- 入口引流：主站可加轻量 UA 检测，手机访问时顶部提示「前往移动版 `/m/`」（不强制跳转，保留用户选择）。

### 测试

| 层 | 工具 | 范围 |
|----|------|------|
| 类型检查 | `max setup && tsc --noEmit` | 全量，CI 必跑 |
| 构建 | `max build` | 全量，CI 必跑 |
| 组件单测 | Vitest（可选） | 通用组件 `<MobileList>` 等 |
| 真机调试 | Chrome 设备模拟 + 真机扫码 | `max dev` 监听 `0.0.0.0` |
| E2E | Playwright（项目已装） | 核心链路，后续补 |

### 实施分期

| Phase | 目标 | 验收 |
|-------|------|------|
| **0. 骨架** | 独立项目 + umi/antd-mobile + vw 适配 + MobileLayout + TabBar + 登录 + requestErrorConfig + access + OpenAPI 生成 + 示范列表页 | 能登录、进首页、看一个列表 |
| **1. 通用组件 + 示范模块** | 打磨 `<MobileList>`/`<FilterPopup>`/`<FormPage>`/`<DetailPanel>`，以**用户管理**跑通列表/筛选/详情/新增/编辑/删除全链路 | 用户管理移动版完整可用 |
| **2. 系统管理** | 角色/菜单/部门/岗位/字典/文件 | 系统模块全移动化 |
| **3. 企业 + 监控 + 消息** | 公告/消息/任务/配置 + 在线/服务器/缓存/三类日志 | 后台模块全移动化 |
| **4. 看板 + 个人中心 + PWA + 部署** | 工作台看板卡片化 + 个人中心 + MFA 弹窗 + PWA + Nginx 部署联调 | 整体上线 |

> 每个 Phase 独立可验收，Phase 0 结束即有能跑的移动端 demo。

### 风险与对策

| 风险 | 对策 |
|------|------|
| antd 与 antd-mobile 不可同项目混用 | 已由「独立项目」规避 |
| 桌面端部分接口为表格/导出专用（如 Excel 导出） | 移动端按需取舍，不做复杂导出 |
| vw 适配基准 | 统一 375 设计稿，`postcss-px-to-viewport` |
| iPhone 安全区（刘海/底部条） | antd-mobile `SafeArea` + CSS `env(safe-area-inset-*)` |
| 功能对等范围蔓延 | 移动端优先「查 + 轻操作」，复杂批量/表格保留桌面端 |

---

## 7. 后续步骤

本设计评审通过后，进入实现计划阶段（`superpowers:writing-plans`），按 Phase 0 → 4 逐步落地。第一个实现计划聚焦 **Phase 0（项目骨架）**。
