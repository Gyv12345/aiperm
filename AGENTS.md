# 仓库指南

## 语言约定
- 默认使用中文进行沟通、说明与评审反馈。
- 代码、命令、路径、配置键名、API 名称保持原文（通常为英文）。
- 仅在用户明确要求时切换为英文。

## 项目结构与模块组织
本仓库采用前后端分离结构，核心目录如下：
- `backend/src/main/java/com/devlovecode/aiperm/`：Spring Boot 业务模块（`modules/*`）与公共基础设施（`common/*`、`config/*`）。
- `backend/src/main/resources/db/migration/`：Flyway 数据库迁移脚本（命名：`Vx.x.x__description.sql`）。
- `backend/src/test/`：JUnit 集成测试与 `application-test.yaml`。
- `frontend/src/`：Vue 3 应用（`views/`、`components/`、`stores/`、`composables/`、`router/`、`utils/`）。
- `frontend/src/api/generated.ts`、`frontend/src/models/`：Orval 自动生成的 API 客户端与类型。
- `docs/`：设计文档与实施计划。

## 构建、测试与开发命令
- 启动后端开发服务：`cd backend && ./gradlew bootRun`
- 后端构建：`cd backend && ./gradlew build`
- 后端测试：`cd backend && ./gradlew test`
- 安装前端依赖：`cd frontend && pnpm install`
- 启动前端开发服务：`cd frontend && pnpm run dev`（或在仓库根目录执行 `./start-frontend.sh`）
- 前端质量检查：`cd frontend && pnpm run type-check && pnpm run lint && pnpm run build`
- 后端接口变更后重新生成前端 API：`cd frontend && pnpm run generate:api`

## 编码风格与命名规范
- Java 使用 4 空格缩进，基于 Java 25，优先构造器注入（`@RequiredArgsConstructor`）。
- 后端分层命名：`SysXxx`（Entity）、`XxxRepository`、`XxxService`、`SysXxxController`、`XxxDTO`、`XxxVO`。
- Vue/TypeScript 使用 2 空格缩进，采用 `<script setup lang="ts">`，组件文件使用 PascalCase，组合式函数以 `use` 开头。
- 提交前执行 ESLint（`frontend/eslint.config.js`）；已生成 API 存在时，避免手写重复请求代码。

## 测试规范
- 后端测试基于 Spring Boot + JUnit 5，测试文件放在 `backend/src/test/java`，命名以 `*Test` 结尾。
- 执行 `./gradlew test` 依赖本地测试环境（至少 MySQL `aiperm_test`，部分鉴权路径可能依赖 Redis）。
- 前端当前主要通过 CI 静态质量门禁（`type-check`、`lint`、`build`）。

## 提交与 Pull Request 规范
- 提交信息遵循仓库既有 Conventional Commit 风格：`feat(scope): ...`、`fix: ...`、`docs: ...`、`refactor: ...`。
- 单次提交应聚焦单一变更；涉及迁移脚本或 API 生成产物时，与功能代码同 PR 提交。
- PR 建议包含：变更摘要、影响范围（`backend`/`frontend`）、测试命令与结果、关联 Issue、UI 改动截图。

## 安全与配置提示
敏感信息通过环境变量注入（如 `DB_*`、`REDIS_*`、`SA_TOKEN_JWT_SECRET`），禁止提交真实凭据。涉及角色与权限修改时，重点校验 data scope 逻辑，避免越权。
