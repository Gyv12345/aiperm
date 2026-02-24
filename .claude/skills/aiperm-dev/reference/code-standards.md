## 代码规范

### ⚠️ 依赖注入规范（最重要）

- **禁止使用 @Autowired**：全项目使用 Lombok，必须使用 `@RequiredArgsConstructor` + `private final` 字段注入

```java
// ❌ 错误：禁止使用 @Autowired
@Service
public class SysUserServiceImpl {
    @Autowired
    private SysUserMapper sysUserMapper;
}

// ✅ 正确：使用 Lombok 的 @RequiredArgsConstructor
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements ISysUserService {

    private final SysUserMapper sysUserMapper; // final 字段自动注入
}
```

### 命名规范

- **类名**: 大驼峰命名 (PascalCase)
- **方法名**: 小驼峰命名 (camelCase)
- **常量**: 全大写下划线 (UPPER_SNAKE_CASE)
- **包名**: 全小写 (lowercase)

### 对象分层规范（⚠️ 重要）

#### 1. Entity(实体类)
- **位置**: `entity` 包
- **用途**: 对应数据库表结构，与数据库表一一映射
- **命名**: `Sys{名称}` (如 SysUser)
- **特征**: 继承 BaseEntity

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "用户实体")
public class SysUser extends BaseEntity {

    @Schema(description = "用户名")
    @TableField("username")
    private String username;

    @Schema(description = "状态")
    @TableField("status")
    private Integer status;
}
```

#### 2. Request DTO(请求对象)
- **位置**: `dto/request` 包
- **用途**: 接收客户端请求参数
- **命名**: `{操作}{Entity}Request`
- **特征**: 可以包含校验注解

```java
@Data
@Schema(description = "用户创建请求")
public class UserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码")
    private String password;
}
```

#### 3. VO(视图对象)
- **位置**: `vo` 包
- **用途**: 返回给客户端的数据
- **命名**: `{Entity}VO`
- **特征**: 可以对数据进行格式化和脱敏处理

```java
@Data
@Schema(description = "用户视图对象")
public class UserVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "创建时间（格式化后）")
    private String createTimeStr;
}
```

#### 4. 数据流转规则

```
请求 → Controller → Request DTO → Service → Entity → Mapper → 数据库
                                          ↓
数据库 → Mapper → Entity → Service → VO → Controller → 响应
```

### 注释规范

- 类和方法必须添加 JavaDoc 注释
- 复杂业务逻辑添加行内注释
- API 接口添加 Swagger 注解（@Tag, @Operation, @Schema）

### 导入规范（⚠️ 重要）

- **禁止使用完全限定名**：不要在代码中直接使用 `java.time.LocalDateTime` 这样的完全限定名
- **必须先导入再使用**：所有外部类必须在文件顶部使用 `import` 导入

```java
// ✅ 正确：先导入，再使用
import java.time.LocalDateTime;
import cn.hutool.core.util.StrUtil;

public class MyService {
    private LocalDateTime createTime;
}

// ❌ 错误：使用完全限定名
public class MyService {
    private java.time.LocalDateTime createTime;
}
```

### 业务异常处理

```java
// 使用 BusinessException 处理业务异常
if (user == null) {
    throw new BusinessException("用户不存在");
}

// 带错误码的异常
if (passwordInvalid) {
    throw new BusinessException(ErrorCode.PASSWORD_ERROR);
}
```

### 数据库查询

```java
// 使用 Wrappers 进行数据库操作
List<SysUser> users = baseMapper.selectList(
    Wrappers.<SysUser>lambdaQuery()
        .eq(SysUser::getStatus, 0)
        .orderByAsc(SysUser::getId)
);

// 分页查询
Page<SysUser> page = new Page<>(pageNum, pageSize);
Page<SysUser> result = baseMapper.selectPage(page,
    Wrappers.<SysUser>lambdaQuery()
        .like(StringUtils.hasText(username), SysUser::getUsername, username)
);
return PageResult.of(result);
```

### Controller 规范

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

    @PostMapping
    @Operation(summary = "创建用户")
    @SaCheckPermission("system:user:add")
    public R<Void> create(@Valid @RequestBody UserCreateRequest request) {
        boolean success = sysUserService.create(request);
        return success ? R.ok() : R.fail();
    }
}
```

### Service 规范

```java
public interface ISysUserService {

    // 分页查询
    PageResult<SysUser> page(Long pageNum, Long pageSize, String username);

    // 单条查询
    SysUser getById(Long id);

    // 新增
    boolean create(UserCreateRequest request);

    // 更新
    boolean update(UserUpdateRequest request);

    // 删除
    boolean delete(Long id);
}

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Override
    public PageResult<SysUser> page(Long pageNum, Long pageSize, String username) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        Page<SysUser> result = lambdaQuery()
            .like(StringUtils.hasText(username), SysUser::getUsername, username)
            .page(page);
        return PageResult.of(result);
    }
}
```
