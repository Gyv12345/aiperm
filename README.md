# AiPerm

AiPerm 是一个 AI Native 的权限与代理基座项目，目标是让团队可以快速搭建“Agent + 权限控制 + 企业后台”一体化系统。

## 项目介绍

- 后端：Spring Boot 4 + Java 25 + Sa-Token + JPA + Flyway + Redis
- 前端：Vue 3 + TypeScript + Vite + Element Plus
- 架构方向：支持 GraalVM Native Image（后续可打包为原生二进制）

为什么强调 Spring Boot 最新技术栈 + Java 25 + 原生二进制：
- 启动更快，发布更灵活
- 内存占用更低，单机可承载更多实例
- 在相同机器资源下通常可以支撑更高并发密度

## 项目背景

这个项目最早由 GLM5 + Claude Code 协作生成，后续因为 GLM 在部分时段响应变慢，开发流程切换到 Codex 持续推进。当前仓库以“AI 协作开发”为主，目标是作为可复用基座。

我在实际业务里做过一个精简版：基于 newapi 做模型请求代理转发，中间通过 Java 服务判断用户是否有调用资格。这个开源项目就是在该实践之上，进一步抽象出来的通用版本。

## 核心思路

- Agent 尽量放在前端侧
- 用户自行购买并配置 API Token 到本地环境
- 服务端不托管用户大模型密钥，降低密钥泄露风险
- 后端负责权限、鉴权、审计、业务工具能力编排

## 现有能力

- 账号认证：用户名密码 / 短信 / 邮箱 / OAuth
- 权限模型：用户、角色、菜单、部门、岗位、字典
- 安全能力：2FA、幂等、限流、操作日志、登录日志
- 企业能力：通知、消息、定时任务（动态调度）

## 后续规划

- 前端 Agent 增加 Tool 能力，直接调用后台业务能力
- 引入 CLI 工作流（或以 Skill 体系先行）
- 逐步支持前后端 CRUD 代码自动生成

## 项目结构

```text
backend/    Spring Boot 服务
frontend/   Vue 3 管理端
docs/       设计与实现文档（内部资料，可按需公开）
```

## 本地开发

### 1) 启动后端

```bash
cd backend
./gradlew bootRun
```

### 2) 启动前端

```bash
cd frontend
pnpm install
pnpm run dev
```

或在仓库根目录：

```bash
./start-frontend.sh
```

### 3) 常用检查

```bash
# 后端
cd backend && ./gradlew test

# 前端
cd frontend && pnpm run type-check && pnpm run lint && pnpm run build
```

## 配置说明

- 默认配置：`backend/src/main/resources/application.yaml`
- 开发配置：`backend/src/main/resources/application-dev.yaml`
- 生产配置模板：`backend/src/main/resources/application-prod.yaml`

核心环境变量（建议通过 CI/CD 或部署平台注入）：
- `DB_URL` `DB_USERNAME` `DB_PASSWORD`
- `REDIS_HOST` `REDIS_PORT` `REDIS_PASSWORD` `REDIS_DATABASE`
- `SA_TOKEN_JWT_SECRET`（若启用 JWT 模式时）

Flyway 支持独立变量：
- `FLYWAY_URL` `FLYWAY_USER` `FLYWAY_PASSWORD`

## 发布到 GitHub 前（建议）

### 必做

1. 确认无明文密钥、密码、Token
2. 检查 `application-dev.yaml` / `application-prod.yaml` 是否仅保留占位或安全默认值
3. 确认 `.gitignore` 覆盖本地私有文件（`.env`、日志、IDE 文件）
4. 跑一次质量门禁
   - `cd backend && ./gradlew test`
   - `cd frontend && pnpm run check`

### docs 是否发布

`docs/` 里多数是内部设计和过程文档。公开仓库建议至少不发布 `docs/plans/*`，仅保留对外必要文档（部署说明、API 使用说明等）。

## 数据库导出建议

结论：不建议导出并发布完整表数据。

建议发布内容：
1. 必须：表结构（schema）
2. 可选：脱敏后的最小演示数据（seed）
3. 不要：生产/真实用户数据、日志数据、手机号邮箱等隐私数据

MySQL 参考命令：

```bash
# 仅结构
mysqldump -u <user> -p --no-data aiperm > aiperm_schema.sql

# 仅数据（谨慎，先脱敏）
mysqldump -u <user> -p --no-create-info aiperm > aiperm_data.sql
```

更推荐方式：
- 用 Flyway 维护结构迁移（`backend/src/main/resources/db/migration`）
- 单独提供可公开的初始化脚本

## 初始化 GitHub 发布

当前仓库未配置 remote，可按以下方式发布：

```bash
git remote add origin <your-github-repo-url>
git push -u origin master
```

## 公司与联系方式

- 公司：河南爱编程网络科技有限公司
- 官网与联系方式：<https://devlovecode.com>
