# AIPerm · React + Ant Design Pro 前端

AIPerm（权限结构总览）的 React 版前端，基于 **umi/max（Ant Design Pro 官方栈）**。与现有 Vue 3 版 `frontend/` 并列，后端无需改动。

## 技术栈

- **umi/max 4** + **@ant-design/pro-components** + **antd 5**
- 运行时配置：getInitialState（鉴权/用户/菜单）+ Access（权限）
- 请求层：基于 @umijs/max request，自动解包后端统一响应 `R<T>`，注入 Sa-Token（`Authorization` 无 Bearer 前缀）
- API 客户端：`@umijs/openapi` 从后端 `/v3/api-docs` 生成 `src/services/aiperm/`

## 目录结构

```
frontend-react/
├── config/
│   ├── config.ts          # umi 主配置
│   ├── routes.ts          # 路由（含 access 权限字段）
│   └── proxy.ts           # dev 代理 /api → http://localhost:8080
├── public/logo.svg
├── scripts/gen-openapi.mjs # OpenAPI → services 生成脚本（含中文 Tag→英文、R<T> 解包）
├── openapi.json           # 后端 /v3/api-docs 离线备份（提交入仓）
├── src/
│   ├── app.tsx            # 运行时配置：getInitialState / request / layout / MFA 挂载
│   ├── access.ts          # 权限函数（canPermission/canRole 等）
│   ├── requestErrorConfig.ts # 请求/响应拦截器：R<T> 解包、401 重登、423 MFA
│   ├── common.d.ts        # R<T> / PageResult<T> / PageParams 全局类型
│   ├── components/
│   │   ├── AccessButton/  # 权限按钮
│   │   └── MfaVerifyModal/# 全局 MFA 校验弹窗（监听 423 事件）
│   ├── services/
│   │   ├── aiperm/        # OpenAPI 生成（英文文件名，按后端模块分）
│   │   └── auth.ts        # 手写：登录/信息/菜单树（业务类型更贴合）
│   └── pages/
│       ├── Dashboard/     # 工作台（统计卡片）
│       ├── User/Login/    # 登录（密码 + 图形验证码）
│       ├── System/User/   # 用户管理（ProTable + CRUD + 重置密码/状态）
│       ├── System/Role/   # 角色管理（ProTable + CRUD + 分配菜单树）
│       ├── System/Menu/   # 菜单管理（树形 ProTable + CRUD）
│       ├── Profile/       # 个人中心（信息/改密/登录日志）
│       ├── Placeholder/   # 长尾模块占位
│       └── 404.tsx
```

## 快速开始

```bash
# 1. 安装依赖
pnpm install

# 2. 启动后端（需在 http://localhost:8080 运行，见 backend/）

# 3. 启动前端 dev
pnpm dev
```

访问 http://localhost:8000 ，使用后端已有账号登录。

## 命令

| 命令 | 说明 |
| --- | --- |
| `pnpm dev` | 启动 dev 服务（端口 8000，代理 /api → 8080） |
| `pnpm build` | 生产构建 |
| `pnpm tsc` | TypeScript 类型检查（**每次改动后必须执行**） |
| `pnpm openapi` | 用仓库内 `openapi.json` 重新生成 services |
| `pnpm openapi:fetch` | 从运行中的后端拉取最新 spec 并重新生成 |

## 后端对接约定

- **鉴权**：Sa-Token，请求头 `Authorization` = 裸 token（**无 `Bearer ` 前缀**），登录后存 localStorage（key `token`）。
- **统一响应** `R<T> = { code, message, data }`：响应拦截器自动解包 `data`，页面直接拿到业务数据。成功码 `code===200`（兼容 `0`）。
- **分页** `PageResult<T> = { total, list, pageNum, pageSize, pages }`（注意是 `list`/`pageNum`，非 `records`/`current`）。
- **HTTP 状态码**：`401` → 清 token 跳登录；`423` → 派发全局事件触发 MFA 弹窗；`403/500` 提示；`404` 静默。
- **动态菜单**：`/auth/menus` 返回树形菜单（`menuType`：M=目录 / C=菜单 / F=按钮），`menuDataRender` 渲染侧边栏。
- **按钮权限**：`<Access accessible={access.canPermission('system:user:create')}>`。

## 重新生成 API 客户端

后端接口变更后：

```bash
# 方式 A：后端在运行（推荐，拿到最新 spec）
pnpm openapi:fetch

# 方式 B：手动替换 openapi.json 后生成
pnpm openapi
```

生成脚本 `scripts/gen-openapi.mjs` 做了两项定制：
1. 把中文 `@Tag` 映射为英文文件名（`用户管理` → `user.ts`）。
2. 把响应类型 `R<T>` 解包为内层 `T`，使生成代码的返回类型与拦截器自动解包后的运行时值一致。

> 注意：`/auth/menus`、`/system/menu/tree` 等返回 `List<T>` 包装的接口未被自动解包（内层是匿名数组），调用处用 `(res as any)` 或取 `.data` 兜底。

## 迁移进度

| 模块 | 状态 |
| --- | --- |
| 登录 / 鉴权 / 动态菜单 / 权限 / MFA | ✅ 已完成 |
| 工作台 Dashboard | ✅ 已完成 |
| 系统管理：用户 / 角色 / 菜单 | ✅ 已完成 |
| 个人中心 | ✅ 已完成 |
| 部门 / 岗位 / 字典 / 审批 / 企业 / 监控 / 日志 | ⏳ 占位中，接口已生成，待页面迁移 |
