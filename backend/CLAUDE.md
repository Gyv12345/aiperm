# Backend - aiperm 后端开发规范

> 本文件定义 aiperm 后端开发的所有规范和约定。AI 助手在开发后端代码时必须遵循此文档。

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 21 | 编程语言 |
| Spring Boot | 3.5.11 | 基础框架 |
| Sa-Token | 1.44.0 | 权限认证 |
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
│   ├── domain/                 # 基础类（BaseEntity, R, PageResult）
│   ├── enums/                  # 枚举（ErrorCode, OperType）
│   ├── exception/              # 异常（BusinessException, GlobalExceptionHandler）
│   └── repository/             # 基础 Repository
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
| Entity | `SysXxx` | `extends BaseEntity` | `@TableName("sys_xxx")` |
| Repository | `XxxRepository` | `extends BaseRepository<Xxx>` | `@Repository` |
| Service | `XxxService` | - | `@Service` |
| Controller | `SysXxxController` | - | `@RestController` |
| 请求 DTO | `XxxCreateRequest` / `XxxUpdateRequest` / `XxxQueryRequest` | - | 含 `@Valid` 注解 |
| 响应 VO | `XxxVO` | - | - |
| DTO | `XxxDTO` | - | 内部数据传输 |

## 文件位置约定

```
modules/system/
├── entity/                     SysXxx.java
├── repository/                 XxxRepository.java
├── service/                    XxxService.java
├── controller/                 SysXxxController.java
├── dto/request/                XxxCreateRequest.java
├── dto/                        XxxDTO.java
└── vo/                         XxxVO.java

resources/
├── db/migration/               Vx.x.x__description.sql
└── application.yaml
```

## 新业务模块开发 7 步流程

### 1. 建表

创建 Flyway 迁移脚本 `backend/src/main/resources/db/migration/Vx.x.x__xxx.sql`

```sql
CREATE TABLE sys_xxx (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    -- 业务字段
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);
```

### 2. Entity

```java
@Data
@TableName("sys_xxx")
public class SysXxx extends BaseEntity {
    private Long id;
    // 业务字段
}
```

### 3. Repository

```java
@Repository
public class XxxRepository extends BaseRepository<SysXxx> {
}
```

### 4. Service

```java
@Service
@RequiredArgsConstructor
public class XxxService {
    private final XxxRepository xxxRepository;
    private final Converter converter;

    public List<XxxVO> findAll(XxxQueryRequest req) {
        // 实现逻辑
    }
}
```

### 5. Controller

```java
@RestController
@RequestMapping("/system/xxx")
@RequiredArgsConstructor
@SaCheckLogin
public class SysXxxController {
    private final XxxService xxxService;

    @GetMapping
    public R<PageResult<XxxVO>> list(XxxQueryRequest req) {
        return R.ok(xxxService.findAll(req));
    }

    @PostMapping
    @SaCheckPermission("system:xxx:create")
    @Log(title = "Xxx管理", operType = OperType.CREATE)
    public R<Void> create(@RequestBody @Valid XxxCreateRequest req) {
        xxxService.create(req);
        return R.ok();
    }
}
```

### 6. DTO/VO

```java
// 请求 DTO
@Data
public class XxxCreateRequest {
    @NotBlank(message = "名称不能为空")
    private String name;
}

// 响应 VO
@Data
public class XxxVO {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
}
```

### 7. 前端生成

```bash
cd frontend && pnpm run generate:api
```

## 权限注解使用

```java
@SaCheckLogin                              // 检查登录
@SaCheckRole("admin")                      // 检查角色
@SaCheckPermission("system:user:create")   // 检查权限
@SaCheckPermission(value = {"user:update", "user:delete"}, mode = SaMode.OR)  // 满足任一
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

## @Log 注解使用（每个写操作必须加）

```java
@Log(title = "用户管理", operType = OperType.CREATE)
@PostMapping
public R<Void> create(@RequestBody @Valid XxxCreateRequest req) { ... }

@Log(title = "用户管理", operType = OperType.UPDATE)
@PutMapping("/{id}")
public R<Void> update(@PathVariable Long id, @RequestBody @Valid XxxUpdateRequest req) { ... }

@Log(title = "用户管理", operType = OperType.DELETE)
@DeleteMapping("/{id}")
public R<Void> delete(@PathVariable Long id) { ... }
```

## 依赖注入规范

**必须使用构造函数注入，禁止 `@Autowired` 字段注入：**

```java
// 正确做法
@Service
@RequiredArgsConstructor
public class XxxService {
    private final XxxRepository xxxRepository;
    private final Converter converter;
}

// 错误做法（禁止）
@Service
public class XxxService {
    @Autowired
    private XxxRepository xxxRepository;
}
```

## 统一响应格式

```java
// 成功响应
return R.ok(data);
return R.ok();

// 分页响应
return R.ok(PageResult.of(list, total));

// 失败响应
throw new BusinessException(ErrorCode.PARAM_ERROR, "参数错误");
```

## OSS 文件上传使用

```java
@Service
@RequiredArgsConstructor
public class XxxService {
    private final OssService ossService;

    public String upload(MultipartFile file) {
        OssResult result = ossService.upload(file);
        return result.getUrl();        // 完整访问地址
    }

    public void deleteFile(String fileName) {
        ossService.delete(fileName);   // 删除文件
    }
}
```

## 开发检查清单

开发新模块前必须逐项确认：

- [ ] 建表 SQL 写入 Flyway 迁移文件
- [ ] Entity 继承 `BaseEntity`
- [ ] Controller 类加 `@SaCheckLogin`
- [ ] 写操作加 `@Log` + `@SaCheckPermission`
- [ ] 依赖注入使用 `@RequiredArgsConstructor` + `private final`
- [ ] 参照 `modules/system/entity/SysDictType.java` 及相关文件作为模板
