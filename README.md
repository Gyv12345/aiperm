# AiPerm

AiPerm 不是一个单纯的 RBAC 后台模板，而是一个面向 AI 协作开发的业务基座。

它要解决的问题很明确：

- 给 AI 一个稳定、可持续扩展的企业应用骨架
- 把认证、权限、菜单、审计、监控、任务、存储这些通用能力先打好底
- 后续新增业务模块时，让 AI 能按项目既有规范继续补数据库、后端、菜单、前端页面，而不是每次从零发散

## 为什么做这个项目

这个项目的核心目标是做一个可复用的 AI 基座，而不是一次性的管理后台 Demo。

在很多实际业务里，AI 很擅长补全 CRUD、菜单、接口、页面、校验和迁移脚本，但前提是项目本身要有清晰边界、稳定结构和统一约定。AiPerm 的价值就在这里：

- 对业务开发者：先把权限、认证、菜单、审计、监控这些“底盘”做好
- 对 AI：给它一个足够稳定的工程上下文，让它知道新增模块应该落在哪、怎么命名、怎么连菜单、怎么补前端
- 对后续维护：把“继续长业务”这件事，收敛成一套可以重复执行的工程流程

## 项目定位

AiPerm 当前定位是：

- AI Native 企业后台基座
- Agent + 权限控制 + 企业管理能力的统一入口
- 后续业务模块持续增长的宿主工程

核心思路是：

- 后端负责认证、权限、审计、菜单、数据边界、企业能力编排
- 前端负责管理端交互、Agent 页面承载、业务操作入口
- 大模型密钥尽量不托管在服务端，优先采用 BYOK / 本地持有方案

## 技术基线

### 后端

- Java 25
- Spring Boot 4
- Spring Modulith
- Spring Data JPA
- Flyway
- Redis
- Sa-Token

### 前端

- Vue 3
- TypeScript
- Vite
- Element Plus

## 为什么引入 Spring Modulith

今天项目已经引入 Spring Modulith，目的不是“追新”，而是为了让后续业务模块增长时更可控。

它带来的实际价值是：

- 模块边界更清晰，AI 在补代码时不容易跨模块乱写
- 跨模块调用更容易收敛到明确的 `api` 能力，而不是随意依赖内部实现
- 后续新增业务模块时，可以持续保持“按领域拆分”的结构，不把项目重新做回大泥球

当前后端模块已经按领域组织在 `backend/src/main/java/com/devlovecode/aiperm/modules/` 下，例如：

- `auth`
- `system`
- `monitor`
- `enterprise`
- `storage`
- `dashboard`
- `audit`

## 目录结构

```text
aiperm/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/devlovecode/aiperm/
│       ├── common/                  # 公共基础能力
│       ├── config/                  # 全局配置
│       └── modules/                 # 按领域划分的业务模块
│           ├── auth/
│           ├── system/
│           ├── monitor/
│           ├── enterprise/
│           ├── storage/
│           ├── dashboard/
│           └── audit/
│
├── backend/src/main/resources/db/migration/
│   └── Vx.x.x__*.sql               # Flyway 迁移脚本
│
├── frontend/
│   └── src/
│       ├── api/                     # 前端 API 封装
│       ├── views/                   # 页面视图
│       ├── stores/                  # Pinia 状态管理
│       ├── router/                  # 路由
│       ├── components/              # 公共组件
│       └── utils/                   # 工具与请求封装
│
├── docs/                            # 设计与实施文档
├── aiperm-api-generator.skill       # AI 接口对接技能包
└── aiperm-dev.skill                 # AI 模块开发技能包
```

## 后续新增业务模块，应该怎么加

这是这个项目最重要的约定之一。

后续如果新增业务模块，不建议临时讨论“这次怎么放”，而是直接按项目结构继续长。

### 1. 后端先建领域模块

在 `backend/src/main/java/com/devlovecode/aiperm/modules/<domain>/` 下创建自己的包结构。常见子包：

- `api`
- `controller`
- `service`
- `repository`
- `entity`
- `dto`
- `vo`
- `export`

原则：

- 模块内部实现留在本模块内
- 跨模块依赖优先通过 `modules/<other>/api` 暴露的能力访问
- 不要直接把别的模块内部 service/repository 当公共接口使用

### 2. 数据库变更走 Flyway

所有表结构、字段、菜单初始化、权限点初始化都通过 Flyway 维护：

- 路径：`backend/src/main/resources/db/migration/`
- 命名：`Vx.x.x__description.sql`

新增业务模块时，至少要考虑：

- 业务表
- 索引
- 初始化菜单
- 初始化按钮权限
- 超级管理员默认授权（如适用）

### 3. 菜单和权限也属于模块交付的一部分

在 AiPerm 里，一个完整业务模块不只是后端接口，还包括：

- 菜单目录
- 页面菜单
- 按钮权限
- 角色菜单关系

也就是说，新增模块时，菜单 SQL 不应后补，而应视为模块本身的一部分。

### 4. 前端页面按菜单组件路径落地

前端页面放在 `frontend/src/views/` 下，建议按领域组织，例如：

- `frontend/src/views/system/...`
- `frontend/src/views/monitor/...`
- `frontend/src/views/enterprise/...`

后端菜单表里的 `component` 字段，需要和前端视图路径对齐。

例如：

- 菜单 `component = monitor/online/index`
- 对应页面文件：`frontend/src/views/monitor/online/index.vue`

当前项目就是通过这个约定动态生成前端路由的。

### 5. 前端 API 层跟着模块走

前端 API 不应该散着写。新增后端 Controller 后，前端要补对应的 API 文件，保持模块映射关系清晰。

例如：

- `backend/modules/system/.../UserController`
- 对应 `frontend/src/api/system/user.ts`

### 6. 验证方式也按项目约定走

前端改动后至少执行：

```bash
cd frontend && pnpm run type-check
```

后端由开发者在 IntelliJ IDEA 中启动和验证。

## 让 AI 直接按项目继续开发

AiPerm 的一个重要设计目标，就是让 AI 可以直接接着项目往下做，而不是每次都重新解释一遍规则。

后续如果你要让 AI 新增模块，可以直接让它按这个项目完成：

- 数据库迁移
- 后端模块
- 菜单和权限
- 前端 API
- 前端页面

推荐给 AI 的任务描述方式：

1. 说明业务目标
2. 指定模块名
3. 明确是否需要菜单、按钮权限、列表页、表单页、导出等
4. 让 AI 按 AiPerm 现有结构补齐数据库、后端、前端

例如：

```text
请基于 AiPerm 新增采购管理模块，包含采购单列表、创建、编辑、详情、删除、导出。
需要补 Flyway、后端模块、菜单权限、前端 API、前端页面，并遵守当前项目结构和命名规范。
```

## 仓库内置的 AI Skill

仓库里目前放了两个可直接复用的 skill 包：

### `aiperm-dev.skill`

用于让 AI 按 AiPerm 既有结构继续开发业务模块，覆盖：

- 模块设计
- 数据库表结构
- 后端分层实现
- 菜单与权限接入
- 前端对接流程

### `aiperm-api-generator.skill`

用于让 AI 根据后端 Controller 结构补前端 API 层，保持接口对接方式和项目现状一致。

这两个 skill 的意义不是做“额外文档”，而是把项目约定显式交给 AI，使它在后续功能开发时更稳定。

## 本地开发

### 后端

推荐直接使用 IntelliJ IDEA 启动 `backend`。

如果需要命令行启动：

```bash
cd backend
mvn spring-boot:run
```

### 前端

```bash
cd frontend
pnpm install
pnpm run dev
```

### 前端检查

```bash
cd frontend
pnpm run type-check
pnpm run lint
pnpm run build
```

## 配置说明

- 默认配置：`backend/src/main/resources/application.yaml`
- 开发配置：`backend/src/main/resources/application-dev.yaml`
- 生产配置模板：`backend/src/main/resources/application-prod.yaml`

核心环境变量：

- `DB_URL` `DB_USERNAME` `DB_PASSWORD`
- `REDIS_HOST` `REDIS_PORT` `REDIS_PASSWORD` `REDIS_DATABASE`
- `SA_TOKEN_JWT_SECRET`

## 仓库约定

以下内容属于本地 AI 协作资产，不应继续提交到公开仓库：

- `.agents/`
- `.claude/`

这类目录更适合作为本地工作环境的一部分，而不是项目源码的一部分。

## 项目现状

当前 AiPerm 已经具备这些基础能力：

- 账号认证：用户名密码 / 短信 / 邮箱 / OAuth
- 权限模型：用户、角色、菜单、部门、岗位、字典
- 安全能力：2FA、幂等、限流、操作日志、登录日志
- 企业能力：通知、消息、定时任务
- 监控能力：在线用户、服务监控、缓存监控、登录日志、操作日志、任务日志

## 联系方式

- 公司：河南爱编程网络科技有限公司
- 官网：<https://devlovecode.com>
