## aiperm 技术栈专精

### 核心框架组件

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 21 | 编程语言 |
| Spring Boot | 3.5.11 | 基础框架 |
| Spring JdbcClient | - | 数据库操作（替代 MyBatis-Plus） |
| Sa-Token | 1.44.0 | 权限认证 |
| Redis | 7.x | 缓存/Session |
| Flyway | - | 数据库版本管理 |
| SpringDoc | 2.8.3 | API 文档 |
| Spring AI MCP | 1.1.2 | MCP Server |
| Hutool | 5.8.34 | 工具类 |
| FastExcel | 1.3.0 | Excel 导入导出 |

### 数据库操作规范

- **JdbcClient**: 使用 Spring JdbcClient 进行数据库操作
- **BaseRepository**: 通用 Repository 基类，提供基础 CRUD
- **SqlBuilder**: SQL 条件构建器，支持动态条件拼接
- **分页查询**: 使用 `PageResult<T>`

### 核心组件

| 组件 | 说明 |
|------|------|
| `BaseEntity` | 实体基类（id, createTime, updateTime, createBy, updateBy, deleted, version） |
| `BaseRepository<T>` | Repository 基类，提供 findById、deleteById、queryPage 等方法 |
| `SqlBuilder` | SQL 条件构建器，支持 likeIf、whereIf、inIf 等动态条件 |
| `R<T>` | 统一响应封装 |
| `PageResult<T>` | 分页结果封装（total, list, pageNum, pageSize, pages） |
| `Views` | DTO 验证分组（Create、Update、Query） |

### 业务异常处理

- **BusinessException**: 统一业务异常
- **参数校验**: JSR-303 注解 + @JsonView 分组验证
- **全局异常**: GlobalExceptionHandler 统一处理

### API 接口规范

- **权限控制**: @SaCheckPermission、@SaCheckLogin 注解
- **操作日志**: @Log 注解
- **统一响应**: 使用 R<T> 包装结果
- **分页响应**: 使用 PageResult<T>

### 常用工具类

- **Hutool**: StrUtil、CollUtil、DateUtil、BeanUtil
- **Sa-Token**: StpUtil（获取登录用户信息）
- **BCrypt**: 密码加密（Hutool 提供）

## ⚠️ 重要：已废弃的技术

| 已废弃 | 替代方案 |
|--------|----------|
| MyBatis-Plus | Spring JdbcClient + BaseRepository |
| @TableName、@TableField | 无需注解，Entity 只需继承 BaseEntity |
| Mapper 接口 | Repository 类 |
| ServiceImpl | 简单 Service 类 |
| MapStruct-Plus | 手动 toVO 转换方法 |
| 多个 Request DTO | 单一 DTO + @JsonView 分组 |
