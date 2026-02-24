## 常见开发场景

### 1. CRUD功能开发
```java
@Slf4j
@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户相关接口")
@Validated
public class SysUserController {

    private final ISysUserService sysUserService;

    @GetMapping("/page")
    @Operation(summary = "分页查询用户列表")
    @SaCheckPermission("system:user:list")
    public R<PageResult<SysUser>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        PageResult<SysUser> result = sysUserService.page(pageNum, pageSize);
        return R.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户")
    @SaCheckPermission("system:user:query")
    public R<SysUser> getById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        return R.ok(user);
    }

    @PostMapping
    @Operation(summary = "创建用户")
    @SaCheckPermission("system:user:add")
    public R<Void> create(@Valid @RequestBody SysUser user) {
        boolean success = sysUserService.create(user);
        return success ? R.ok() : R.fail();
    }

    @PutMapping
    @Operation(summary = "更新用户")
    @SaCheckPermission("system:user:edit")
    public R<Void> update(@Valid @RequestBody SysUser user) {
        boolean success = sysUserService.update(user);
        return success ? R.ok() : R.fail();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @SaCheckPermission("system:user:remove")
    public R<Void> delete(@PathVariable Long id) {
        boolean success = sysUserService.delete(id);
        return success ? R.ok() : R.fail();
    }
}
```

### 2. 业务状态管理
```java
// 状态枚举
public enum UserStatus {
    PENDING(0, "待处理"),
    ACTIVE(2, "进行中"),
    COMPLETED(3, "已完成");

    private final Integer code;
    private final String description;

    UserStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    // 状态变更服务
    public void changeStatus(Long userId, UserStatus status) {
        // 状态变更逻辑和业务规则验证
    }
}
```

### 3. 分页查询
```java
@GetMapping("/page")
public R<PageResult<SysUser>> page(
    @RequestParam(defaultValue = "1") Long pageNum,
    @RequestParam(defaultValue = "10") Long pageSize,
    @RequestParam(required = false) String username,
    @RequestParam(required = false) Integer status) {
    // 构建查询条件
    LambdaQueryWrapper<SysUser> wrapper = Wrappers.<SysUser>lambdaQuery()
        .like(StringUtils.isNotBlank(username), SysUser::getUsername, username)
        .eq(status != null, SysUser::getStatus, status);

    // 分页查询
    Page<SysUser> page = new Page<>(pageNum, pageSize);
    sysUserMapper.selectPage(wrapper, page);

    return PageResult.of(page);
}
```
