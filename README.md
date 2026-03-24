# AiPerm

企业级 RBAC 权限管理系统，前后端分离：
- 后端：Spring Boot 4 + Sa-Token + JPA + Flyway + Redis
- 前端：Vue 3 + TypeScript + Vite + Element Plus

## 功能概览
- 账号认证：用户名密码 / 短信 / 邮箱 / OAuth
- 权限模型：用户、角色、菜单、部门、岗位、字典
- 安全能力：2FA、幂等、限流、操作日志、登录日志
- 企业功能：通知、消息、定时任务（动态调度）

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
1. 确认无明文密钥、密码、Token（已移除 `build.gradle` 的 Flyway 明文凭据）。
2. 检查 `application-dev.yaml` / `application-prod.yaml` 是否仅保留占位或安全默认值。
3. 确认 `.gitignore` 覆盖本地私有文件（`.env`、日志、IDE 文件）。
4. 跑一次质量门禁：
   - `cd backend && ./gradlew test`
   - `cd frontend && pnpm run check`

### docs 是否发布
- `docs/` 主要是内部设计与过程文档。
- 如果你的 GitHub 仓库是开源公开仓库，通常建议：
  - 要么不发布 `docs/plans/*`（避免暴露内部决策细节）；
  - 要么仅保留必要对外文档（如部署说明、API 说明）。

## 数据库导出建议（你问的重点）

结论：**不要导出并发布完整表数据**。

建议发布内容：
1. **必须**：表结构（schema）
2. **可选**：脱敏后的最小演示数据（seed）
3. **不要**：生产/真实用户数据、日志数据、手机号邮箱等隐私数据

MySQL 参考命令：
```bash
# 仅结构
mysqldump -u <user> -p --no-data aiperm > aiperm_schema.sql

# 仅数据（谨慎，先脱敏）
mysqldump -u <user> -p --no-create-info aiperm > aiperm_data.sql
```

更推荐：
- 用 Flyway 维护结构迁移（`backend/src/main/resources/db/migration`）
- 单独提供一份可公开的初始化脚本（管理员账号用弱默认并强制首次修改）

## 初始化 GitHub 发布（当前仓库未配置 remote）
```bash
git remote add origin <your-github-repo-url>
git push -u origin master
```

---
如需，我可以继续帮你做一版“公开仓库精简包”（自动筛掉 docs/plans、补充 LICENSE、补充 `.env.example`、发布前安全扫描脚本）。
