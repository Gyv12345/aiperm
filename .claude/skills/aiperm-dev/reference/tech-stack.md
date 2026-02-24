## aiperm 技术栈专精

### 核心框架组件
- **Spring Boot 3.5.11**: 基础框架
- **MyBatis-Plus 3.5.9**: ORM框架
- **Sa-Token 1.39.0**: 安全认证
- **Redis**: 缓存
- **MapStruct-Plus 1.4.6**: 对象转换器
- **Hutool 5.8.34**: 工具库
- **SpringDoc 2.8.3**: API 文档

### 数据库操作规范
- **查询构造器**: 使用 `Wrappers.<Entity>lambdaQuery()`
- **逻辑删除**: 框架自动处理，无需手动添加条件
- **分页查询**: 使用 `Page` 和 `PageResult`
- **缓存管理**: 使用 Redis

### 业务异常处理
- **BusinessException**: 统一业务异常处理
- **参数校验**: JSR-303 注解 + @Valid
- **全局异常**: GlobalExceptionHandler 统一处理

### API接口规范
- **权限控制**: @SaCheckPermission 注解
- **统一响应**: 使用 R<T> 包装结果
- **VO设计**: 返回值必须使用VO对象
- **分页响应**: 使用 PageResult<T>

### 常用工具类
- **MyBatis-Plus**: 内置 CRUD 方法
- **Hutool**: ObjectUtil、 StrUtil、 CollUtil、 DateUtil、 BeanUtil
- **Sa-Token**: SaManager、 StpUtil

## 依赖版本

| 依赖 | 版本 |
|------|------|
| Spring Boot | 3.5.11 |
| Java | 21 |
| Sa-Token | 1.39.0 |
| MyBatis-Plus | 3.5.9 |
| MapStruct-Plus | 1.4.6 |
| Hutool | 5.8.34 |
| SpringDoc | 2.8.3 |
