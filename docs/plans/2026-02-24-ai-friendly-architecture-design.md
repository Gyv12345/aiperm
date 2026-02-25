# AI 友好架构简化设计

> 日期：2026-02-24
> 目标：让 AI 能更高效地生成代码，减少分层复杂度和模板代码

## 一、背景与目标

### 当前痛点

1. **分层繁琐** — Entity/Mapper/Service/Controller/DTO/VO/Converter 每层都要写
2. **关联复杂** — 用户-角色-菜单多表关联，AI 难以理解
3. **权限配置繁琐** — 每个接口都要加权限注解
4. **API 对接流程长** — 后端改动需要重新生成前端 API

### 目标

- 减少分层复杂度，提高 AI 代码生成效率
- 保留核心能力：权限控制、操作审计、缓存、分页
- 灵活适应不同规模项目

---

## 二、架构变化

### 分层对比

```
当前 7 层                          简化后 4 层
─────────────────────────────────────────────────
Controller                    →    Controller
Service 接口                  →    （删除）
Service 实现                  →    Service
Mapper (Java + XML)           →    Repository
Entity                        →    Entity
DTO (Create/Update/Query)     →    DTO (复用)
VO                            →    VO
Converter (MapStruct)         →    （删除）
```

### 模块文件结构

```
modules/system/xxx/
├── entity/
│   └── SysXxx.java           # 数据库实体
├── repository/
│   └── XxxRepository.java    # 数据访问（JdbcClient）
├── service/
│   └── XxxService.java       # 业务逻辑（无接口）
├── controller/
│   └── XxxController.java    # REST 接口
├── dto/
│   └── XxxDTO.java           # 请求参数（多场景复用）
└── vo/
    └── XxxVO.java            # 响应对象
```

### 量化变化

| 维度 | 当前 | 简化后 | 变化 |
|------|------|--------|------|
| 分层 | 7层 | 4层 | -43% |
| 文件数/模块 | 11个 | 6个 | -45% |
| 依赖 | MyBatis-Plus + MapStruct-Plus | JdbcClient | -2个 |

---

## 三、各层设计

### 3.1 Entity 层

保持不变，继承 `BaseEntity`：

```java
@TableName("sys_user")
@Schema(description = "用户实体")
public class SysUser extends BaseEntity {
    private String username;
    private String password;
    private String nickname;
    // ...
}
```

### 3.2 Repository 层

使用 Spring Boot 3.2+ 的 `JdbcClient`，替代 MyBatis-Plus：

```java
@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcClient db;

    public void save(SysUser user) {
        if (user.getId() == null) {
            String sql = """
                INSERT INTO sys_user (username, password, nickname, create_time)
                VALUES (:username, :password, :nickname, NOW())
                """;
            db.sql(sql).paramSource(user).update();
        } else {
            String sql = """
                UPDATE sys_user
                SET username = :username, nickname = :nickname, update_time = NOW()
                WHERE id = :id
                """;
            db.sql(sql).paramSource(user).update();
        }
    }

    public SysUser findById(Long id) {
        return db.sql("SELECT * FROM sys_user WHERE id = :id AND deleted = 0")
            .param("id", id)
            .query(SysUser.class)
            .single();
    }

    public void deleteById(Long id) {
        db.sql("UPDATE sys_user SET deleted = 1 WHERE id = :id")
            .param("id", id)
            .update();
    }

    public PageResult<SysUser> queryPage(UserDTO query) {
        // 动态条件查询
    }
}
```

**优势：**
- 无 XML、无注解映射，纯 Java 代码
- SQL 写在代码中，AI 一眼能看懂
- 去掉 MyBatis-Plus 依赖，更轻量

### 3.3 Service 层

合并接口和实现，无接口：

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    @Transactional
    public Long create(UserDTO dto) {
        // 校验
        if (userRepo.existsByUsername(dto.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        // 创建
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(encoder.encode(dto.getPassword()));
        userRepo.save(user);
        return user.getId();
    }

    public PageResult<UserVO> queryPage(UserDTO dto) {
        return userRepo.queryPage(dto).map(this::toVO);
    }

    private UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        return vo;
    }
}
```

### 3.4 Controller 层

使用 Sa-Token 原生注解：

```java
@RestController
@RequestMapping("/system/user")
@Tag(name = "用户管理")
public class UserController {

    private final UserService userService;

    @GetMapping
    @SaCheckPermission("system:user:list")
    @Log(title = "用户管理", operType = OperType.QUERY)
    public R<PageResult<UserVO>> list(UserDTO dto) {
        return R.ok(userService.queryPage(dto));
    }

    @PostMapping
    @SaCheckPermission("system:user:create")
    @Log(title = "用户管理", operType = OperType.CREATE)
    public R<Long> create(@RequestBody @Validated(Views.Create.class) UserDTO dto) {
        return R.ok(userService.create(dto));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:user:update")
    @Log(title = "用户管理", operType = OperType.UPDATE)
    public R<Void> update(@PathVariable Long id, @RequestBody @Validated(Views.Update.class) UserDTO dto) {
        userService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:user:delete")
    @Log(title = "用户管理", operType = OperType.DELETE)
    public R<Void> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return R.ok();
    }
}
```

**约定：**
- 登录校验 → 拦截器统一处理（白名单配置）
- `@SaCheckPermission` — 权限校验
- `@Log` — 写操作审计

### 3.5 DTO 层

使用 Jackson 视图复用，一个 DTO 支持多场景：

```java
public class Views {
    public interface Create {}
    public interface Update {}
    public interface Query {}
}

@Data
@Schema(description = "用户数据")
public class UserDTO {

    // 查询参数
    @JsonView(Views.Query.class)
    private String username;

    @JsonView(Views.Query.class)
    private Integer status;

    // 创建必填
    @JsonView({Views.Create.class, Views.Query.class})
    @NotBlank(message = "用户名不能为空", groups = Views.Create.class)
    private String username;

    @JsonView(Views.Create.class)
    @NotBlank(message = "密码不能为空", groups = Views.Create.class)
    private String password;

    // 通用字段
    @JsonView({Views.Create.class, Views.Update.class})
    private String nickname;

    @JsonView({Views.Create.class, Views.Update.class})
    private String email;

    // 分页参数
    @JsonView(Views.Query.class)
    private Integer page = 1;

    @JsonView(Views.Query.class)
    private Integer pageSize = 10;
}
```

---

## 四、依赖变化

### 删除的依赖

```groovy
// 删除
implementation 'com.baomidou:mybatis-plus-spring-boot3-starter:3.5.9'
implementation 'io.github.linpeilie:mapstruct-plus-spring-boot-starter:1.4.6'
```

### 保留的依赖

```groovy
// 保留
implementation 'cn.dev33:sa-token-spring-boot3-starter:1.39.0'
implementation 'org.springframework.boot:spring-boot-starter-jdbc'
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
implementation 'org.springframework.boot:spring-boot-starter-validation'
```

### 删除的配置类

```
config/
├── MybatisPlusConfig.java      # 删除
└── MyMetaObjectHandler.java    # 删除
```

---

## 五、迁移路径

### Phase 1: 基础设施准备

1. 创建 `BaseRepository<T>` 通用基类
2. 创建 `SqlBuilder` 动态 SQL 工具
3. 调整 Sa-Token 配置

### Phase 2: 迁移字典模块（作为模板）

将 `SysDictType` 和 `SysDictData` 作为第一个迁移示例。

**迁移步骤：**
1. 创建 `DictTypeRepository`
2. 创建 `DictTypeService`
3. 创建 `DictTypeDTO`
4. 简化 `DictTypeController`
5. 测试验证
6. 删除旧文件

### Phase 3: 迁移其他模块

| 顺序 | 模块 | 原因 |
|------|------|------|
| 1 | SysDict | 结构简单，作为模板 |
| 2 | SysPost | 结构简单，无关联 |
| 3 | SysDept | 树形结构，稍复杂 |
| 4 | SysMenu | 有权限关联 |
| 5 | SysRole | 有多对多关联 |
| 6 | SysUser | 最复杂，关联最多 |

### Phase 4: 清理

1. 删除 `MybatisPlusConfig.java`
2. 删除 `MyMetaObjectHandler.java`
3. 删除所有 Mapper XML 文件
4. 删除所有 Converter 文件
5. 删除所有 Service 接口文件
6. 更新 `build.gradle` 移除依赖
7. 运行全量测试

---

## 六、保留的核心能力

| 能力 | 实现方式 |
|------|----------|
| 权限控制 | `@SaCheckPermission` |
| 操作审计 | `@Log` 注解 |
| Redis 缓存 | 保留 |
| 分页能力 | `PageResult<T>` |
| 事务管理 | `@Transactional` |
| 参数校验 | `@Valid` + `@NotBlank` |

---

## 七、AI 开发效率提升

| 痛点 | 解决方案 | 效果 |
|------|----------|------|
| 分层繁琐 | 4 层替代 7 层 | AI 生成路径短 |
| 关联复杂 | Repository 封装 | SQL 一目了然 |
| 权限配置繁琐 | 拦截器统一登录 | 只需 `@SaCheckPermission` |
| API 对接流程长 | DTO 结构更简单 | Orval 生成更准确 |
| 模板代码多 | 去掉接口、Converter、XML | 减少 45% 代码量 |
