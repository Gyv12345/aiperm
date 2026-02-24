---
name: aiperm-dev
description: aiperm RBAC权限管理系统功能开发技能，专门用于开发基于aiperm框架的业务功能。当需要进行功能开发、模块创建、业务逻辑实现、数据库设计、API接口开发时激活。触发关键词：功能开发、模块开发、业务实现、新增功能、功能模块、开发功能、业务功能、创建模块、权限管理、菜单管理、角色管理、用户管理、字典管理、部门管理、岗位管理、Controller开发、Service开发、Mapper开发、Entity开发、DTO开发、VO开发、API接口开发、REST接口、数据库设计、表结构、SQL、建表、DDL、DML、创建表、修改表、ALTER TABLE、Orval、API生成
---

# aiperm RBAC 权限管理系统开发专家

## 职责定位

作为 aiperm RBAC 权限管理系统的**功能开发主导技能**，专注于基于 aiperm 框架的完整业务功能开发，从需求分析到代码实现的全流程支持。

## 核心能力

### 1. 完整功能开发流程
- **需求分析**: 理解业务需求，设计功能模块
- **数据库设计**: 设计符合规范的数据库表结构
- **后端开发**: Entity → Mapper → Service → Controller 分层实现
- **API生成**: 使用 Orval 自动生成前端 API 客户端
- **前端对接**: 使用生成的 TypeScript 类型和 API 函数

### 2. aiperm 框架专精
- **代码规范**: 严格遵循 aiperm 开发规范
- **最佳实践**: 应用框架设计模式和最佳实践
- **组件使用**: 熟练使用内置组件和工具类
- **权限控制**: 使用 Sa-Token 实现权限控制

## 何时激活

### 核心触发条件

**功能开发类**: 功能开发、模块开发、业务实现、新增功能、创建模块

**系统管理类**: 权限管理、用户管理、角色管理、菜单管理、字典管理、部门管理、岗位管理

**代码开发类**: Controller开发、Service开发、Mapper开发、Entity开发、DTO开发、VO开发、API接口开发、REST接口

**数据库设计类**: 数据库设计、表结构、SQL、建表、DDL、DML、创建表、修改表、ALTER TABLE

**前端开发类**: Orval、API生成、前端API、TypeScript类型

## 标准开发工作流

### 阶段1: 需求分析与设计
1. 理解业务需求和数据流
2. 设计数据库表结构（参考 database-design.md）
3. 设计API接口
4. 规划权限控制

### 阶段2: 后端实现
1. 创建 Entity 实体类（继承 BaseEntity）
2. 创建 Mapper 接口
3. 实现 Service 服务层
4. 开发 Controller REST 接口
5. 配置权限注解

### 阶段3: 前端对接（⚠️ 重要）
1. 启动后端服务：`./gradlew bootRun`
2. 生成 API 客户端：`cd frontend && pnpm run generate:api`
3. 查看生成的 TypeScript 类型
4. 使用生成的 API 函数开发前端

### 阶段4: 质量保证
1. 代码审查和优化
2. 功能测试验证
3. 接口文档更新

## 开发模块结构

```
src/main/java/com/devlovecode/aiperm/
├── common/                     # 公共组件
│   ├── domain/                 # 基础实体（BaseEntity, R, PageResult）
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

## 前端架构

```
frontend/src/
├── api/                        # API 层（Orval 自动生成）
│   ├── generated.ts            # 生成的 API 函数
│   └── index.ts                # API 导出
├── models/                     # TypeScript 类型定义（Orval 自动生成）
├── stores/                     # Pinia 状态管理
├── router/                     # Vue Router 路由配置
├── views/                      # 页面视图
├── components/                 # 公共组件
└── utils/                      # 工具函数
    └── api-mutator.ts          # Axios 请求拦截器
```

## 核心规范（最重要的几点）

### 代码规范
1. **禁止使用 @Autowired**，使用 `@RequiredArgsConstructor` + `private final` 字段注入
2. **类中不要嵌套类**，嵌套类会增加代码复杂度，无法进行单元测试
3. **Entity 放在 entity 包**，继承 BaseEntity，与数据库表一一映射
4. **DTO 是请求对象，VO 是返回对象**，严格遵循数据流向
5. **所有字段命名遵循驼峰格式**（camelCase），数据库字段使用下划线（snake_case）
6. **禁止使用完全限定类名**，所有类必须先 `import` 再使用

### 数据库设计规范
7. **数据库设计必须遵循规范**，包含所有公共字段、使用反引号、添加注释
8. **表名和字段名使用反引号包裹**（\`table_name\`），命名使用 snake_case
9. **每张表必须包含公共字段**：id, create_time, create_by, update_time, update_by, deleted
10. **SQL 文件保存到 script/{module}/sql/{table_name}.sql**

### API 开发规范（⚠️ 最重要！）
11. **前端绝对禁止手写 API 调用代码！**
12. **必须先开发后端 API**，然后运行 `pnpm run generate:api` 生成前端代码
13. **使用生成的 TypeScript 类型**，确保类型安全

### 权限注解规范
14. **使用 Sa-Token 注解控制接口权限**：
    - `@SaCheckLogin` - 检查用户是否登录
    - `@SaCheckRole("admin")` - 检查用户是否具有角色
    - `@SaCheckPermission("user:create")` - 检查用户是否具有权限

## 统一响应格式

### 单条数据返回
```java
@GetMapping("/{id}")
public R<SysUser> getById(@PathVariable Long id) {
    SysUser user = sysUserService.getById(id);
    return R.ok(user);
}
```

### 分页数据返回
```java
@GetMapping("/page")
public R<PageResult<SysUser>> page(
    @RequestParam(defaultValue = "1") Long pageNum,
    @RequestParam(defaultValue = "10") Long pageSize) {
    PageResult<SysUser> result = sysUserService.page(pageNum, pageSize);
    return R.ok(result);
}
```

### 操作结果返回
```java
@PostMapping
public R<Void> create(@Valid @RequestBody SysUser user) {
    boolean success = sysUserService.create(user);
    return success ? R.ok() : R.fail();
}
```

## 参考文档导航

### 📁 项目结构与规范
- **[project-structure.md](reference/project-structure.md)** - 项目架构、模块说明、依赖关系

### 💾 数据库设计
- **[database-design.md](reference/database-design.md)** - MySQL数据库设计规范
  - 通用格式规则、公共字段规范
  - 表设计最佳实践、索引设计
  - DDL/DML操作规范、SQL文件管理

### 📋 代码规范
- **[code-standards.md](reference/code-standards.md)** - 完整的代码规范
  - 依赖注入规范（禁止 @Autowired）
  - 命名规范、对象分层规范（Entity、DTO、VO）
  - 业务异常处理、数据库查询

### 🔌 API 开发（⚠️ 重要）
- **[api-development.md](reference/api-development.md)** - Orval API 自动生成流程
  - 后端 API 开发流程
  - 前端 API 生成步骤
  - TypeScript 类型使用

### ✅ 质量检查
- **[quality-checklist.md](reference/quality-checklist.md)** - 开发前、中、后的质量检查清单
- **[common-scenarios.md](reference/common-scenarios.md)** - CRUD、状态管理等常见场景

### 🔧 技术细节
- **[tech-stack.md](reference/tech-stack.md)** - aiperm 核心框架组件
- **[utils-reference.md](reference/utils-reference.md)** - 工具类使用参考

## 快速开始

### 场景1: 创建新的CRUD功能
1. 阅读 **[database-design.md](reference/database-design.md)** 设计数据库表
2. 阅读 **[project-structure.md](reference/project-structure.md)** 了解模块结构
3. 创建 Entity → Mapper → Service → Controller
4. 阅读 **[api-development.md](reference/api-development.md)** 生成前端 API
5. 使用 **[quality-checklist.md](reference/quality-checklist.md)** 进行质量检查

### 场景2: 前端对接 API
1. 确保后端 API 已开发完成
2. 启动后端服务：`./gradlew bootRun`
3. 生成 API：`cd frontend && pnpm run generate:api`
4. 查看 `frontend/src/models/` 中的类型定义
5. 使用生成的 API 函数

### 场景3: 性能优化
1. 检查数据库查询和索引
2. 实现缓存策略（Redis）
3. 优化接口返回结构

## 开发提示

- ⚠️ **遇到问题先查阅 reference 文档**，大部分问题的答案都在详细参考文档中
- ⚠️ **遵循"核心规范"**，这些是项目最核心的规范
- ⚠️ **数据库设计遵循 database-design.md 规范**，包含公共字段和注释
- ⚠️ **前端禁止手写 API 调用**，必须使用 Orval 生成
- ⚠️ **使用 @RequiredArgsConstructor**，禁止使用 @Autowired
- ⚠️ **使用 Sa-Token 注解控制权限**

通过这个技能，可以高效完成基于 aiperm 框架的各种业务功能开发（包括数据库设计、后端实现、前端对接），确保代码质量和项目进度。
