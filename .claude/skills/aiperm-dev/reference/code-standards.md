## 代码规范

### ⚠️ 依赖注入规范（最重要）

- **禁止使用 @Autowired**：全项目使用 Lombok，必须使用 `@RequiredArgsConstructor` + `private final` 字段注入

```java
// ❌ 错误：禁止使用 @Autowired
@Service
public class UserService {
    @Autowired
    private UserRepository userRepo;
}

// ✅ 正确：使用 Lombok 的 @RequiredArgsConstructor
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo; // final 字段自动注入
}
```

### 对象分层规范

#### 1. Entity（实体类）
- **位置**: `entity` 包
- **用途**: 对应数据库表结构
- **命名**: `Sys{名称}` (如 SysUser)
- **特征**: 继承 BaseEntity，无需任何注解

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity {

    private String username;

    private Integer status;

    private String remark;
}
```

#### 2. DTO（数据传输对象）
- **位置**: `dto` 包
- **用途**: 接收请求参数，使用 @JsonView 分组验证
- **命名**: `{名称}DTO`
- **特征**: 包含分页参数和业务字段，使用 @JsonView 标记场景

```java
@Data
@Schema(description = "用户数据")
public class UserDTO {

    // 分页参数（仅 Query 场景）
    @JsonView(Views.Query.class)
    private Integer page = 1;

    @JsonView(Views.Query.class)
    private Integer pageSize = 10;

    // 业务字段（多场景复用）
    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @NotBlank(message = "用户名不能为空", groups = {Views.Create.class, Views.Update.class})
    private String username;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    private Integer status;
}
```

#### 3. VO（视图对象）
- **位置**: `vo` 包
- **用途**: 返回给客户端的数据
- **命名**: `{名称}VO`

```java
@Data
@Schema(description = "用户响应")
public class UserVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
```

#### 4. 数据流转规则

```
请求 → Controller → DTO → Service → Entity → Repository → 数据库
                                    ↓
数据库 → Repository → Entity → Service → VO → Controller → 响应
```

### Repository 规范

```java
@Repository
public class XxxRepository extends BaseRepository<SysXxx> {

    public XxxRepository(JdbcClient db) {
        super(db, "sys_xxx", SysXxx.class);
    }

    public void insert(SysXxx entity) {
        String sql = """
            INSERT INTO sys_xxx (name, status, deleted, version, create_time, create_by)
            VALUES (:name, :status, 0, 0, :createTime, :createBy)
            """;
        db.sql(sql)
                .param("name", entity.getName())
                .param("status", entity.getStatus())
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

### Service 规范

```java
@Service
@RequiredArgsConstructor
public class XxxService {

    private final XxxRepository xxxRepo;

    public PageResult<XxxVO> queryPage(XxxDTO dto) {
        PageResult<SysXxx> result = xxxRepo.queryPage(
                dto.getName(), dto.getStatus(), dto.getPage(), dto.getPageSize()
        );
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
        try {
            return StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            return "system";
        }
    }
}
```

### Controller 规范

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

### 注解使用规范

#### 权限注解

```java
@SaCheckLogin                                    // Controller 类级别：要求登录
@SaCheckPermission("system:user:list")           // 方法级别：指定权限码
@SaCheckRole("admin")                            // 方法级别：指定角色
```

权限码格式：`模块:资源:操作`（如 `system:dict:create`、`log:oper:delete`）

#### @Log 操作日志注解

```java
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.enums.OperType;

@Log(title = "字典类型管理", operType = OperType.QUERY)    // 查询
@Log(title = "字典类型管理", operType = OperType.CREATE)   // 新增
@Log(title = "字典类型管理", operType = OperType.UPDATE)   // 修改
@Log(title = "字典类型管理", operType = OperType.DELETE)   // 删除
```

#### DTO 验证分组（Views）

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

### 核心规范（禁止事项）

1. **禁止使用 `@Autowired`**，统一使用 `@RequiredArgsConstructor` + `private final`
2. **禁止手写前端 API 调用**，必须用 `pnpm run generate:api` 生成
3. **禁止直接修改 Flyway 已执行的脚本**，变更必须新建版本号更高的迁移文件
4. **禁止在 Entity 中写业务逻辑**，Entity 只是数据载体
5. **禁止在 Controller 中写复杂业务逻辑**，复杂逻辑放在 Service 中
6. **禁止使用 MyBatis-Plus 注解**（@TableName、@TableField 等），项目已迁移到 JdbcClient
7. **禁止使用多个 Request DTO**，使用单一 DTO + @JsonView 分组验证

### 统一响应格式

```java
R.ok()              // 成功，无数据
R.ok(data)          // 成功，带数据
R.fail("消息")       // 失败，带消息
PageResult.of(total, list, pageNum, pageSize)  // 分页结果封装
result.map(this::toVO)                          // 分页结果转换
```
