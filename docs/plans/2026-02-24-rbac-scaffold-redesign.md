# aiperm RBAC 脚手架重构设计文档

**日期**：2026-02-24
**状态**：已批准，待实施
**目标**：将 aiperm 改造为 AI 友好的 RBAC 通用脚手架，适用于 CMS/CRM/ERP 等业务系统快速开发

---

## 背景与问题

当前代码库存在以下问题需要修复：

1. `common/domain` 与 `common/entity` **完全重复**（BaseEntity、R、PageResult 各有两份）
2. `common/exception/GlobalExceptionHandler` 与 `common/handler/GlobalExceptionHandler` **重复**
3. `sys_menu` 与 `sys_permission` **重复设计**（导致两套关联表）
4. 缺少**操作日志模块**
5. 缺少 **OSS 文件存储模块**
6. `aiperm-dev` 技能规范不足，AI 开发时缺乏明确参照

---

## 决策摘要

| 决策项 | 选择 |
|--------|------|
| menu/permission 合并方案 | 合并为单张 `sys_menu`，按钮行存 `perms` 权限码 |
| OSS 存储后端 | 本地存储 + 阿里云 OSS，通过配置切换 |
| 日志记录方式 | AOP 注解方式（`@Log`），自动记录操作日志 |
| AI 友好方案 | 标准化规范 + demo 模块 + OpenAPI→Orval 闭环 |
| 改造方式 | 方案 B：在当前项目整体重构，技术栈不变 |

---

## 第一节：后端包结构

### 删除
- `common/entity/`（整个包删除）
- `common/handler/GlobalExceptionHandler.java`

### 保留并增强
- `common/domain/`（唯一基础类来源）
- `common/exception/`（唯一异常处理来源）

### 新增
```
common/
├── annotation/
│   └── Log.java               # 操作日志注解
└── aspect/
    └── LogAspect.java         # AOP 切面，自动记录 @Log 方法

modules/
├── log/                       # 操作日志模块
│   ├── entity/SysOperLog.java
│   ├── mapper/SysOperLogMapper.java
│   ├── service/ISysOperLogService.java
│   ├── service/impl/SysOperLogServiceImpl.java
│   └── controller/SysOperLogController.java
└── oss/                       # 文件存储模块
    ├── config/OssProperties.java
    ├── service/OssService.java        # 接口
    ├── service/impl/LocalOssServiceImpl.java
    ├── service/impl/AliyunOssServiceImpl.java
    └── controller/OssController.java
```

### 最终完整包结构
```
src/main/java/com/devlovecode/aiperm/
├── common/
│   ├── domain/          # BaseEntity, R, PageResult
│   ├── enums/           # ErrorCode, OperType, OssType
│   ├── exception/       # BusinessException, GlobalExceptionHandler
│   ├── annotation/      # @Log
│   └── aspect/          # LogAspect
├── config/              # SaTokenConfig, MybatisPlusConfig, WebMvcConfig
└── modules/
    ├── auth/            # 认证模块
    ├── system/          # 系统管理（用户/角色/菜单/部门/岗位/字典）
    ├── log/             # 操作日志
    └── oss/             # 文件存储
```

---

## 第二节：数据库重设计

### 删除的表
- `sys_permission`（整个删除）
- `sys_role_permission`（整个删除）

### 修改的表：`sys_menu` 增加 `perms` 字段

```sql
ALTER TABLE sys_menu
  ADD COLUMN perms VARCHAR(100) DEFAULT NULL COMMENT '权限标识（按钮用，如 system:user:add）'
    AFTER component;
```

`menu_type` 取值：`M`=目录，`C`=菜单，`F`=按钮
- 目录/菜单行：`path`、`component`、`icon` 有值，`perms` 为空
- 按钮行：`perms` 有值（如 `system:user:add`），路由字段为空

### 角色-菜单关联（合并后）
```
sys_role_menu (role_id, menu_id)  ← 原两张关联表合并为一张
```

### 新增表

```sql
-- 操作日志表
CREATE TABLE sys_oper_log (
  id          BIGINT NOT NULL AUTO_INCREMENT,
  title       VARCHAR(50)  COMMENT '操作模块',
  oper_type   TINYINT      COMMENT '操作类型：1-新增 2-修改 3-删除 4-查询',
  method      VARCHAR(200) COMMENT '方法名',
  request_method VARCHAR(10) COMMENT 'HTTP方法',
  oper_url    VARCHAR(255) COMMENT '请求URL',
  oper_ip     VARCHAR(50)  COMMENT '操作IP',
  oper_param  TEXT         COMMENT '请求参数JSON',
  json_result TEXT         COMMENT '响应结果JSON',
  status      TINYINT DEFAULT 0 COMMENT '0-成功 1-失败',
  error_msg   VARCHAR(2000) COMMENT '错误信息',
  cost_time   BIGINT       COMMENT '耗时(ms)',
  oper_user   VARCHAR(50)  COMMENT '操作人账号',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) COMMENT='操作日志表';

-- 文件记录表
CREATE TABLE sys_file (
  id            BIGINT NOT NULL AUTO_INCREMENT,
  file_name     VARCHAR(200) COMMENT '存储文件名',
  original_name VARCHAR(200) COMMENT '原始文件名',
  file_path     VARCHAR(500) COMMENT '存储路径',
  file_url      VARCHAR(500) COMMENT '访问URL',
  file_size     BIGINT       COMMENT '文件大小(字节)',
  file_type     VARCHAR(100) COMMENT 'MIME类型',
  storage_type  VARCHAR(20)  COMMENT 'local/aliyun',
  deleted       TINYINT DEFAULT 0,
  create_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by     VARCHAR(50),
  PRIMARY KEY (id)
) COMMENT='文件记录表';
```

---

## 第三节：AI 开发规范

### 标准分层约定

| 层 | 类名规范 | 继承/实现 |
|----|---------|----------|
| Entity | `SysXxx` | `extends BaseEntity` |
| Mapper | `SysXxxMapper` | `extends BaseMapper<SysXxx>` |
| Service 接口 | `ISysXxxService` | `extends IService<SysXxx>` |
| Service 实现 | `SysXxxServiceImpl` | `extends ServiceImpl<Mapper, Entity>` |
| Controller | `SysXxxController` | `@RestController` |
| 请求 DTO | `XxxCreateRequest` / `XxxUpdateRequest` / `XxxQueryRequest` | — |
| 响应 VO | `XxxVO` | — |
| 转换器 | `XxxConverter` | MapStruct-Plus 接口 |

### 权限码约定
```
格式：模块:资源:操作
示例：system:user:list / system:user:create / system:user:update / system:user:delete
      system:role:list / oss:file:upload / log:oper:list
```

### @Log 注解使用
```java
@Log(title = "用户管理", operType = OperType.CREATE)
@PostMapping
public R<Void> create(@RequestBody @Valid UserCreateRequest req) { ... }
```

### OSS 上传使用
```java
@Autowired
private OssService ossService;

OssResult result = ossService.upload(file);
// result.getUrl() → 文件访问地址
```

### 新增业务模块检查清单
- [ ] 建表 SQL 写入新 Flyway 迁移文件 `Vx.x.x__描述.sql`
- [ ] Entity 继承 `BaseEntity`，加 `@TableName` `@Schema`
- [ ] Controller 每个写操作加 `@Log(title="xxx", operType=OperType.xxx)`
- [ ] 按钮权限写入 `sys_menu` 初始化数据（`menu_type='F'`, `perms='module:res:action'`）
- [ ] 后端完成后运行 `cd frontend && pnpm run generate:api`
- [ ] 使用生成的 API 函数，不手写 axios 调用

### demo 参照模块
`modules/system/` 下的**字典管理**（`SysDict`）作为完整示例，包含 Entity/Mapper/Service/Controller/DTO/VO/Converter 全套文件，AI 开发新功能时以此为模板。

### 前端开发规范（不变）
- 禁止手写 API 调用，必须通过 `pnpm run generate:api` 生成
- 使用 `getScrmApi()` / 直接导入生成函数
- 类型定义从 `@/models` 导入

---

## 实施范围总结

### 后端改动
1. 删除 `common/entity/` 包（3个文件）
2. 删除 `common/handler/GlobalExceptionHandler.java`
3. 修复所有 `import common.entity.*` → `import common.domain.*`
4. 新增 `common/annotation/Log.java`
5. 新增 `common/aspect/LogAspect.java`
6. 新增 `modules/log/` 模块（完整 CRUD）
7. 新增 `modules/oss/` 模块（本地 + 阿里云）
8. 新增 Flyway 迁移脚本（删 permission 表、加 perms 字段、加 log/file 表）
9. 新增字典管理 demo 模块

### 规范文档改动
1. 更新 `CLAUDE.md`：新增后端开发规范章节
2. 更新 `aiperm-dev` 技能：补充完整开发流程、命名规范、示例代码
