---
name: aiperm-dev
description: aiperm RBAC权限管理系统功能开发技能，专门用于开发基于aiperm框架的业务功能。当需要进行功能开发、模块创建、业务逻辑实现、数据库设计、API接口开发时激活。触发关键词：功能开发、模块开发、业务实现、新增功能、功能模块、开发功能、业务功能、创建模块、权限管理、菜单管理、角色管理、用户管理、字典管理、部门管理、岗位管理、Controller开发、Service开发、Repository开发、Entity开发、DTO开发、VO开发、API接口开发、REST接口、数据库设计、表结构、SQL、建表、DDL、DML、创建表、修改表、ALTER TABLE、Orval、API生成
---

# aiperm RBAC 权限管理系统开发专家

## 职责定位

作为 aiperm RBAC 权限管理系统的**功能开发主导技能**，专注于基于 aiperm 框架的完整业务功能开发，从需求分析到代码实现的全流程支持。

## 项目模块结构（当前）

```
backend/src/main/java/com/devlovecode/aiperm/
├── common/
│   ├── annotation/         @Log 注解
│   ├── aspect/             LogAspect 切面、LogEvent 事件
│   ├── domain/             BaseEntity, R, PageResult, Views
│   ├── enums/              ErrorCode, OperType
│   ├── exception/          BusinessException, GlobalExceptionHandler
│   └── repository/         BaseRepository, SqlBuilder
├── config/
│   ├── SaTokenConfig.java
│   └── WebMvcConfig.java
└── modules/
    ├── auth/               认证模块（登录/验证码）
    ├── log/                操作日志模块（SysOperLog）
    ├── oss/                文件存储模块（本地/阿里云）
    └── system/             系统管理模块
        ├── entity/         SysUser, SysRole, SysMenu, SysDept, SysPost
        │                   SysDictType, SysDictData
        ├── repository/     各 Repository 类（继承 BaseRepository）
        ├── service/        各 Service 类
        ├── controller/     各 REST Controller
        ├── dto/            数据传输对象（含验证分组）
        └── vo/             响应 VO

backend/src/main/resources/
├── db/migration/           Flyway 迁移脚本
└── application.yaml        配置文件
```

## 技术栈说明

| 技术 | 说明 |
|------|------|
| Spring JdbcClient | 数据库操作（替代 MyBatis-Plus） |
| BaseRepository | 通用 Repository 基类，提供基础 CRUD |
| SqlBuilder | SQL 条件构建器 |
| Views | DTO 验证分组（Create/Update/Query） |

## 新业务模块开发 6 步流程

### Step 1：建表（Flyway 迁移脚本）

在 `backend/src/main/resources/db/migration/` 创建 `Vx.x.x__描述.sql`：

```sql
CREATE TABLE IF NOT EXISTS `sys_xxx` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(100) NOT NULL COMMENT '名称',
    -- 业务字段...
    `deleted`     TINYINT     DEFAULT 0,
    `version`     INT         DEFAULT 0,
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `create_by`   VARCHAR(50) DEFAULT NULL,
    `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`   VARCHAR(50) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='xxx表';
```

### Step 2：Entity 实体类

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class SysXxx extends BaseEntity {

    private String name;

    private Integer status;

    private String remark;
}
```

**关键点：**
- 继承 `BaseEntity`（自动获得 id, createTime, updateTime, createBy, updateBy, deleted, version）
- 不需要任何 ORM 注解（@TableName, @TableField 等）

### Step 3：Repository 数据访问层

```java
@Repository
public class XxxRepository extends BaseRepository<SysXxx> {

    public XxxRepository(JdbcClient db) {
        super(db, "sys_xxx", SysXxx.class);
    }

    /**
     * 插入
     */
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

    /**
     * 更新
     */
    public int update(SysXxx entity) {
        String sql = """
            UPDATE sys_xxx
            SET name = :name, status = :status, remark = :remark,
                update_time = :updateTime, update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("name", entity.getName())
                .param("status", entity.getStatus())
                .param("remark", entity.getRemark())
                .param("updateTime", LocalDateTime.now())
                .param("updateBy", entity.getUpdateBy())
                .param("id", entity.getId())
                .update();
    }

    /**
     * 分页查询（使用 SqlBuilder）
     */
    public PageResult<SysXxx> queryPage(String name, Integer status, int pageNum, int pageSize) {
        SqlBuilder sb = new SqlBuilder();
        sb.likeIf(name != null && !name.isBlank(), "name", name)
          .whereIf(status != null, "status = ?", status);

        return queryPage(sb.getWhereClause(), sb.getParams(), pageNum, pageSize);
    }
}
```

**BaseRepository 提供的方法：**
- `findById(Long id)` - 根据 ID 查询
- `findAll()` - 查询所有
- `deleteById(Long id)` - 软删除
- `count()` - 统计总数
- `existsById(Long id)` - 检查是否存在
- `queryPage(...)` - 通用分页查询

### Step 4：Service 业务逻辑层

```java
@Service
@RequiredArgsConstructor
public class XxxService {

    private final XxxRepository xxxRepo;

    /**
     * 分页查询
     */
    public PageResult<XxxVO> queryPage(XxxDTO dto) {
        PageResult<SysXxx> result = xxxRepo.queryPage(
                dto.getName(), dto.getStatus(),
                dto.getPage(), dto.getPageSize()
        );
        return result.map(this::toVO);
    }

    /**
     * 查询详情
     */
    public XxxVO findById(Long id) {
        return xxxRepo.findById(id)
                .map(this::toVO)
                .orElseThrow(() -> new BusinessException("数据不存在"));
    }

    /**
     * 创建
     */
    @Transactional
    public Long create(XxxDTO dto) {
        SysXxx entity = new SysXxx();
        entity.setName(dto.getName());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        entity.setRemark(dto.getRemark());
        entity.setCreateBy(getCurrentUsername());

        xxxRepo.insert(entity);
        return entity.getId();
    }

    /**
     * 更新
     */
    @Transactional
    public void update(Long id, XxxDTO dto) {
        SysXxx entity = xxxRepo.findById(id)
                .orElseThrow(() -> new BusinessException("数据不存在"));

        entity.setName(dto.getName());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        entity.setUpdateBy(getCurrentUsername());

        xxxRepo.update(entity);
    }

    /**
     * 删除
     */
    @Transactional
    public void delete(Long id) {
        if (!xxxRepo.existsById(id)) {
            throw new BusinessException("数据不存在");
        }
        xxxRepo.deleteById(id);
    }

    // ========== 私有方法 ==========

    private XxxVO toVO(SysXxx entity) {
        XxxVO vo = new XxxVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private String getCurrentUsername() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            return "system";
        }
    }
}
```

### Step 5：Controller（加权限和日志注解）

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

    @Operation(summary = "查询详情")
    @SaCheckPermission("system:xxx:list")
    @GetMapping("/{id}")
    public R<XxxVO> detail(@PathVariable Long id) {
        return R.ok(xxxService.findById(id));
    }

    @Operation(summary = "创建")
    @SaCheckPermission("system:xxx:create")
    @Log(title = "xxx管理", operType = OperType.CREATE)
    @PostMapping
    public R<Long> create(@RequestBody @Validated({Default.class, Views.Create.class}) XxxDTO dto) {
        return R.ok(xxxService.create(dto));
    }

    @Operation(summary = "更新")
    @SaCheckPermission("system:xxx:update")
    @Log(title = "xxx管理", operType = OperType.UPDATE)
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id,
                          @RequestBody @Validated({Default.class, Views.Update.class}) XxxDTO dto) {
        xxxService.update(id, dto);
        return R.ok();
    }

    @Operation(summary = "删除")
    @SaCheckPermission("system:xxx:delete")
    @Log(title = "xxx管理", operType = OperType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        xxxService.delete(id);
        return R.ok();
    }
}
```

### Step 6：DTO 和 VO

```java
// DTO（带验证分组）
@Data
@Schema(description = "xxx数据")
public class XxxDTO {

    // ========== 分页查询参数（仅 Query 场景）==========

    @JsonView(Views.Query.class)
    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @JsonView(Views.Query.class)
    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;

    // ========== 业务字段（多场景复用）==========

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "名称")
    @NotBlank(message = "名称不能为空", groups = {Views.Create.class, Views.Update.class})
    @Size(max = 100, message = "名称不能超过100个字符")
    private String name;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "备注")
    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}

// VO（响应对象）
@Data
@Schema(description = "xxx响应")
public class XxxVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
```

### Step 7：前端生成 API

```bash
# 确保后端服务已启动
cd backend && ./gradlew bootRun

# 生成前端 API 客户端（类型安全）
cd frontend && pnpm run generate:api
```

## 参照模板

**以字典管理模块为标准参照（最完整的示例）：**

| 层 | 文件路径 |
|----|---------|
| Entity | `modules/system/entity/SysDictType.java` |
| Repository | `modules/system/repository/DictTypeRepository.java` |
| Service | `modules/system/service/DictTypeService.java` |
| Controller | `modules/system/controller/SysDictTypeController.java` |
| DTO | `modules/system/dto/DictTypeDTO.java` |
| VO | `modules/system/vo/DictTypeVO.java` |
| Flyway SQL | `resources/db/migration/V2.1.0__add_dict_tables.sql` |

## 命名规范完整表格

| 层 | 类名格式 | 示例 |
|----|---------|------|
| Entity | `Sys{模块}` | `SysDictType` |
| Repository | `{模块}Repository` | `DictTypeRepository` |
| Service | `{模块}Service` | `DictTypeService` |
| Controller | `Sys{模块}Controller` | `SysDictTypeController` |
| DTO | `{模块}DTO` | `DictTypeDTO` |
| VO | `{模块}VO` | `DictTypeVO` |
| Flyway脚本 | `Vx.x.x__{描述}.sql` | `V2.1.0__add_dict_tables.sql` |

## SqlBuilder 使用说明

```java
SqlBuilder sb = new SqlBuilder();

// LIKE 模糊查询（条件满足时添加）
sb.likeIf(name != null && !name.isBlank(), "name", name);

// 精确条件（条件满足时添加）
sb.whereIf(status != null, "status = ?", status);

// IN 条件
sb.inIf(ids != null && !ids.isEmpty(), "id", ids);

// 获取 WHERE 子句（带 AND 前缀）
String whereClause = sb.getWhereClause();

// 获取参数列表
List<Object> params = sb.getParams();
```

## 注解使用规范

### 权限注解

```java
@SaCheckLogin                                    // Controller 类级别：要求登录
@SaCheckPermission("system:user:list")           // 方法级别：指定权限码
@SaCheckRole("admin")                            // 方法级别：指定角色
```

权限码格式：`模块:资源:操作`（如 `system:dict:create`、`log:oper:delete`）

### @Log 操作日志注解

```java
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.enums.OperType;

@Log(title = "字典类型管理", operType = OperType.QUERY)    // 查询
@Log(title = "字典类型管理", operType = OperType.CREATE)   // 新增
@Log(title = "字典类型管理", operType = OperType.UPDATE)   // 修改
@Log(title = "字典类型管理", operType = OperType.DELETE)   // 删除
@Log(title = "文件管理",     operType = OperType.UPLOAD)   // 上传
```

日志自动异步写入 `sys_oper_log` 表，不影响主业务性能。

### DTO 验证分组（Views）

```java
// 使用 @JsonView 标记字段在哪些场景可见
@JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
private String name;

// 使用 @NotBlank 等注解的 groups 指定在哪些场景生效
@NotBlank(message = "名称不能为空", groups = {Views.Create.class, Views.Update.class})
private String name;

// Controller 中使用 @Validated 指定验证组
@Validated({Default.class, Views.Create.class}) XxxDTO dto
```

## 核心规范（禁止事项）

1. **禁止使用 `@Autowired`**，统一使用 `@RequiredArgsConstructor` + `private final`
2. **禁止手写前端 API 调用**，必须用 `pnpm run generate:api` 生成
3. **禁止直接修改 Flyway 已执行的脚本**，变更必须新建版本号更高的迁移文件
4. **禁止在 Entity 中写业务逻辑**，Entity 只是数据载体
5. **禁止在 Controller 中写复杂业务逻辑**，复杂逻辑放在 Service 中
6. **禁止使用 MyBatis-Plus 注解**，项目已迁移到 JdbcClient

## 统一响应格式

```java
R.ok()              // 成功，无数据
R.ok(data)          // 成功，带数据
R.fail()            // 失败，无消息
R.fail("消息")       // 失败，带消息
PageResult.of(total, list, page, pageSize)  // 分页结果封装
PageResult.empty(page, pageSize)            // 空分页结果
result.map(this::toVO)                      // 分页结果转换
```

## 前后端 API 联调完整流程

```
后端开发 API
    ↓
启动服务：cd backend && ./gradlew bootRun
    ↓
访问 Swagger：http://localhost:8080/swagger-ui.html（确认接口正确）
    ↓
生成前端 API：cd frontend && pnpm run generate:api
    ↓
查看 frontend/src/models/ 中的 TypeScript 类型定义
    ↓
前端使用生成的 API 函数（类型安全，无需手写）
```

## 开发检查清单

开发新模块前逐项确认：

- [ ] 参照字典管理模块（`SysDictType`）的完整代码结构
- [ ] Flyway 迁移脚本版本号正确（比现有最高版本大）
- [ ] Entity 继承 `BaseEntity`，无需任何注解
- [ ] Repository 继承 `BaseRepository`，构造函数传入表名和实体类
- [ ] Controller 类级别加 `@SaCheckLogin`
- [ ] Controller 所有写操作（POST/PUT/DELETE）加 `@Log` 注解
- [ ] Controller 方法级别加 `@SaCheckPermission("模块:资源:操作")`
- [ ] 依赖注入全部使用 `@RequiredArgsConstructor` + `private final`
- [ ] DTO 使用 `@JsonView` 和验证分组
- [ ] 后端完成后运行 `pnpm run generate:api` 同步前端
- [ ] IDE 中无编译错误和类型错误
