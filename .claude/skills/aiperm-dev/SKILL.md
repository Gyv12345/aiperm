---
name: aiperm-dev
description: aiperm RBAC权限管理系统功能开发技能，专门用于开发基于aiperm框架的业务功能。当需要进行功能开发、模块创建、业务逻辑实现、数据库设计、API接口开发时激活。触发关键词：功能开发、模块开发、业务实现、新增功能、功能模块、开发功能、业务功能、创建模块、权限管理、菜单管理、角色管理、用户管理、字典管理、部门管理、岗位管理、Controller开发、Service开发、Repository开发、Entity开发、DTO开发、VO开发、API接口开发、REST接口、数据库设计、表结构、SQL、建表、DDL、DML、创建表、修改表、ALTER TABLE、Orval、API生成
---

# aiperm RBAC 权限管理系统开发专家

## 职责定位

作为 aiperm RBAC 权限管理系统的**功能开发主导技能**，专注于基于 aiperm 框架的完整业务功能开发。

## 技术栈

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

## 项目结构

```
backend/src/main/java/com/devlovecode/aiperm/
├── common/                     # 公共组件
│   ├── annotation/             @Log 注解
│   ├── aspect/                 LogAspect 切面
│   ├── domain/                 BaseEntity, R, PageResult, Views
│   ├── enums/                  ErrorCode, OperType
│   ├── exception/              BusinessException, GlobalExceptionHandler
│   └── repository/             BaseRepository, SqlBuilder
├── config/
│   ├── SaTokenConfig.java
│   └── WebMvcConfig.java
└── modules/                    # 业务模块
    ├── auth/                   认证模块
    ├── system/                 系统管理（用户、角色、菜单、部门、字典、岗位）
    ├── enterprise/             企业管理（配置、任务、消息、通知）
    ├── log/                    操作日志
    ├── oss/                    文件存储
    └── mcp/                    MCP 工具模块

backend/src/main/resources/
├── db/migration/               Flyway 迁移脚本
└── application.yaml            配置文件
```

## 新业务模块开发 6 步流程

### Step 1：建表（Flyway）

```sql
-- backend/src/main/resources/db/migration/Vx.x.x__xxx.sql
CREATE TABLE IF NOT EXISTS `sys_xxx` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(100) NOT NULL COMMENT '名称',
    `status`      INT         DEFAULT 0 COMMENT '状态',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted`     TINYINT     DEFAULT 0,
    `version`     INT         DEFAULT 0,
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `create_by`   VARCHAR(50) DEFAULT NULL,
    `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`   VARCHAR(50) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Step 2：Entity

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class SysXxx extends BaseEntity {
    private String name;
    private Integer status;
    private String remark;
}
```

### Step 3：Repository

```java
@Repository
public class XxxRepository extends BaseRepository<SysXxx> {

    public XxxRepository(JdbcClient db) {
        super(db, "sys_xxx", SysXxx.class);
    }

    public void insert(SysXxx entity) {
        String sql = """
            INSERT INTO sys_xxx (name, status, remark, deleted, version, create_time, create_by)
            VALUES (:name, :status, :remark, 0, 0, :createTime, :createBy)
            """;
        db.sql(sql)
                .param("name", entity.getName())
                .param("status", entity.getStatus())
                .param("remark", entity.getRemark())
                .param("createTime", LocalDateTime.now())
                .param("createBy", entity.getCreateBy())
                .update();
    }

    public PageResult<SysXxx> queryPage(String name, Integer status, int pageNum, int pageSize) {
        SqlBuilder sb = new SqlBuilder();
        sb.likeIf(name != null && !name.isBlank(), "name", name)
          .whereIf(status != null, "status = ?", status);
        return queryPage(sb.getWhereClause(), sb.getParams(), pageNum, pageSize);
    }
}
```

### Step 4：Service

```java
@Service
@RequiredArgsConstructor
public class XxxService {
    private final XxxRepository xxxRepo;

    public PageResult<XxxVO> queryPage(XxxDTO dto) {
        PageResult<SysXxx> result = xxxRepo.queryPage(dto.getName(), dto.getStatus(), dto.getPage(), dto.getPageSize());
        return result.map(this::toVO);
    }

    @Transactional
    public Long create(XxxDTO dto) {
        SysXxx entity = new SysXxx();
        entity.setName(dto.getName());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        entity.setCreateBy(getCurrentUsername());
        xxxRepo.insert(entity);
        return entity.getId();
    }

    private XxxVO toVO(SysXxx entity) {
        XxxVO vo = new XxxVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setStatus(entity.getStatus());
        return vo;
    }

    private String getCurrentUsername() {
        try { return StpUtil.getLoginIdAsString(); }
        catch (Exception e) { return "system"; }
    }
}
```

### Step 5：Controller

```java
@Tag(name = "xxx管理")
@RestController
@RequestMapping("/system/xxx")
@SaCheckLogin
@RequiredArgsConstructor
public class SysXxxController {
    private final XxxService xxxService;

    @Operation(summary = "分页查询")
    @SaCheckPermission("system:xxx:list")
    @Log(title = "xxx管理", operType = OperType.QUERY)
    @GetMapping
    public R<PageResult<XxxVO>> list(@Validated({Default.class, Views.Query.class}) XxxDTO dto) {
        return R.ok(xxxService.queryPage(dto));
    }

    @Operation(summary = "创建")
    @SaCheckPermission("system:xxx:create")
    @Log(title = "xxx管理", operType = OperType.CREATE)
    @PostMapping
    public R<Long> create(@RequestBody @Validated({Default.class, Views.Create.class}) XxxDTO dto) {
        return R.ok(xxxService.create(dto));
    }
}
```

### Step 6：DTO

```java
@Data
@Schema(description = "xxx数据")
public class XxxDTO {
    @JsonView(Views.Query.class)
    private Integer page = 1;

    @JsonView(Views.Query.class)
    private Integer pageSize = 10;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @NotBlank(message = "名称不能为空", groups = {Views.Create.class, Views.Update.class})
    private String name;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    private Integer status;
}
```

### Step 7：生成前端 API

```bash
cd frontend && pnpm run generate:api
```

## 命名规范

| 层 | 命名格式 | 示例 |
|----|---------|------|
| Entity | `Sys{模块}` | `SysDictType` |
| Repository | `{模块}Repository` | `DictTypeRepository` |
| Service | `{模块}Service` | `DictTypeService` |
| Controller | `Sys{模块}Controller` | `SysDictTypeController` |
| DTO | `{模块}DTO` | `DictTypeDTO` |
| VO | `{模块}VO` | `DictTypeVO` |

## SqlBuilder 使用

```java
SqlBuilder sb = new SqlBuilder();
sb.likeIf(name != null && !name.isBlank(), "name", name);     // LIKE 模糊查询
sb.whereIf(status != null, "status = ?", status);             // 精确条件
sb.inIf(ids != null && !ids.isEmpty(), "id", ids);            // IN 条件
String whereClause = sb.getWhereClause();                      // 获取 WHERE 子句
List<Object> params = sb.getParams();                          // 获取参数列表
```

## 核心规范（禁止事项）

1. **禁止使用 `@Autowired`**，统一使用 `@RequiredArgsConstructor` + `private final`
2. **禁止手写前端 API 调用**，必须用 `pnpm run generate:api` 生成
3. **禁止直接修改 Flyway 已执行的脚本**
4. **禁止使用 MyBatis-Plus 注解**（@TableName、@TableField 等）
5. **禁止使用多个 Request DTO**，使用单一 DTO + @JsonView 分组

## 统一响应格式

```java
R.ok(data)                                    // 成功响应
R.ok()                                        // 成功无数据
PageResult.of(total, list, pageNum, pageSize) // 分页
result.map(this::toVO)                        // 分页转换
```

## 前后端字段规范

后端 `PageResult` 返回格式：
```json
{
  "total": 100,
  "list": [...],
  "pageNum": 1,
  "pageSize": 10,
  "pages": 10
}
```

**前端必须使用：**
- ✅ `list` - 数据列表（不是 `records`）
- ✅ `pageNum` - 当前页码（不是 `page`）

## 参照模板

**以字典管理模块为标准参照：**

| 层 | 文件路径 |
|----|---------|
| Entity | `modules/system/entity/SysDictType.java` |
| Repository | `modules/system/repository/DictTypeRepository.java` |
| Service | `modules/system/service/DictTypeService.java` |
| Controller | `modules/system/controller/SysDictTypeController.java` |
| DTO | `modules/system/dto/DictTypeDTO.java` |
| VO | `modules/system/vo/DictTypeVO.java` |

## 开发检查清单

- [ ] Flyway 迁移脚本版本号正确
- [ ] Entity 继承 `BaseEntity`，无需注解
- [ ] Repository 继承 `BaseRepository`
- [ ] Controller 类级别加 `@SaCheckLogin`
- [ ] 写操作加 `@Log` + `@SaCheckPermission`
- [ ] 依赖注入使用 `@RequiredArgsConstructor` + `private final`
- [ ] DTO 使用 `@JsonView` 分组验证
- [ ] 运行 `pnpm run generate:api` 同步前端
