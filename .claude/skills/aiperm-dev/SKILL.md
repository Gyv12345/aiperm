---
name: aiperm-dev
description: aiperm RBAC权限管理系统功能开发技能，专门用于开发基于aiperm框架的业务功能。当需要进行功能开发、模块创建、业务逻辑实现、数据库设计、API接口开发时激活。触发关键词：功能开发、模块开发、业务实现、新增功能、功能模块、开发功能、业务功能、创建模块、权限管理、菜单管理、角色管理、用户管理、字典管理、部门管理、岗位管理、Controller开发、Service开发、Mapper开发、Entity开发、DTO开发、VO开发、API接口开发、REST接口、数据库设计、表结构、SQL、建表、DDL、DML、创建表、修改表、ALTER TABLE、Orval、API生成
---

# aiperm RBAC 权限管理系统开发专家

## 职责定位

作为 aiperm RBAC 权限管理系统的**功能开发主导技能**，专注于基于 aiperm 框架的完整业务功能开发，从需求分析到代码实现的全流程支持。

## 项目模块结构（当前）

```
src/main/java/com/devlovecode/aiperm/
├── common/
│   ├── annotation/         @Log 注解
│   ├── aspect/             LogAspect 切面、LogEvent 事件
│   ├── domain/             BaseEntity, R, PageResult
│   ├── enums/              ErrorCode, OperType
│   └── exception/          BusinessException, GlobalExceptionHandler
├── config/
│   ├── MybatisPlusConfig.java
│   ├── MyMetaObjectHandler.java
│   ├── SaTokenConfig.java
│   └── WebMvcConfig.java
└── modules/
    ├── auth/               认证模块（登录/验证码）
    ├── log/                操作日志模块（SysOperLog）
    ├── oss/                文件存储模块（本地/阿里云）
    └── system/             系统管理模块
        ├── entity/         SysUser, SysRole, SysMenu, SysDept, SysPost
        │                   SysDictType, SysDictData
        ├── mapper/         各 Mapper 接口
        ├── service/        各 Service 接口 + 实现
        ├── controller/     各 REST Controller
        ├── dto/request/    请求 DTO
        └── vo/             响应 VO

resources/db/migration/    Flyway 迁移脚本（V2.0.0, V2.1.0...）
resources/mapper/          MyBatis XML 映射文件
```

## 新业务模块开发 7 步流程

### Step 1：建表（Flyway 迁移脚本）

在 `src/main/resources/db/migration/` 创建 `Vx.x.x__描述.sql`：

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
@TableName("sys_xxx")
@Schema(description = "xxx")
public class SysXxx extends BaseEntity {
    @Schema(description = "名称")
    @TableField("name")
    private String name;
    // ... 业务字段
}
```

**关键点：**
- 继承 `BaseEntity`（自动获得 id, create_time, update_time, create_by, update_by, deleted, version）
- `@TableName` 对应数据库表名
- 非数据库字段加 `@TableField(exist = false)`

### Step 3：Mapper 接口 + XML

```java
@Mapper
public interface SysXxxMapper extends BaseMapper<SysXxx> {
    // 复杂查询在此定义，简单查询用 BaseMapper 即可
}
```

```xml
<!-- resources/mapper/system/SysXxxMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.devlovecode.aiperm.modules.system.mapper.SysXxxMapper">
</mapper>
```

### Step 4：Service 接口 + 实现

```java
public interface ISysXxxService extends IService<SysXxx> {
    PageResult<SysXxx> page(Integer pageNum, Integer pageSize, String keyword);
    void create(SysXxx entity);
    void update(SysXxx entity);
    void delete(Long id);
}

@Service
@RequiredArgsConstructor  // ← 必须用这个，禁止 @Autowired
public class SysXxxServiceImpl extends ServiceImpl<SysXxxMapper, SysXxx>
        implements ISysXxxService {
    // 实现方法...
}
```

### Step 5：Controller（加权限和日志注解）

```java
@Tag(name = "xxx管理")
@RestController
@RequestMapping("/system/xxx")
@SaCheckLogin                // ← 整个 Controller 需要登录
@RequiredArgsConstructor
public class SysXxxController {

    private final ISysXxxService xxxService;

    @Operation(summary = "分页查询")
    @SaCheckPermission("system:xxx:list")
    @GetMapping("/page")
    public R<PageResult<SysXxx>> page(...) { ... }

    @Operation(summary = "创建")
    @SaCheckPermission("system:xxx:create")
    @Log(title = "xxx管理", operType = OperType.CREATE)   // ← 写操作必加
    @PostMapping
    public R<Void> create(@Valid @RequestBody XxxCreateRequest req) { ... }

    @Operation(summary = "更新")
    @SaCheckPermission("system:xxx:update")
    @Log(title = "xxx管理", operType = OperType.UPDATE)
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody XxxUpdateRequest req) { ... }

    @Operation(summary = "删除")
    @SaCheckPermission("system:xxx:delete")
    @Log(title = "xxx管理", operType = OperType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) { ... }
}
```

### Step 6：DTO 和 VO

```java
// 请求 DTO（带校验注解）
@Data
@Schema(description = "创建xxx请求")
public class XxxCreateRequest {
    @NotBlank(message = "名称不能为空")
    @Schema(description = "名称")
    private String name;
}

// 响应 VO
@Data
@Schema(description = "xxx响应VO")
public class XxxVO {
    private Long id;
    private String name;
    private LocalDateTime createTime;
}
```

### Step 7：前端生成 API

```bash
# 确保后端服务已启动
./gradlew bootRun

# 生成前端 API 客户端（类型安全）
cd frontend && pnpm run generate:api
```

## 参照模板

**以字典管理模块为标准参照（最完整的示例）：**

| 层 | 文件路径 |
|----|---------|
| Entity | `modules/system/entity/SysDictType.java` |
| Mapper | `modules/system/mapper/SysDictTypeMapper.java` |
| Service | `modules/system/service/ISysDictTypeService.java` |
| Service实现 | `modules/system/service/impl/SysDictTypeServiceImpl.java` |
| Controller | `modules/system/controller/SysDictTypeController.java` |
| DTO | `modules/system/dto/request/DictTypeCreateRequest.java` |
| VO | `modules/system/vo/DictTypeVO.java` |
| Flyway SQL | `resources/db/migration/V2.1.0__add_dict_tables.sql` |

## 命名规范完整表格

| 层 | 类名格式 | 示例 |
|----|---------|------|
| Entity | `Sys{模块}` | `SysDictType` |
| Mapper | `Sys{模块}Mapper` | `SysDictTypeMapper` |
| Service接口 | `ISys{模块}Service` | `ISysDictTypeService` |
| Service实现 | `Sys{模块}ServiceImpl` | `SysDictTypeServiceImpl` |
| Controller | `Sys{模块}Controller` | `SysDictTypeController` |
| 创建请求DTO | `{模块}CreateRequest` | `DictTypeCreateRequest` |
| 更新请求DTO | `{模块}UpdateRequest` | `DictTypeUpdateRequest` |
| 响应VO | `{模块}VO` | `DictTypeVO` |
| Flyway脚本 | `Vx.x.x__{描述}.sql` | `V2.1.0__add_dict_tables.sql` |

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

@Log(title = "字典类型管理", operType = OperType.CREATE)   // 新增
@Log(title = "字典类型管理", operType = OperType.UPDATE)   // 修改
@Log(title = "字典类型管理", operType = OperType.DELETE)   // 删除
@Log(title = "文件管理",     operType = OperType.UPLOAD)   // 上传
```

日志自动异步写入 `sys_oper_log` 表，不影响主业务性能。

### MapStruct-Plus 对象转换

```java
// 在 Entity 上加 @AutoMapper 声明映射关系
import io.github.linpeilie.annotations.AutoMapper;

@AutoMapper(target = XxxVO.class)           // Entity → VO
@AutoMapper(target = SysXxx.class)          // VO/DTO → Entity
public class SysXxx extends BaseEntity { ... }

// 注入 Converter bean 进行转换（不要手写 mapper 接口！）
import io.github.linpeilie.Converter;

@RequiredArgsConstructor
public class SysXxxController {
    private final Converter converter;

    // 单对象转换
    XxxVO vo = converter.convert(entity, XxxVO.class);

    // 列表转换
    List<XxxVO> voList = converter.convert(entityList, XxxVO.class);
}
```

## 核心规范（禁止事项）

1. **禁止使用 `@Autowired`**，统一使用 `@RequiredArgsConstructor` + `private final`
2. **禁止手写前端 API 调用**，必须用 `pnpm run generate:api` 生成
3. **禁止直接修改 Flyway 已执行的脚本**，变更必须新建版本号更高的迁移文件
4. **禁止在 Entity 中写业务逻辑**，Entity 只是数据载体
5. **禁止在 Controller 中写复杂业务逻辑**，复杂逻辑放在 Service 中

## OSS 文件上传

```java
@Autowired  // 实际应用中使用 @RequiredArgsConstructor
private OssService ossService;

// 上传
OssResult result = ossService.upload(multipartFile);
String url = result.getUrl();           // 访问 URL
String fileName = result.getFileName(); // 存储名（删除时使用）

// 删除
ossService.delete(fileName);
```

切换存储方式只需修改 `application.yaml`：
```yaml
oss:
  storage-type: local    # 或 aliyun
```

## 统一响应格式

```java
R.ok()              // 成功，无数据
R.ok(data)          // 成功，带数据
R.fail()            // 失败，无消息
R.fail("消息")       // 失败，带消息
PageResult.of(page) // 分页结果封装（接受 Page<T> 或 IPage<T>）
```

## 前后端 API 联调完整流程

```
后端开发 API
    ↓
启动服务：./gradlew bootRun
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
- [ ] Entity 继承 `BaseEntity`，`@TableName` 对应正确的表名
- [ ] Controller 类级别加 `@SaCheckLogin`
- [ ] Controller 所有写操作（POST/PUT/DELETE）加 `@Log` 注解
- [ ] Controller 方法级别加 `@SaCheckPermission("模块:资源:操作")`
- [ ] 依赖注入全部使用 `@RequiredArgsConstructor` + `private final`
- [ ] 后端完成后运行 `pnpm run generate:api` 同步前端
- [ ] IDE 中无编译错误和类型错误
