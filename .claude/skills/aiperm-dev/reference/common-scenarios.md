## 常见开发场景

### 1. 完整 CRUD 功能开发

#### Controller

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

#### Service

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
        if (!xxxRepo.existsById(id)) {
            throw new BusinessException("数据不存在");
        }
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

#### Repository

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

### 2. SqlBuilder 使用说明

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

### 3. BaseRepository 提供的方法

| 方法 | 说明 |
|------|------|
| `findById(Long id)` | 根据 ID 查询 |
| `findAll()` | 查询所有 |
| `deleteById(Long id)` | 软删除 |
| `count()` | 统计总数 |
| `existsById(Long id)` | 检查是否存在 |
| `queryPage(...)` | 通用分页查询 |
