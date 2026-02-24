## 质量检查清单

### 开发前检查
- [ ] 需求分析完整明确
- [ ] 数据库表结构设计合理
- [ ] API接口设计规范
- [ ] 权限控制考虑周全

### 代码规范检查
- [ ] **禁止使用 @Autowired**，使用 `@RequiredArgsConstructor` + `private final`
- [ ] **Entity 放在 entity 包**，继承 BaseEntity
- [ ] **DTO 用于接收请求参数**，VO 用于返回数据
- [ ] **所有字段遵循驼峰命名**（camelCase）
- [ ] **统一响应使用 R<T>**，分页响应使用 PageResult<T>

### 开发中检查
- [ ] 遵循 aiperm 代码规范
- [ ] 使用统一异常处理
- [ ] 添加必要的日志记录
- [ ] 实现数据权限控制

### 开发后检查
- [ ] 单元测试覆盖充分
- [ ] API文档更新完整
- [ ] 代码审查通过
- [ ] 功能测试达标
- [ ] **前端 API 是否已生成**（运行 `pnpm run generate:api`）

### API 开发检查清单
- [ ] 后端 API 是否已开发完成？
- [ ] 是否运行了 `pnpm run generate:api`？
- [ ] 是否查看了 `src/models/` 中的类型定义？
- [ ] 是否使用了生成的 API 函数？
- [ ] IDE 是否显示类型错误？

### 前端对接检查清单
- [ ] 是否使用了 Orval 生成的 API？
- [ ] API 返回类型是否正确（R<T>、 PageResult<T>）
- [ ] 埥询参数是否使用了生成的类型
