## 项目结构速查

### 整体架构

```
aiperm/                                   # 项目根目录
├── src/main/java/com/devlovecode/aiperm/
│   ├── AipermApplication.java            # 启动类
│   │
│   ├── common/                           # 公共组件
│   │   ├── domain/                       # 基础实体
│   │   │   ├── BaseEntity.java           # 实体基类
│   │   │   ├── R.java                    # 统一响应封装
│   │   │   └── PageResult.java           # 分页结果封装
│   │   ├── enums/                        # 枚举
│   │   │   └── ErrorCode.java            # 错误码枚举
│   │   ├── exception/                    # 异常处理
│   │   │   ├── BusinessException.java    # 业务异常
│   │   │   └── GlobalExceptionHandler.java # 全局异常处理
│   │   └── handler/                      # 全局处理器
│   │
│   ├── config/                           # 配置类
│   │   ├── SaTokenConfig.java            # Sa-Token 权限配置
│   │   ├── SaTokenInterface.java         # 权限接口实现
│   │   ├── MybatisPlusConfig.java        # MyBatis-Plus 配置
│   │   ├── MyMetaObjectHandler.java      # 字段自动填充
│   │   └── WebMvcConfig.java             # Web MVC 配置
│   │
│   └── modules/                          # 业务模块（按领域划分）
│       ├── auth/                         # 认证模块
│       │   ├── dto/request/              # 请求 DTO
│       │   │   └── LoginRequest.java     # 登录请求
│       │   └── vo/                       # 视图对象
│       │       ├── LoginVO.java          # 登录响应
│       │       └── CaptchaVO.java        # 验证码响应
│       │
│       └── system/                       # 系统管理模块
│           ├── entity/                   # 实体类
│           │   ├── SysUser.java          # 用户实体
│           │   ├── SysRole.java          # 角色实体
│           │   ├── SysPermission.java    # 权限实体
│           │   ├── SysMenu.java          # 菜单实体
│           │   ├── SysDept.java          # 部门实体
│           │   └── SysPost.java          # 岗位实体
│           ├── mapper/                   # MyBatis Mapper
│           │   ├── SysUserMapper.java
│           │   └── ...
│           ├── service/                  # 服务层
│           │   ├── ISysUserService.java  # 服务接口
│           │   └── impl/
│           │       └── SysUserServiceImpl.java # 服务实现
│           ├── controller/               # REST 控制器
│           │   ├── SysUserController.java
│           │   └── ...
│           ├── dto/request/              # 请求 DTO
│           │   ├── UserCreateRequest.java
│           │   ├── UserUpdateRequest.java
│           │   └── UserQueryRequest.java
│           ├── vo/                       # 视图对象
│           │   ├── UserVO.java
│           │   └── ...
│           └── converter/                # 对象转换器
│               ├── UserConverter.java    # MapStruct-Plus 转换器
│               └── ...
│
├── src/main/resources/
│   ├── application.yml                   # 主配置文件
│   └── mapper/                           # MyBatis XML 映射文件
│
├── frontend/                             # 前端项目
│   ├── src/
│   │   ├── api/                          # API 层（Orval 自动生成）
│   │   │   ├── generated.ts              # 生成的 API 函数
│   │   │   └── index.ts                  # API 导出
│   │   ├── models/                       # TypeScript 类型（Orval 生成）
│   │   ├── stores/                       # Pinia 状态管理
│   │   ├── router/                       # Vue Router 路由
│   │   ├── views/                        # 页面视图
│   │   ├── components/                   # 公共组件
│   │   ├── utils/                        # 工具函数
│   │   │   └── api-mutator.ts            # Axios 请求拦截器
│   │   └── main.ts                       # 应用入口
│   ├── orval.config.ts                   # Orval 配置
│   └── package.json
│
├── script/                               # SQL 脚本
│   └── {module}/
│       └── sql/                          # 业务表 SQL
│
├── build.gradle                          # Gradle 构建配置
└── settings.gradle                       # Gradle 设置
```

### 模块说明

#### common（公共模块）
- **domain**: 基础实体类，包含 BaseEntity、R、PageResult
- **enums**: 枚举类，如 ErrorCode
- **exception**: 异常类，统一异常处理
- **handler**: 全局处理器

#### config（配置模块）
- **SaTokenConfig**: Sa-Token 权限框架配置
- **SaTokenInterface**: 权限接口实现（获取用户权限、角色）
- **MybatisPlusConfig**: MyBatis-Plus 配置（分页插件等）
- **MyMetaObjectHandler**: 字段自动填充处理器
- **WebMvcConfig**: Web MVC 配置

#### modules（业务模块）

**auth 模块**（认证）
- 登录、登出、验证码
- Token 管理

**system 模块**（系统管理）
- 用户管理、角色管理、权限管理
- 菜单管理、部门管理、岗位管理

### 模块命名规范

```
模块目录结构：
modules/{module}/
├── entity/           # 实体类（继承 BaseEntity）
├── mapper/           # MyBatis Mapper 接口
├── service/          # 服务层接口
│   └── impl/         # 服务层实现
├── controller/       # REST 控制器
├── dto/              # 数据传输对象
│   └── request/      # 请求 DTO
├── vo/               # 视图对象（返回数据）
└── converter/        # 对象转换器（MapStruct-Plus）
```

### 类命名规范

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| Entity | Sys{名称} | SysUser, SysRole |
| Mapper | {Entity}Mapper | SysUserMapper |
| Service 接口 | I{Entity}Service | ISysUserService |
| Service 实现 | {Entity}ServiceImpl | SysUserServiceImpl |
| Controller | {Entity}Controller | SysUserController |
| Request DTO | {操作}{Entity}Request | UserCreateRequest |
| VO | {Entity}VO | UserVO |
| Converter | {Entity}Converter | UserConverter |

### 数据库表命名规范

- 表名前缀：`sys_`（系统模块）、业务模块自定义前缀
- 命名格式：snake_case
- 示例：`sys_user`, `sys_role`, `sys_menu`
