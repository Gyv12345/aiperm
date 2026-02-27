# aiperm - RBAC 权限管理系统

## 重要
宿主自己启动前端和后端，自己测试，开发过程中可以调用编译查看报错，但是不用帮助宿主重启服务

## 项目概述

**aiperm** 是一个 RBAC（基于角色的访问控制）权限管理系统，采用前后端分离架构。

| 层级 | 技术栈 |
|------|--------|
| 后端 | Spring Boot 3.5.11 + Java 21 + Sa-Token + MySQL + Redis |
| 前端 | Vue 3 + TypeScript + Vite 7 + Element Plus + Pinia |

## Quick Navigation

| 模块 | 说明 |
|------|------|
| [开发流程](.claude/rules/development-workflow.md) | **功能模块开发 5 阶段流程** |
| [Backend Guidelines](backend/CLAUDE.md) | 后端开发规范、分层架构、权限注解、API 开发流程 |
| [Frontend Guidelines](frontend/CLAUDE.md) | 前端开发规范、组件结构、API 生成、状态管理 |

## 核心原则

1. **开发流程**：需求分析 → 数据库 → 后端 → 前端 → 测试联调
2. **API 生成**：前端禁止手写 API，必须使用 `aiperm-api-generator` 技能生成
3. **权限控制**：所有接口必须加 `@SaCheckLogin`，写操作加 `@SaCheckPermission`
4. **操作日志**：每个写操作必须加 `@Log` 注解
5. **类型安全**：前后端全程类型安全，拒绝 `any`
6. **前后端规范一致**：接口字段名必须与后端完全一致

## ⚠️ 前后端接口规范（重要！）

**前后端字段名必须保持一致！**

### 分页结果 PageResult

后端 `PageResult<T>` 返回格式：
```json
{
  "total": 100,
  "list": [...],
  "pageNum": 1,
  "pageSize": 10,
  "pages": 10
}
```

前端类型定义（`src/types/index.ts`）：
```typescript
export interface PageResult<T> {
  total: number
  list: T[]        // 注意：是 list，不是 records
  pageNum: number  // 注意：是 pageNum，不是 page
  pageSize: number
  pages: number
}
```

### 常见错误

| 错误 | 正确 |
|------|------|
| `result.records` | `result.list` |
| `result.page` | `result.pageNum` |

### 检查清单

- [ ] 前端类型定义与后端返回字段名完全一致
- [ ] 分页数据使用 `result.list` 而非 `result.records`
- [ ] 页码使用 `result.pageNum` 而非 `result.page`

## 常用命令

```bash
# 后端
cd backend && ./gradlew bootRun          # 启动开发服务器
cd backend && ./gradlew test             # 运行测试

# 前端
cd frontend && pnpm install              # 安装依赖
cd frontend && pnpm run dev              # 启动开发服务器
```

## 前端 API 生成

**禁止手写 API！** 使用 `aiperm-api-generator` 技能：

```
/aiperm-api-generator
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

### 新增业务模块时

- [ ] 创建 Flyway 迁移脚本 `Vx.x.x__xxx.sql`
- [ ] Entity 继承 `BaseEntity`
- [ ] Repository 继承 `BaseRepository`
- [ ] DTO 使用 `@JsonView` 分组验证
- [ ] Controller 加 `@SaCheckLogin`，写操作加 `@Log` + `@SaCheckPermission`
- [ ] 使用 `aiperm-api-generator` 技能生成前端 API
- [ ] 参照 `modules/system/dict/` 作为模板

### 新增 API 时

- [ ] 后端 Controller 加 `@SaCheckLogin`
- [ ] 写操作加 `@Log` + `@SaCheckPermission`
- [ ] 使用 `aiperm-api-generator` 技能生成前端 API
- [ ] 前端使用生成的 API 函数

## 常见问题

| 问题 | 解决方案 |
|------|----------|
| 前端 API 类型不匹配 | 使用 `/aiperm-api-generator` 技能重新生成 |
| 无法连接数据库 | 检查 MySQL 服务、数据库 `aiperm` 是否存在 |
| Redis 连接失败 | 检查 Redis 服务是否启动 |
| Sa-Token 登录失效 | 检查 Redis 连接、Token 是否过期 |
| 分页数据为空 | 检查是否使用 `result.list`（不是 `records`）|
