## 质量检查清单

### 开发前检查

- [ ] 需求分析完整明确
- [ ] 数据库表结构设计合理
- [ ] API 接口设计规范
- [ ] 权限控制考虑周全

### 代码规范检查

- [ ] **禁止使用 @Autowired**，使用 `@RequiredArgsConstructor` + `private final`
- [ ] **Entity 放在 entity 包**，继承 BaseEntity，无需任何注解
- [ ] **DTO 使用 @JsonView 分组验证**，而非多个 Request DTO
- [ ] **VO 用于返回数据**
- [ ] **统一响应使用 R<T>**，分页响应使用 PageResult<T>
- [ ] **禁止使用 MyBatis-Plus 注解**（@TableName、@TableField 等）

### 后端开发检查

- [ ] Repository 继承 `BaseRepository`
- [ ] Controller 类级别加 `@SaCheckLogin`
- [ ] Controller 写操作加 `@Log` + `@SaCheckPermission`
- [ ] DTO 使用 `@JsonView` 标记场景
- [ ] 依赖注入使用 `@RequiredArgsConstructor` + `private final`

### 前后端联调检查

- [ ] 后端 API 是否已开发完成？
- [ ] 后端服务是否已启动？
- [ ] 是否运行了 `pnpm run generate:api`？
- [ ] 是否查看了 `src/models/` 中的类型定义？
- [ ] 是否使用了生成的 API 函数？
- [ ] 前端分页使用 `list` 和 `pageNum`（不是 `records` 和 `page`）

### Flyway 检查

- [ ] 迁移脚本版本号正确（比现有最高版本大）
- [ ] 表名和列名使用反引号
- [ ] 包含所有公共字段（deleted、version、create_time 等）
- [ ] 添加了表注释和列注释

### 最终检查

- [ ] IDE 中无编译错误
- [ ] 前端无 TypeScript 类型错误
- [ ] 功能测试达标
