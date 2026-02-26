# Backend - aiperm 后端开发规范

> 本文件定义 aiperm 后端开发的所有规范和约定。AI 助手在开发后端代码时必须遵循此文档。

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 21 | 编程语言 |
| Spring Boot | 3.5.11 | 基础框架 |
| Sa-Token | 1.44.0 | 权限认证 |
| Spring JdbcClient | - | 数据库操作（替代 MyBatis-Plus） |
| MySQL | 8.x | 关系数据库 |
| Redis | 7.x | 缓存/Session |
| Flyway | - | 数据库版本管理 |
| SpringDoc | 2.8.3 | API 文档 |
| Hutool | 5.8.34 | 工具类 |

## 目录结构

```
backend/src/main/java/com/devlovecode/aiperm/
├── common/                     # 公共组件
│   ├── annotation/             # 注解（@Log）
│   ├── aspect/                 # 切面（LogAspect）
│   ├── domain/                 # 基础类（BaseEntity, R, PageResult, Views）
│   ├── enums/                  # 枚举（ErrorCode, OperType）
│   ├── exception/              # 异常（BusinessException, GlobalExceptionHandler）
│   └── repository/             # 基础 Repository（BaseRepository, SqlBuilder）
├── config/                     # 配置类
│   ├── SaTokenConfig.java      # Sa-Token 配置
│   └── WebMvcConfig.java       # Web MVC 配置
└── modules/                    # 业务模块
    ├── auth/                   # 认证模块
    ├── system/                 # 系统管理
    ├── log/                    # 操作日志
    └── oss/                    # 对象存储
```

## 标准分层约定

| 层 | 类名规范 | 继承/实现 | 注解 |
|----|---------|----------|------|
| Entity | `SysXxx` | `extends BaseEntity` | 无需注解 |
| Repository | `XxxRepository` | `extends BaseRepository<Xxx>` | `@Repository` |
| Service | `XxxService` | - | `@Service` |
| Controller | `SysXxxController` | - | `@RestController` |
| DTO | `XxxDTO` | - | 含 `@JsonView` 和验证注解 |
| VO | `XxxVO` | - | - |

## 文件位置约定

```
modules/system/
├── entity/                     SysXxx.java
├── repository/                 XxxRepository.java
├── service/                    XxxService.java
├── controller/                 SysXxxController.java
├── dto/                        XxxDTO.java
└── vo/                         XxxVO.java

resources/
├── db/migration/               Vx.x.x__description.sql
└── application.yaml
```

## 新业务模块开发 6 步流程

### 1. 建表

创建 Flyway 迁移脚本 `backend/src/main/resources/db/migration/Vx.x.x__xxx.sql`

```sql
CREATE TABLE sys_xxx (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '名称',
    status INT DEFAULT 1 COMMENT '状态',
    remark VARCHAR(500) COMMENT '备注',
    deleted TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2. Entity

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class SysXxx extends BaseEntity {
    private String name;
    private Integer status;
    private String remark;
}
```

### 3. Repository

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

### 4. Service

```java
@Service
@RequiredArgsConstructor
public class XxxService {
    private final XxxRepository xxxRepo;

    public PageResult<XxxVO> queryPage(XxxDTO dto) {
        PageResult<SysXxx> result = xxxRepo.queryPage(
                dto.getName(), dto.getStatus(),
                dto.getPage(), dto.getPageSize()
        );
        return result.map(this::toVO);
    }

    public XxxVO findById(Long id) {
        return xxxRepo.findById(id)
                .map(this::toVO)
                .orElseThrow(() -> new BusinessException("数据不存在"));
    }

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

    @Transactional
    public void delete(Long id) {
        xxxRepo.deleteById(id);
    }

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

### 5. Controller

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

### 6. DTO/VO

```java
// DTO
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

    @JsonView({Views.Create.class, Views.Update.class})
    private String remark;
}

// VO
@Data
public class XxxVO {
    private Long id;
    private String name;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
}
```

## SqlBuilder 使用

```java
SqlBuilder sb = new SqlBuilder();
sb.likeIf(name != null, "name", name);           // LIKE 模糊查询
sb.whereIf(status != null, "status = ?", status); // 精确条件
sb.inIf(ids != null, "id", ids);                  // IN 条件

String whereClause = sb.getWhereClause();  // 获取 WHERE 子句
List<Object> params = sb.getParams();      // 获取参数列表
```

## 权限注解使用

```java
@SaCheckLogin                              // 检查登录
@SaCheckRole("admin")                      // 检查角色
@SaCheckPermission("system:user:create")   // 检查权限
```

## 权限码约定

格式：`模块:资源:操作`

| 示例 | 说明 |
|------|------|
| `system:user:list/create/update/delete` | 用户管理 |
| `system:role:list/create` | 角色管理 |
| `system:dict:list/create/update/delete` | 字典管理 |
| `log:oper:list/delete` | 操作日志 |
| `oss:file:upload/delete` | 文件管理 |

## @Log 注解使用

```java
@Log(title = "用户管理", operType = OperType.QUERY)   // 查询
@Log(title = "用户管理", operType = OperType.CREATE)  // 新增
@Log(title = "用户管理", operType = OperType.UPDATE)  // 修改
@Log(title = "用户管理", operType = OperType.DELETE)  // 删除
```

## 依赖注入规范

**必须使用构造函数注入：**

```java
// 正确
@Service
@RequiredArgsConstructor
public class XxxService {
    private final XxxRepository xxxRepo;
}

// 禁止
@Service
public class XxxService {
    @Autowired
    private XxxRepository xxxRepo;
}
```

## 统一响应格式

```java
R.ok(data)                        // 成功响应
R.ok()                            // 成功无数据
PageResult.of(total, list, page, pageSize)  // 分页
result.map(this::toVO)            // 分页转换
```

### PageResult 字段说明（重要！）

后端 `PageResult` 返回的 JSON 格式：

```json
{
  "total": 100,
  "list": [...],      // 数据列表，不是 records！
  "pageNum": 1,       // 当前页码，不是 page！
  "pageSize": 10,
  "pages": 10         // 总页数
}
```

**前端必须使用相同的字段名：**
- ✅ `list` - 数据列表
- ✅ `pageNum` - 当前页码
- ❌ `records` - 错误！
- ❌ `page` - 错误！

## 开发检查清单

- [ ] 建表 SQL 写入 Flyway 迁移文件
- [ ] Entity 继承 `BaseEntity`（无需注解）
- [ ] Repository 继承 `BaseRepository`
- [ ] Controller 类加 `@SaCheckLogin`
- [ ] 写操作加 `@Log` + `@SaCheckPermission`
- [ ] 依赖注入使用 `@RequiredArgsConstructor`
- [ ] 参照 `modules/system/entity/SysDictType.java` 作为模板
