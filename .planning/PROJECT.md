# aiperm Page-Agent 工具增强

## What This Is

为 aiperm 前端的 page-agent 智能助手增加后端 API 调用工具，使 AI 能够通过自然语言直接查询和操作系统数据（用户、角色、菜单等），而不仅仅是填写表单。工具复用前端 Orval 生成的 API 函数，危险写操作需用户确认后执行。

## Core Value

AI 助手能直接调用后端 API 完成系统管理操作，用户只需用自然语言描述需求。

## Requirements

### Validated

- ✓ Page-agent 框架已集成（`FormPageAgent.vue`）— existing
- ✓ 2 个记忆工具（record_workflow, remember_page_note）— existing
- ✓ 页面记忆系统（localStorage 持久化）— existing
- ✓ Orval 自动生成后端 API 函数（`src/api/generated.ts`）— existing
- ✓ 认证 Token 自动注入（Axios 拦截器）— existing
- ✓ 后端 RBAC 权限控制（Sa-Token）— existing

### Active

- [ ] 系统数据查询工具 — 查询用户列表/详情、角色列表、菜单树、部门树、岗位列表等
- [ ] 系统数据写入工具 — 创建/更新/删除用户、角色、菜单等，需用户确认
- [ ] 日志与监控查询工具 — 查询操作日志、登录日志
- [ ] 辅助数据查询工具 — 查询字典数据、字典类型等
- [ ] 危险操作确认机制 — 写入/删除操作弹出确认对话框，用户同意后才执行
- [ ] 工具注册到 page-agent — 所有工具通过 `customTools` 配置注册
- [ ] 工具描述优化 — 清晰的工具描述让 AI 理解何时使用哪个工具

### Out of Scope

- 后端 Agent 模块重构 — 已移除，不在本次范围
- 独立 HTTP 请求 — 工具统一复用前端生成 API
- 短信验证码功能 — 已有 TODO，独立需求
- 数据导出功能 — 独立需求

## Context

- 现有 `FormPageAgent.vue` 位于 `frontend/src/components/agent/`
- 使用 `page-agent` 库（v1.6.1）的 `tool()` 函数注册自定义工具
- 工具输入用 `zod/v4` 定义 schema
- 后端 API 全部通过 Orval 生成到 `frontend/src/api/generated.ts`
- 前端请求自动携带 Token（Axios 拦截器）
- 后端写操作有 `@SaCheckPermission` 权限控制

## Constraints

- **Tech Stack**: 必须使用 `page-agent` 的 `tool()` API 和 `zod` schema
- **API 调用**: 必须复用 Orval 生成的 API 函数，禁止手写 HTTP 请求
- **安全**: 写入操作必须有用户确认步骤，不能静默执行
- **类型安全**: 所有工具的输入输出必须类型安全，禁止 `any`
- **组件大小**: `FormPageAgent.vue` 已有 731 行，工具定义应拆分到独立文件

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| 复用 Orval 生成 API | 已有类型安全、Token 注入、拦截器 | — Pending |
| 工具拆分到独立文件 | FormPageAgent.vue 已 731 行，避免继续膨胀 | — Pending |
| 危险操作需确认 | 防止 AI 误操作导致数据丢失 | — Pending |
| 使用 zod 定义 schema | page-agent 已依赖 zod，保持一致 | — Pending |

---
*Last updated: 2026-03-25 after initialization*
