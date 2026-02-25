# aiperm - RBAC 权限管理系统

## 项目概述

**aiperm** 是一个 RBAC（基于角色的访问控制）权限管理系统，采用前后端分离架构。

| 层级 | 技术栈 |
|------|--------|
| 后端 | Spring Boot 3.5.11 + Java 21 + Sa-Token + MySQL + Redis |
| 前端 | Vue 3 + TypeScript + Vite 7 + Element Plus + Pinia + Orval |

## Quick Navigation

| 模块 | 说明 |
|------|------|
| [Backend Guidelines](backend/CLAUDE.md) | 后端开发规范、分层架构、权限注解、API 开发流程 |
| [Frontend Guidelines](frontend/CLAUDE.md) | 前端开发规范、组件结构、API 生成、状态管理 |

## 核心原则

1. **API 优先**：前端禁止手写 API，必须使用 Orval 生成
2. **权限控制**：所有接口必须加 `@SaCheckLogin`，写操作加 `@SaCheckPermission`
3. **操作日志**：每个写操作必须加 `@Log` 注解
4. **类型安全**：前后端全程类型安全，拒绝 `any`
5. **模块化**：按业务领域划分模块，遵循标准分层

## 常用命令

```bash
# 后端
cd backend && ./gradlew bootRun          # 启动开发服务器
cd backend && ./gradlew test             # 运行测试

# 前端
cd frontend && pnpm install              # 安装依赖
cd frontend && pnpm run generate:api     # 生成 API 客户端（重要！）
cd frontend && pnpm run dev              # 启动开发服务器
```

## 服务端点

| 服务 | 地址 |
|------|------|
| 后端 API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/api/swagger-ui.html |
| 前端开发服务器 | http://localhost:5173 |

## 数据库配置

| 数据库 | 连接信息 |
|--------|----------|
| MySQL | localhost:3306/aiperm (root/root) |
| Redis | localhost:6379 |

默认管理员：`admin` / `admin123`

## 可用技能

| 技能 | 触发场景 |
|------|----------|
| `aiperm-dev` | 后端业务模块开发、数据库设计 |
| `db-query` | 查询数据库、调试数据问题 |
| `ui-ux-pro-max` | 前端 UI/UX 设计、页面开发 |
| `sa-token-springboot` | 权限认证、登录/Token 相关 |

## 开发检查清单

### 新增 API 时

- [ ] 后端 Controller 加 `@SaCheckLogin`
- [ ] 写操作加 `@Log` + `@SaCheckPermission`
- [ ] 运行 `pnpm run generate:api` 生成前端 API
- [ ] 前端使用生成的 API 函数

### 新增业务模块时

- [ ] 创建 Flyway 迁移脚本 `Vx.x.x__xxx.sql`
- [ ] Entity 继承 `BaseEntity`
- [ ] Service 接口继承 `IService`
- [ ] 参照 `modules/system/entity/SysDictType.java` 作为模板

## 常见问题

| 问题 | 解决方案 |
|------|----------|
| 前端 API 类型不匹配 | `cd frontend && pnpm run generate:api` |
| 无法连接数据库 | 检查 MySQL 服务、数据库 `aiperm` 是否存在 |
| Redis 连接失败 | 检查 Redis 服务是否启动 |
| Sa-Token 登录失效 | 检查 Redis 连接、Token 是否过期 |
