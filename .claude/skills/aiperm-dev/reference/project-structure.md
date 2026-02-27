## 项目结构速查

### 整体架构

```
aiperm/
├── backend/                                # 后端项目
│   ├── src/main/java/com/devlovecode/aiperm/
│   │   ├── AipermApplication.java          # 启动类
│   │   │
│   │   ├── common/                         # 公共组件
│   │   │   ├── annotation/                 # 注解（@Log）
│   │   │   ├── aspect/                     # 切面（LogAspect、LogEvent）
│   │   │   ├── context/                    # 上下文（DataScopeHolder）
│   │   │   ├── domain/                     # 基础类
│   │   │   │   ├── BaseEntity.java         # 实体基类
│   │   │   │   ├── R.java                  # 统一响应封装
│   │   │   │   ├── PageResult.java         # 分页结果封装
│   │   │   │   └── Views.java              # DTO 验证分组
│   │   │   ├── enums/                      # 枚举
│   │   │   │   ├── ErrorCode.java          # 错误码枚举
│   │   │   │   ├── OperType.java           # 操作类型枚举
│   │   │   │   └── DataScopeEnum.java      # 数据范围枚举
│   │   │   ├── exception/                  # 异常处理
│   │   │   │   ├── BusinessException.java  # 业务异常
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── interceptor/                # 拦截器
│   │   │   ├── repository/                 # Repository 基类
│   │   │   │   ├── BaseRepository.java     # 通用 Repository
│   │   │   │   └── SqlBuilder.java         # SQL 条件构建器
│   │   │   ├── service/                    # 公共服务
│   │   │   └── util/                       # 工具类
│   │   │
│   │   ├── config/                         # 配置类
│   │   │   ├── SaTokenConfig.java          # Sa-Token 配置
│   │   │   ├── StpInterfaceImpl.java       # 权限接口实现
│   │   │   └── WebMvcConfig.java           # Web MVC 配置
│   │   │
│   │   └── modules/                        # 业务模块（按领域划分）
│   │       ├── auth/                       # 认证模块
│   │       │   ├── controller/             # AuthController
│   │       │   ├── dto/request/            # LoginRequest
│   │       │   └── vo/                     # LoginVO、CaptchaVO、UserInfoVO
│   │       │
│   │       ├── system/                     # 系统管理模块
│   │       │   ├── entity/                 # SysUser、SysRole、SysMenu...
│   │       │   ├── repository/             # 各 Repository 类
│   │       │   ├── service/                # 各 Service 类
│   │       │   ├── controller/             # 各 Controller 类
│   │       │   ├── dto/                    # DTO（含 @JsonView 分组）
│   │       │   └── vo/                     # 响应 VO
│   │       │
│   │       ├── enterprise/                 # 企业管理模块
│   │       │   ├── entity/                 # SysConfig、SysJob、SysMessage、SysNotice
│   │       │   ├── repository/
│   │       │   ├── service/
│   │       │   ├── controller/
│   │       │   ├── dto/
│   │       │   └── vo/
│   │       │
│   │       ├── log/                        # 操作日志模块
│   │       │   ├── entity/                 # SysOperLog
│   │       │   ├── repository/
│   │       │   ├── service/
│   │       │   ├── controller/
│   │       │   └── listener/               # LogEventListener
│   │       │
│   │       ├── oss/                        # 文件存储模块
│   │       │   ├── config/                 # OssProperties
│   │       │   ├── domain/                 # OssResult
│   │       │   ├── service/                # OssService、LocalOssServiceImpl、AliyunOssServiceImpl
│   │       │   └── controller/
│   │       │
│   │       └── mcp/                        # MCP 模块
│   │           ├── config/                 # McpServerConfig
│   │           ├── security/               # McpSecurityInterceptor
│   │           ├── tool/                   # 各 Tool 类（UserTool、RoleTool...）
│   │           └── toon/                   # ToonEncoder（TOON 格式编码器）
│   │
│   └── src/main/resources/
│       ├── db/migration/                   # Flyway 迁移脚本
│       └── application.yaml                # 配置文件
│
├── frontend/                               # 前端项目
│   ├── src/
│   │   ├── api/                            # API 层（Orval 自动生成）
│   │   ├── models/                         # TypeScript 类型（Orval 生成）
│   │   ├── stores/                         # Pinia 状态管理
│   │   ├── router/                         # Vue Router 路由
│   │   ├── views/                          # 页面视图
│   │   ├── components/                     # 公共组件
│   │   └── composables/                    # 组合式函数
│   └── orval.config.ts                     # Orval 配置
│
└── .claude/                                # Claude AI 配置
    └── skills/aiperm-dev/                  # 开发技能
```

### 模块命名规范

```
modules/{module}/
├── entity/           # 实体类（继承 BaseEntity，命名：Sys{名称}）
├── repository/       # Repository 类（命名：{名称}Repository）
├── service/          # Service 类（命名：{名称}Service）
├── controller/       # REST 控制器（命名：Sys{名称}Controller）
├── dto/              # 数据传输对象（命名：{名称}DTO，使用 @JsonView 分组）
└── vo/               # 视图对象（命名：{名称}VO）
```

### 类命名规范

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| Entity | Sys{名称} | SysUser, SysRole |
| Repository | {名称}Repository | UserRepository |
| Service | {名称}Service | UserService |
| Controller | Sys{名称}Controller | SysUserController |
| DTO | {名称}DTO | UserDTO |
| VO | {名称}VO | UserVO |
| Flyway 脚本 | Vx.x.x\_\_{描述}.sql | V2.1.0\_\_add_dict_tables.sql |

### 数据库表命名规范

- 表名前缀：`sys_`（系统模块）
- 命名格式：snake_case
- 示例：`sys_user`, `sys_role`, `sys_menu`
