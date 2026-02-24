# aiperm - RBAC 权限管理系统

## 项目概述

**aiperm** 是一个 RBAC（基于角色的访问控制）权限管理系统，采用前后端分离架构：

| 层级 | 技术栈 |
|------|--------|
| 后端 | Spring Boot 3.5.11 + Java 21 + Sa-Token + MyBatis-Plus + MySQL + Redis |
| 前端 | Vue 3 + TypeScript + Vite 7 + Element Plus + Pinia + Orval |

## 常用开发命令

### 后端命令

```bash
# 启动开发服务器
./gradlew bootRun

# 构建项目
./gradlew build

# 运行测试
./gradlew test

# 清理构建
./gradlew clean
```

### 前端命令

```bash
cd frontend

# 安装依赖
pnpm install

# 生成 API 客户端（重要！）
pnpm run generate:api

# 启动开发服务器
pnpm run dev

# 构建生产版本
pnpm run build

# 预览生产版本
pnpm run preview
```

## 服务端点

| 服务 | 地址 |
|------|------|
| 后端 API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/api/swagger-ui.html |
| API Docs (OpenAPI) | http://localhost:8080/api/v3/api-docs |
| 前端开发服务器 | http://localhost:5173 |

## 数据库配置

| 数据库 | 连接信息 |
|--------|----------|
| MySQL | localhost:3306/aiperm (用户: root, 密码: root) |
| Redis | localhost:6379 |

默认管理员账号：`admin` / `admin123`

## 核心架构说明

### 后端分层架构

```
src/main/java/com/devlovecode/aiperm/
├── common/                     # 公共组件
│   ├── entity/                 # 基础实体（BaseEntity, R, PageResult）
│   ├── enums/                  # 枚举（ErrorCode）
│   ├── exception/              # 异常处理（BusinessException, GlobalExceptionHandler）
│   └── handler/                # 全局处理器
├── config/                     # 配置类
│   ├── SaTokenConfig.java      # Sa-Token 权限配置
│   ├── SaTokenInterface.java   # 权限接口实现
│   ├── MybatisPlusConfig.java  # MyBatis-Plus 配置
│   └── WebMvcConfig.java       # Web MVC 配置
└── modules/                    # 业务模块（按领域划分）
    ├── auth/                   # 认证模块
    │   ├── dto/request/        # 请求 DTO（LoginRequest）
    │   └── vo/                 # 视图对象（LoginVO, CaptchaVO）
    └── system/                 # 系统管理模块
        ├── entity/             # 实体（User, Role, Permission, Menu, Dept, Post）
        ├── mapper/             # MyBatis Mapper
        ├── service/            # 服务层接口和实现
        ├── controller/         # REST 控制器
        ├── dto/request/        # 请求 DTO
        ├── vo/                 # 视图对象
        └── converter/          # 对象转换器（MapStruct-Plus）
```

### 前端架构

```
frontend/src/
├── api/                        # API 层（Orval 自动生成）
│   ├── generated.ts            # 生成的 API 函数
│   └── index.ts                # API 导出
├── models/                     # TypeScript 类型定义（Orval 自动生成）
├── stores/                     # Pinia 状态管理
│   ├── user.ts                 # 用户状态（token 存储）
│   └── app.ts                  # 应用状态
├── router/                     # Vue Router 路由配置
├── views/                      # 页面视图
│   ├── dashboard/              # 仪表盘
│   ├── login/                  # 登录页
│   └── error/                  # 错误页（404）
├── components/                 # 公共组件
├── utils/                      # 工具函数
│   ├── api-mutator.ts          # Axios 请求拦截器
│   └── index.ts                # 工具导出
└── main.ts                     # 应用入口
```

## ⚠️ API 开发流程（重要！）

**前端绝对禁止手写 API 调用代码！**

必须遵循以下流程：

### 标准流程

1. **后端开发/修改 API 接口** → 在 Controller 中添加或修改接口
2. **启动后端服务** → `./gradlew bootRun`
3. **生成 API 客户端** → `cd frontend && pnpm run generate:api`
4. **查看生成的类型** → 检查 `frontend/src/models/` 中的 TypeScript 类型
5. **使用生成的 API** → 导入并使用生成的 API 函数

### 错误做法（❌）

```typescript
// 不要这样！缺乏类型安全
const response = await axios.get('/api/users')
const users = response.data // 类型为 any
```

### 正确做法（✅）

```typescript
// 1. 先生成 API
// cd frontend && pnpm run generate:api

// 2. 导入生成的类型和 API
import type { UserVO } from '@/models'
import { userControllerFindAll } from '@/api/generated'

// 3. 使用生成的 API（完全类型安全）
const { data } = await userControllerFindAll({ page: 1, pageSize: 10 })
// data 的类型自动推断为 UserVO[]
```

### 开发检查清单

在开发任何涉及 API 的功能前：

- [ ] 后端 API 是否已开发完成？
- [ ] 是否运行了 `pnpm run generate:api`？
- [ ] 是否查看了 `src/models/` 中的类型定义？
- [ ] 是否使用了生成的 API 函数？
- [ ] IDE 是否显示类型错误？

## 后端权限注解

使用 Sa-Token 注解控制接口权限：

```java
@SaCheckLogin                        // 检查用户是否登录
@SaCheckRole("admin")                // 检查用户是否具有 admin 角色
@SaCheckPermission("user:create")    // 检查用户是否具有 user:create 权限
@SaCheckPermission(value = {"user:update", "user:delete"}, mode = SaMode.OR)  // 满足任一权限
```

## 可用技能

### db-query：数据库只读查询

用于安全地执行数据库查询操作：

```bash
# 配置数据库连接（首次使用）
# 编辑 .claude/skills/db-query/assets/db-config.yaml

# 执行查询
python3 .claude/skills/db-query/scripts/query.py "SELECT * FROM sys_user LIMIT 5"

# 列出所有表
python3 .claude/skills/db-query/scripts/query.py --tables

# 查看表结构
python3 .claude/skills/db-query/scripts/query.py --schema sys_user
```

**安全限制**：只允许 SELECT、SHOW、DESCRIBE、EXPLAIN 操作

### ui-ux-pro-max：UI/UX 设计辅助

用于前端界面设计和开发，支持 Vue 3 + Element Plus 技术栈。

## 依赖版本

### 后端主要依赖

| 依赖 | 版本 |
|------|------|
| Spring Boot | 3.5.11 |
| Java | 21 |
| Sa-Token | 1.39.0 |
| MyBatis-Plus | 3.5.9 |
| MapStruct-Plus | 1.4.6 |
| Hutool | 5.8.34 |
| SpringDoc | 2.8.3 |

### 前端主要依赖

| 依赖 | 版本 |
|------|------|
| Vue | 3.5.x |
| TypeScript | 5.9.x |
| Vite | 7.3.x |
| Element Plus | 2.13.x |
| Pinia | 3.0.x |
| Vue Router | 5.0.x |
| Orval | 7.13.x |
| Axios | 1.13.x |

## 常见问题

### 前端 API 类型不匹配

**原因**：后端 API 已修改但前端未重新生成

**解决**：
```bash
cd frontend && pnpm run generate:api
```

### 无法连接数据库

**检查项**：
1. MySQL 服务是否启动
2. 数据库 `aiperm` 是否存在
3. 用户名密码是否正确（默认 root/root）

### Redis 连接失败

**检查项**：
1. Redis 服务是否启动
2. 端口是否正确（默认 6379）
3. 是否需要密码

### Sa-Token 登录失效

**检查项**：
1. Redis 连接是否正常
2. Token 是否过期（默认 30 天）
3. 请求头是否包含 `Authorization`
