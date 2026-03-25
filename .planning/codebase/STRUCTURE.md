# Codebase Structure

**Analysis Date:** 2026-03-25

## Directory Layout

```
aiperm/
├── backend/                         # Spring Boot 后端 (Java 25)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/devlovecode/aiperm/
│   │   │   │   ├── AipermApplication.java       # 主入口类
│   │   │   │   ├── common/                      # 公共组件
│   │   │   │   │   ├── annotation/              # 自定义注解 (@Log)
│   │   │   │   │   ├── aspect/                  # AOP 切面
│   │   │   │   │   ├── context/                 # 应用上下文
│   │   │   │   │   ├── domain/                  # 基础类 (BaseEntity, R, PageResult)
│   │   │   │   │   ├── enums/                   # 枚举 (ErrorCode, OperType)
│   │   │   │   │   ├── exception/               # 异常处理
│   │   │   │   │   ├── interceptor/             # 拦截器
│   │   │   │   │   ├── repository/              # 基础 Repository (BaseJpaRepository)
│   │   │   │   │   ├── service/                 # 公共服务 (DataScopeService)
│   │   │   │   │   └── util/                     # 工具类
│   │   │   │   ├── config/                      # 配置类
│   │   │   │   │   ├── CacheConfig.java         # Redis 缓存配置
│   │   │   │   │   ├── JacksonConfig.java       # JSON 序列化配置
│   │   │   │   │   ├── SaTokenConfig.java       # Sa-Token 配置
│   │   │   │   │   ├── StpInterfaceImpl.java    # Sa-Token 权限加载实现
│   │   │   │   │   ├── TaskSchedulerConfig.java # 定时任务配置
│   │   │   │   │   └── WebMvcConfig.java        # Web MVC 配置
│   │   │   │   └── modules/                     # 业务模块
│   │   │   │       ├── auth/                    # 认证模块
│   │   │   │       │   ├── controller/
│   │   │   │       │   ├── dto/
│   │   │   │       │   ├── service/
│   │   │   │       │   ├── strategy/            # 登录策略 (密码/SMS/邮箱/OAuth)
│   │   │   │       │   └── vo/
│   │   │   │       ├── system/                  # 系统管理模块
│   │   │   │       │   ├── controller/          # SysUserController, SysRoleController, 等
│   │   │   │       │   ├── dto/                 # 数据传输对象
│   │   │   │       │   ├── entity/              # 实体类 (SysUser, SysRole, 等)
│   │   │   │       │   ├── repository/          # 数据访问层
│   │   │   │       │   ├── service/             # 业务逻辑层
│   │   │   │       │   └── vo/                  # 视图对象
│   │   │   │       ├── oauth/                   # OAuth 第三方登录
│   │   │   │       ├── mfa/                     # 多因子认证
│   │   │   │       ├── captcha/                 # 验证码
│   │   │   │       ├── log/                     # 操作日志 & 登录日志
│   │   │   │       ├── oss/                     # 对象存储 (本地/阿里云)
│   │   │   │       ├── enterprise/              # 企业管理
│   │   │   │       │   ├── controller/          # SysNoticeController, SysJobController, 等
│   │   │   │       │   ├── entity/
│   │   │   │       │   ├── repository/
│   │   │   │       │   └── service/
│   │   │   │       ├── dashboard/               # 仪表盘
│   │   │   │       └── profile/                 # 个人中心
│   │   │   └── resources/
│   │   │       ├── application.yaml             # 主配置文件
│   │   │       ├── application-dev.yaml         # 开发环境配置
│   │   │       ├── application-prod.yaml        # 生产环境配置
│   │   │       └── db/migration/                # Flyway 数据库迁移脚本
│   │   │           ├── V1.0.0__init_rbac_schema.sql
│   │   │           ├── V2.0.0__merge_menu_permission.sql
│   │   │           └── ...
│   │   └── test/                               # 单元测试
│   ├── build.gradle                            # Gradle 构建配置
│   └── gradle/                                 # Gradle wrapper
├── frontend/                                   # Vue 3 前端
│   ├── src/
│   │   ├── api/                                # API 客户端 (手写)
│   │   │   ├── auth.ts                         # 认证 API
│   │   │   ├── system/                         # 系统模块 API
│   │   │   │   ├── user.ts
│   │   │   │   ├── role.ts
│   │   │   │   ├── menu.ts
│   │   │   │   ├── dict.ts
│   │   │   │   ├── dept.ts
│   │   │   │   └── post.ts
│   │   │   ├── enterprise/                     # 企业模块 API
│   │   │   │   ├── notice.ts
│   │   │   │   ├── job.ts
│   │   │   │   ├── config.ts
│   │   │   │   └── message.ts
│   │   │   ├── oauth.ts                        # OAuth API
│   │   │   ├── captcha.ts                      # 验证码 API
│   │   │   ├── log.ts                          # 日志 API
│   │   │   ├── oss.ts                          # 对象存储 API
│   │   │   ├── profile.ts                      # 个人中心 API
│   │   │   └── dashboard.ts                    # 仪表盘 API
│   │   ├── components/                         # 公共组件
│   │   │   ├── layout/                         # 布局组件
│   │   │   │   ├── MainLayout.vue              # 主布局
│   │   │   │   ├── AppHeader.vue               # 顶部导航
│   │   │   │   └── AppSidebar.vue              # 侧边栏菜单
│   │   │   ├── dict/                           # 字典组件
│   │   │   │   ├── DictTag.vue                 # 字典标签
│   │   │   │   ├── DictSelect.vue              # 字典下拉框
│   │   │   │   └── DictRadio.vue               # 字典单选框
│   │   │   ├── table/                          # 表格组件
│   │   │   │   ├── TableToolbar.vue            # 表格工具栏
│   │   │   │   ├── ColumnSetting.vue           # 列设置
│   │   │   │   └── SelectionBar.vue            # 批量操作栏
│   │   │   ├── mfa/                            # MFA 组件
│   │   │   │   ├── MfaVerifyDialog.vue         # MFA 验证对话框
│   │   │   │   └── MfaBindDialog.vue           # MFA 绑定对话框
│   │   │   └── agent/                          # AI Agent 组件
│   │   ├── composables/                        # 组合式函数
│   │   ├── directives/                         # 自定义指令
│   │   │   └── permission.ts                   # 权限指令 (v-permission, v-role)
│   │   ├── router/                             # 路由配置
│   │   │   └── index.ts                        # 路由定义 & 守卫
│   │   ├── stores/                             # Pinia 状态管理
│   │   │   ├── user.ts                         # 用户状态 (token, userInfo, permissions)
│   │   │   ├── permission.ts                   # 权限状态 (menus, routes)
│   │   │   └── app.ts                          # 应用状态 (sidebar, theme)
│   │   ├── types/                              # TypeScript 类型定义
│   │   ├── utils/                              # 工具函数
│   │   │   └── request.ts                      # Axios 封装
│   │   ├── views/                              # 页面视图
│   │   │   ├── login/                          # 登录页
│   │   │   ├── dashboard/                      # 仪表盘
│   │   │   ├── error/                          # 错误页 (404)
│   │   │   ├── profile/                        # 个人中心
│   │   │   ├── system/                         # 系统管理页面
│   │   │   │   ├── user/                       # 用户管理
│   │   │   │   ├── role/                       # 角色管理
│   │   │   │   ├── menu/                       # 菜单管理
│   │   │   │   ├── dict/                       # 字典管理
│   │   │   │   ├── dept/                       # 部门管理
│   │   │   │   └── post/                       # 岗位管理
│   │   │   └── enterprise/                     # 企业管理页面
│   │   │       ├── notice/                     # 通知公告
│   │   │       ├── job/                        # 定时任务
│   │   │       ├── config/                     # 系统配置
│   │   │       ├── message/                    # 消息管理
│   │   │       ├── captcha-config/             # 验证码配置
│   │   │       └── mfa-policy/                 # MFA 策略
│   │   ├── assets/                             # 静态资源
│   │   ├── App.vue                             # 根组件
│   │   └── main.ts                             # 应用入口
│   ├── orval.config.ts                         # Orval API 生成配置
│   ├── package.json                            # 依赖配置
│   ├── vite.config.ts                          # Vite 构建配置
│   ├── tsconfig.json                           # TypeScript 配置
│   └── eslint.config.js                        # ESLint 配置
├── docs/                                       # 项目文档
├── deploy/                                     # 部署脚本
├── .claude/                                    # Claude 技能
├── .omc/                                       # OMC 工作树状态
├── .planning/                                  # 项目规划文档
├── CLAUDE.md                                   # 项目开发规范
├── README.md                                   # 项目说明
└── agents/                                     # 技能文件
```

## Directory Purposes

**backend/src/main/java/com/devlovecode/aiperm/**
- Purpose: Backend Java source root
- Contains: All backend application code
- Key files: `AipermApplication.java` (main entry point)

**backend/src/main/java/com/devlovecode/aiperm/common/**
- Purpose: Shared infrastructure code across all modules
- Contains: Base classes, utilities, configurations, exceptions
- Key files: `BaseEntity.java`, `BaseJpaRepository.java`, `R.java`, `PageResult.java`

**backend/src/main/java/com/devlovecode/aiperm/config/**
- Purpose: Spring configuration classes
- Contains: Bean definitions, property configurations
- Key files: `SaTokenConfig.java`, `WebMvcConfig.java`

**backend/src/main/java/com/devlovecode/aiperm/modules/**
- Purpose: Business feature modules (bounded contexts)
- Contains: Independent feature implementations with own layers
- Structure: Each module has `controller/`, `service/`, `repository/`, `entity/`, `dto/`, `vo/`

**backend/src/main/java/com/devlovecode/aiperm/modules/system/**
- Purpose: Core RBAC system management
- Contains: User, Role, Menu, Permission, Department, Post, Dictionary management
- Key files: `SysUserController.java`, `SysRoleController.java`, `SysMenuController.java`

**backend/src/main/java/com/devlovecode/aiperm/modules/auth/**
- Purpose: Authentication and authorization
- Contains: Login, logout, token management, permission loading
- Key files: `AuthController.java`, `AuthService.java`

**backend/src/main/resources/db/migration/**
- Purpose: Database schema version control
- Contains: Flyway migration SQL scripts
- Naming: `V{version}__{description}.sql` (e.g., `V1.0.0__init_rbac_schema.sql`)

**frontend/src/api/**
- Purpose: Typed API client functions
- Contains: Hand-written API wrappers calling backend endpoints
- Key files: `auth.ts`, `system/user.ts`, `system/role.ts`

**frontend/src/stores/**
- Purpose: Global state management with Pinia
- Contains: Reactive state stores for user, permissions, app settings
- Key files: `user.ts` (auth state), `permission.ts` (menu/routes)

**frontend/src/views/**
- Purpose: Page-level Vue components
- Contains: Route-bound page components
- Structure: Organized by feature (system/, enterprise/, login/)

**frontend/src/components/**
- Purpose: Reusable Vue components
- Contains: Shared UI components (layout, dict, table, mfa)
- Key files: `layout/MainLayout.vue`, `dict/DictSelect.vue`

**frontend/src/router/**
- Purpose: Vue Router configuration
- Contains: Route definitions, navigation guards
- Key files: `index.ts`

## Key File Locations

**Entry Points:**
- `backend/src/main/java/com/devlovecode/aiperm/AipermApplication.java`: Spring Boot main class
- `frontend/src/main.ts`: Vue app initialization
- `frontend/src/App.vue`: Root Vue component

**Configuration:**
- `backend/src/main/resources/application.yaml`: Backend main configuration
- `backend/src/main/resources/application-dev.yaml`: Development environment config
- `backend/build.gradle`: Backend dependencies and build config
- `frontend/vite.config.ts`: Frontend build tool config
- `frontend/package.json`: Frontend dependencies

**Core Logic (Backend):**
- `backend/src/main/java/com/devlovecode/aiperm/common/domain/`: Base domain classes
- `backend/src/main/java/com/devlovecode/aiperm/common/repository/`: Base repository interfaces
- `backend/src/main/java/com/devlovecode/aiperm/modules/*/controller/`: REST endpoints
- `backend/src/main/java/com/devlovecode/aiperm/modules/*/service/`: Business logic

**Core Logic (Frontend):**
- `frontend/src/stores/user.ts`: User authentication state
- `frontend/src/stores/permission.ts`: Menu and route management
- `frontend/src/router/index.ts`: Route configuration and guards
- `frontend/src/api/`: API client functions

**Testing:**
- `backend/src/test/java/com/devlovecode/aiperm/`: Backend unit tests
- No frontend tests configured (Playwright installed but not used)

## Naming Conventions

**Backend Java Files:**
- Entity: `SysXxx.java` (e.g., `SysUser.java`, `SysRole.java`)
- Controller: `SysXxxController.java` (e.g., `SysUserController.java`)
- Service: `XxxService.java` (e.g., `UserService.java`)
- Repository: `XxxRepository.java` (e.g., `UserRepository.java`)
- DTO: `XxxDTO.java` (e.g., `UserDTO.java`)
- VO: `XxxVO.java` (e.g., `UserVO.java`)

**Frontend Files:**
- Components: PascalCase (e.g., `UserList.vue`, `DictSelect.vue`)
- Composables: camelCase with `use` prefix (e.g., `useUserList.ts`)
- Stores: camelCase (e.g., `user.ts`, `permission.ts`)
- API files: camelCase (e.g., `user.ts`, `role.ts`)
- Types/Interfaces: PascalCase (e.g., `UserVO`, `PageResult`)

**Backend Packages:**
- All lowercase: `com.devlovecode.aiperm.modules.system`
- Module-based: `modules/{feature}/layer/`

**Frontend Directories:**
- All lowercase: `src/views/system/user/`
- Feature-based: `src/api/system/`, `src/views/enterprise/`

**Database Migrations:**
- Format: `V{major}.{minor}.{patch}__{description}.sql`
- Example: `V1.0.0__init_rbac_schema.sql`, `V3.5.0__init_menu_data.sql`

## Where to Add New Code

**New Backend Feature (e.g., "Article" module):**
- Entity: `backend/src/main/java/com/devlovecode/aiperm/modules/article/entity/SysArticle.java`
- Repository: `backend/src/main/java/com/devlovecode/aiperm/modules/article/repository/ArticleRepository.java`
- Service: `backend/src/main/java/com/devlovecode/aiperm/modules/article/service/ArticleService.java`
- Controller: `backend/src/main/java/com/devlovecode/aiperm/modules/article/controller/SysArticleController.java`
- DTO: `backend/src/main/java/com/devlovecode/aiperm/modules/article/dto/ArticleDTO.java`
- VO: `backend/src/main/java/com/devlovecode/aiperm/modules/article/vo/ArticleVO.java`
- Migration: `backend/src/main/resources/db/migration/V5.0.0__create_article_table.sql`

**New Frontend Feature:**
- API: `frontend/src/api/system/article.ts`
- Types: Export in API file or `frontend/src/types/article.ts`
- Store: Optional, add to `frontend/src/stores/article.ts` if global state needed
- Components: `frontend/src/views/system/article/index.vue`
- Routes: Auto-generated from backend menu, or manually add to `constantRoutes` in `router/index.ts`

**New Backend Utility:**
- Add to `backend/src/main/java/com/devlovecode/aiperm/common/util/`

**New Frontend Utility:**
- Add to `frontend/src/utils/`

**New Shared Component:**
- Add to `frontend/src/components/` (create subdirectory if needed)
- Register globally in `frontend/src/main.ts` if widely used

**New Configuration:**
- Backend: Add to `backend/src/main/java/com/devlovecode/aiperm/config/` or `application.yaml`
- Frontend: Add to `frontend/vite.config.ts` or environment files

## Special Directories

**backend/src/main/resources/db/migration/**
- Purpose: Flyway database migration scripts
- Generated: No (manually written SQL)
- Committed: Yes
- Execution order: By version number in filename (V1.0.0, V1.0.1, etc.)

**frontend/node_modules/**
- Purpose: npm/pnpm dependencies
- Generated: Yes
- Committed: No (in .gitignore)

**backend/build/, frontend/dist/**
- Purpose: Build output
- Generated: Yes
- Committed: No

**.claude/, .omc/, .planning/**
- Purpose: Claude AI assistant skills and project planning
- Generated: Mixed (skills are code, planning is AI-generated)
- Committed: Yes

**docs/**
- Purpose: Project documentation
- Generated: No (manually written)
- Committed: Yes

---

*Structure analysis: 2026-03-25*
