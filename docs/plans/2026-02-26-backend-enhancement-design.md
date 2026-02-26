# 后端功能增强设计文档

> 日期：2026-02-26
> 状态：待实现

## 概述

本次增强包含三个功能点：
1. 移除多数据源依赖，简化配置
2. 增加通用 Excel 导出能力
3. 实现基于角色的数据权限控制

---

## 一、移除多数据源依赖

### 背景

项目引入了 `dynamic-datasource-spring-boot3-starter`，但实际只配置了一个 master 数据源，代码中也没有使用 `@DS` 注解切换数据源。该依赖增加了不必要的复杂度。

### 变更内容

#### 1.1 移除依赖 (`build.gradle`)

```groovy
// 删除
set('dynamicDatasourceVersion', '4.3.1')

// 删除
implementation "com.baomidou:dynamic-datasource-spring-boot3-starter:${dynamicDatasourceVersion}"
```

#### 1.2 简化配置 (`application.yaml`)

```yaml
# 删除原配置
spring:
  datasource:
    dynamic:
      primary: master
      strict: true
      datasource:
        master:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://xxx:3306/aiperm?...
          username: root
          password: xxx

# 改为 Spring Boot 原生配置
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://xxx:3306/aiperm?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
    username: root
    password: xxx
```

#### 1.3 代码影响

无影响。项目使用 `JdbcClient`，不依赖 dynamic-datasource。

---

## 二、通用 Excel 导出

### 设计目标

提供通用的 Excel 导出能力，各业务模块可快速接入。

### 技术选型

使用项目已有的 `fastexcel` 依赖（EasyExcel 的活跃 fork），无需新增依赖。

### 实现方案

#### 2.1 通用导出工具类

```java
package com.devlovecode.aiperm.common.util;

@Component
@RequiredArgsConstructor
public class ExcelExportHelper {

    /**
     * 导出 Excel
     *
     * @param response HTTP 响应
     * @param filename 文件名（不含扩展名）
     * @param clazz    导出实体类（需标注 @ExcelProperty）
     * @param data     数据列表
     */
    public <T> void export(HttpServletResponse response,
                           String filename,
                           Class<T> clazz,
                           List<T> data) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFilename = URLEncoder.encode(filename + ".xlsx", "UTF-8")
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + encodedFilename);

            FastExcel.write(response.getOutputStream(), clazz)
                    .sheet(filename)
                    .doWrite(data);
        } catch (IOException e) {
            throw new BusinessException("导出失败: " + e.getMessage());
        }
    }
}
```

#### 2.2 Controller 使用示例

```java
@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final UserService userService;
    private final ExcelExportHelper excelExportHelper;

    @Operation(summary = "导出用户")
    @SaCheckPermission("system:user:export")
    @Log(title = "用户管理", operType = OperType.EXPORT)
    @GetMapping("/export")
    public void export(UserQueryDTO dto, HttpServletResponse response) {
        List<UserExportVO> data = userService.listForExport(dto);
        excelExportHelper.export(response, "用户列表", UserExportVO.class, data);
    }
}
```

#### 2.3 导出实体示例

```java
@Data
public class UserExportVO {
    @ExcelProperty("用户名")
    private String username;

    @ExcelProperty("昵称")
    private String nickname;

    @ExcelProperty("部门")
    private String deptName;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("状态")
    private String statusText;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
```

#### 2.4 新增操作类型

在 `OperType` 枚举中添加：

```java
EXPORT("导出")
```

---

## 三、数据权限控制

### 设计目标

基于角色的数据权限控制，支持四种范围：
- 全部数据
- 本部门数据
- 本部门及下级部门数据
- 仅本人数据

### 数据库现状

`sys_role` 表已有 `data_scope` 字段：
```sql
`data_scope` TINYINT DEFAULT 1 COMMENT '数据权限范围：1-全部，2-本部门，3-本部门及以下，4-仅本人'
```

### 实现方案

#### 3.1 数据权限枚举

```java
package com.devlovecode.aiperm.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DataScopeEnum {

    ALL(1, "全部数据"),
    DEPT(2, "本部门数据"),
    DEPT_AND_CHILD(3, "本部门及下级部门数据"),
    SELF(4, "仅本人数据");

    private final Integer code;
    private final String desc;

    public static DataScopeEnum of(Integer code) {
        for (DataScopeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return ALL;
    }
}
```

#### 3.2 数据权限持有者

```java
package com.devlovecode.aiperm.common.context;

public class DataScopeHolder {

    private static final ThreadLocal<String> SQL_HOLDER = new ThreadLocal<>();

    /**
     * 设置数据权限 SQL 片段
     */
    public static void set(String sql) {
        SQL_HOLDER.set(sql);
    }

    /**
     * 获取数据权限 SQL 片段
     */
    public static String get() {
        return SQL_HOLDER.get() != null ? SQL_HOLDER.get() : "";
    }

    /**
     * 清理
     */
    public static void clear() {
        SQL_HOLDER.remove();
    }
}
```

#### 3.3 数据权限服务

```java
package com.devlovecode.aiperm.common.service;

@Service
@RequiredArgsConstructor
public class DataScopeService {

    private final JdbcClient db;

    /**
     * 构建当前用户的数据权限 SQL
     */
    public String buildDataScopeSql(String deptAlias, String userAlias) {
        // 未登录则无限制
        if (!StpUtil.isLogin()) {
            return "";
        }

        Long userId = StpUtil.getLoginIdAsLong();

        // 获取用户角色中最大的数据权限范围（数字越小权限越大）
        Integer dataScope = getMaxDataScope(userId);
        DataScopeEnum scopeEnum = DataScopeEnum.of(dataScope);

        return switch (scopeEnum) {
            case ALL -> "";
            case DEPT -> {
                Long deptId = getUserDeptId(userId);
                yield String.format(" AND %s.id = %d", deptAlias, deptId);
            }
            case DEPT_AND_CHILD -> {
                List<Long> deptIds = getDeptAndChildIds(userId);
                yield String.format(" AND %s.id IN (%s)", deptAlias,
                        deptIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
            }
            case SELF -> String.format(" AND %s.id = %d", userAlias, userId);
        };
    }

    private Integer getMaxDataScope(Long userId) {
        String sql = """
            SELECT MIN(r.data_scope)
            FROM sys_role r
            JOIN sys_user_role ur ON r.id = ur.role_id
            WHERE ur.user_id = ? AND r.deleted = 0 AND r.status = 1
            """;
        Integer result = db.sql(sql).param(userId).query(Integer.class).optional().orElse(1);
        return result != null ? result : 1;
    }

    private Long getUserDeptId(Long userId) {
        String sql = "SELECT dept_id FROM sys_user WHERE id = ? AND deleted = 0";
        return db.sql(sql).param(userId).query(Long.class).optional().orElse(0L);
    }

    private List<Long> getDeptAndChildIds(Long userId) {
        Long deptId = getUserDeptId(userId);
        // 查询部门及所有子部门（使用递归或前端树结构存储的 ancestors 字段）
        String sql = """
            SELECT id FROM sys_dept
            WHERE deleted = 0 AND status = 1
            AND (id = ? OR FIND_IN_SET(?, ancestors))
            """;
        return db.sql(sql).param(deptId).param(deptId).queryList(Long.class);
    }
}
```

#### 3.4 数据权限拦截器

```java
package com.devlovecode.aiperm.common.interceptor;

@Component
@RequiredArgsConstructor
public class DataScopeInterceptor implements HandlerInterceptor {

    private final DataScopeService dataScopeService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 计算并存储数据权限 SQL（使用默认别名）
        String sql = dataScopeService.buildDataScopeSql("d", "u");
        DataScopeHolder.set(sql);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        DataScopeHolder.clear();
    }
}
```

#### 3.5 注册拦截器

```java
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcInterceptor {

    private final DataScopeInterceptor dataScopeInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(dataScopeInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**");
    }
}
```

#### 3.6 补充 Entity 字段

```java
// SysRole.java 添加字段
private Integer dataScope;
```

#### 3.7 Repository 使用示例

```java
@Repository
public class UserRepository extends BaseRepository<SysUser> {

    public PageResult<SysUser> queryPage(String username, Integer status, int pageNum, int pageSize) {
        String dataScopeSql = DataScopeHolder.get();

        SqlBuilder sb = new SqlBuilder();
        sb.likeIf(username != null && !username.isBlank(), "u.username", username);
        sb.whereIf(status != null, "u.status = ?", status);

        String sql = """
            SELECT u.*, d.name as dept_name
            FROM sys_user u
            LEFT JOIN sys_dept d ON u.dept_id = d.id
            WHERE u.deleted = 0
            """ + dataScopeSql + sb.getWhereClause();

        // ... 执行查询
    }
}
```

### SQL 生成示例

| dataScope | 生成的 SQL 片段 |
|-----------|----------------|
| 1 (全部) | `""` (空字符串) |
| 2 (本部门) | `AND d.id = 10` |
| 3 (本部门及下级) | `AND d.id IN (10,11,12,13)` |
| 4 (仅本人) | `AND u.id = 1` |

---

## 四、实施计划

### Phase 1：移除多数据源
- [ ] 修改 `build.gradle`，移除 dynamic-datasource 依赖
- [ ] 修改 `application.yaml`，使用原生数据源配置
- [ ] 启动测试，确认功能正常

### Phase 2：Excel 导出
- [ ] 创建 `ExcelExportHelper` 工具类
- [ ] 在 `OperType` 中添加 `EXPORT` 类型
- [ ] 在用户管理模块实现导出功能（作为示例）
- [ ] 前端添加导出按钮

### Phase 3：数据权限
- [ ] 创建 `DataScopeEnum` 枚举
- [ ] 创建 `DataScopeHolder` 上下文持有者
- [ ] 创建 `DataScopeService` 服务类
- [ ] 创建 `DataScopeInterceptor` 拦截器
- [ ] 在 `SysRole` Entity 中添加 `dataScope` 字段
- [ ] 修改角色相关 DTO/VO，支持 dataScope
- [ ] 在用户查询中应用数据权限（作为示例）
- [ ] 前端角色管理页面添加数据权限选择

---

## 五、注意事项

1. **数据权限性能**：`FIND_IN_SET` 查询可能较慢，如果部门层级深，考虑在部门表增加 `all_child_ids` 冗余字段
2. **多角色取最大权限**：用户有多个角色时，取最大的数据权限（MIN(data_scope)）
3. **超级管理员豁免**：可考虑对特定角色（如 admin）跳过数据权限检查
