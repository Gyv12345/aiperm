# 后端功能增强实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 移除无用多数据源依赖，添加通用 Excel 导出和数据权限控制功能

**Architecture:**
- 移除 dynamic-datasource 依赖，使用 Spring Boot 原生 DataSource
- Excel 导出通过工具类封装 FastExcel，各模块调用
- 数据权限通过拦截器 + ThreadLocal 实现，Repository 层直接获取 SQL 片段

**Tech Stack:** Spring Boot 3.5, FastExcel, Sa-Token, JdbcClient

---

## Phase 1: 移除多数据源依赖

### Task 1.1: 修改 build.gradle

**Files:**
- Modify: `backend/build.gradle`

**Step 1: 移除 dynamic-datasource 版本定义**

找到第 35 行左右的 `ext` 块，删除这行：
```groovy
set('dynamicDatasourceVersion', '4.3.1')
```

**Step 2: 移除 dynamic-datasource 依赖**

找到第 51 行左右，删除这行：
```groovy
implementation "com.baomidou:dynamic-datasource-spring-boot3-starter:${dynamicDatasourceVersion}"
```

**Step 3: 添加 Spring Boot JDBC Starter（如果需要）**

确认 `spring-boot-starter-jdbc` 依赖存在（第 47 行左右应该已有）：
```groovy
implementation 'org.springframework.boot:spring-boot-starter-jdbc'
```

**Step 4: 验证编译**

Run: `cd backend && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add backend/build.gradle
git commit -m "chore: remove unused dynamic-datasource dependency"
```

---

### Task 1.2: 修改 application.yaml

**Files:**
- Modify: `backend/src/main/resources/application.yaml`

**Step 1: 替换数据源配置**

将第 20-30 行的配置：
```yaml
  # 动态数据源配置
  datasource:
    dynamic:
      primary: master
      strict: true
      datasource:
        master:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://localhost:3306/aiperm?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
          username: root
          password: ''
```

替换为：
```yaml
  # 数据源配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/aiperm?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
    username: root
    password: ''
```

**Step 2: 验证启动**

Run: `cd backend && ./gradlew bootRun`
Expected: 应用正常启动，无数据源错误

**Step 3: Commit**

```bash
git add backend/src/main/resources/application.yaml
git commit -m "chore: simplify datasource config to Spring Boot native"
```

---

## Phase 2: 通用 Excel 导出

### Task 2.1: 创建 ExcelExportHelper 工具类

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/common/util/ExcelExportHelper.java`

**Step 1: 创建工具类**

```java
package com.devlovecode.aiperm.common.util;

import cn.idev.excel.FastExcel;
import com.devlovecode.aiperm.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Excel 导出工具类
 *
 * @author DevLoveCode
 */
@Component
public class ExcelExportHelper {

    /**
     * 导出 Excel
     *
     * @param response HTTP 响应
     * @param filename 文件名（不含扩展名）
     * @param clazz    导出实体类（需标注 @ExcelProperty）
     * @param data     数据列表
     * @param <T>      数据类型
     */
    public <T> void export(HttpServletResponse response,
                           String filename,
                           Class<T> clazz,
                           List<T> data) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFilename = URLEncoder.encode(filename + ".xlsx", StandardCharsets.UTF_8)
                    .replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + encodedFilename);

            FastExcel.write(response.getOutputStream(), clazz)
                    .sheet(filename)
                    .doWrite(data);
        } catch (IOException e) {
            throw new BusinessException("导出失败: " + e.getMessage());
        }
    }
}
```

**Step 2: 验证编译**

Run: `cd backend && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/common/util/ExcelExportHelper.java
git commit -m "feat(excel): add ExcelExportHelper for generic export"
```

---

## Phase 3: 数据权限控制

### Task 3.1: 创建数据权限枚举

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/common/enums/DataScopeEnum.java`

**Step 1: 创建枚举类**

```java
package com.devlovecode.aiperm.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据权限范围枚举
 *
 * @author DevLoveCode
 */
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
        if (code == null) {
            return ALL;
        }
        for (DataScopeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return ALL;
    }
}
```

**Step 2: 验证编译**

Run: `cd backend && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/common/enums/DataScopeEnum.java
git commit -m "feat(data-scope): add DataScopeEnum"
```

---

### Task 3.2: 创建数据权限持有者

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/common/context/DataScopeHolder.java`

**Step 1: 创建持有者类**

```java
package com.devlovecode.aiperm.common.context;

/**
 * 数据权限上下文持有者
 * 使用 ThreadLocal 存储当前请求的数据权限 SQL 片段
 *
 * @author DevLoveCode
 */
public class DataScopeHolder {

    private static final ThreadLocal<String> SQL_HOLDER = new ThreadLocal<>();

    private DataScopeHolder() {
    }

    /**
     * 设置数据权限 SQL 片段
     */
    public static void set(String sql) {
        SQL_HOLDER.set(sql);
    }

    /**
     * 获取数据权限 SQL 片段
     *
     * @return SQL 片段，未设置时返回空字符串
     */
    public static String get() {
        String sql = SQL_HOLDER.get();
        return sql != null ? sql : "";
    }

    /**
     * 清理当前线程的数据权限
     */
    public static void clear() {
        SQL_HOLDER.remove();
    }
}
```

**Step 2: 验证编译**

Run: `cd backend && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/common/context/DataScopeHolder.java
git commit -m "feat(data-scope): add DataScopeHolder for thread-local storage"
```

---

### Task 3.3: 创建数据权限服务

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/common/service/DataScopeService.java`

**Step 1: 创建服务类**

```java
package com.devlovecode.aiperm.common.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.enums.DataScopeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据权限服务
 *
 * @author DevLoveCode
 */
@Service
@RequiredArgsConstructor
public class DataScopeService {

    private final JdbcClient db;

    /**
     * 构建当前用户的数据权限 SQL
     *
     * @param deptAlias 部门表别名
     * @param userAlias 用户表别名
     * @return SQL 片段
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
                if (deptId == null || deptId == 0) {
                    yield "";
                }
                yield String.format(" AND %s.id = %d", deptAlias, deptId);
            }
            case DEPT_AND_CHILD -> {
                List<Long> deptIds = getDeptAndChildIds(userId);
                if (deptIds.isEmpty()) {
                    yield "";
                }
                yield String.format(" AND %s.id IN (%s)", deptAlias,
                        deptIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
            }
            case SELF -> String.format(" AND %s.id = %d", userAlias, userId);
        };
    }

    /**
     * 获取用户最大的数据权限范围（数字越小权限越大）
     */
    private Integer getMaxDataScope(Long userId) {
        String sql = """
            SELECT MIN(r.data_scope)
            FROM sys_role r
            JOIN sys_user_role ur ON r.id = ur.role_id
            WHERE ur.user_id = ? AND r.deleted = 0 AND r.status = 1
            """;
        Integer result = db.sql(sql)
                .param(userId)
                .query(Integer.class)
                .optional()
                .orElse(null);
        return result != null ? result : DataScopeEnum.ALL.getCode();
    }

    /**
     * 获取用户的部门 ID
     */
    private Long getUserDeptId(Long userId) {
        String sql = "SELECT dept_id FROM sys_user WHERE id = ? AND deleted = 0";
        return db.sql(sql)
                .param(userId)
                .query(Long.class)
                .optional()
                .orElse(0L);
    }

    /**
     * 获取用户部门及所有子部门 ID
     */
    private List<Long> getDeptAndChildIds(Long userId) {
        Long deptId = getUserDeptId(userId);
        if (deptId == null || deptId == 0) {
            return List.of();
        }

        // 查询部门及所有子部门（使用 ancestors 字段）
        String sql = """
            SELECT id FROM sys_dept
            WHERE deleted = 0 AND status = 1
            AND (id = ? OR FIND_IN_SET(?, ancestors))
            """;
        return db.sql(sql)
                .param(deptId)
                .param(deptId)
                .queryList(Long.class);
    }
}
```

**Step 2: 验证编译**

Run: `cd backend && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/common/service/DataScopeService.java
git commit -m "feat(data-scope): add DataScopeService for building scope SQL"
```

---

### Task 3.4: 创建数据权限拦截器

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/common/interceptor/DataScopeInterceptor.java`

**Step 1: 创建拦截器类**

```java
package com.devlovecode.aiperm.common.interceptor;

import com.devlovecode.aiperm.common.context.DataScopeHolder;
import com.devlovecode.aiperm.common.service.DataScopeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 数据权限拦截器
 * 在请求开始时计算并存储数据权限 SQL
 *
 * @author DevLoveCode
 */
@Component
@RequiredArgsConstructor
public class DataScopeInterceptor implements HandlerInterceptor {

    private final DataScopeService dataScopeService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 计算并存储数据权限 SQL（使用默认别名 d 和 u）
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

**Step 2: 验证编译**

Run: `cd backend && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/common/interceptor/DataScopeInterceptor.java
git commit -m "feat(data-scope): add DataScopeInterceptor"
```

---

### Task 3.5: 注册拦截器

**Files:**
- Modify: `backend/src/main/java/com/devlovecode/aiperm/config/WebMvcConfig.java`

**Step 1: 添加拦截器注册**

在类中添加依赖注入和 `addInterceptors` 方法：

```java
package com.devlovecode.aiperm.config;

import com.devlovecode.aiperm.common.interceptor.DataScopeInterceptor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 *
 * @author DevLoveCode
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final DataScopeInterceptor dataScopeInterceptor;

    /**
     * 本地 OSS 文件访问映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:./uploads/");
    }

    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 拦截器配置
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(dataScopeInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/**", "/error", "/v3/api-docs/**", "/swagger-ui/**");
    }

    /**
     * OpenAPI配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AiPerm RBAC权限系统API")
                        .version("1.0.0")
                        .description("基于Spring Boot 3 + Sa-Token的RBAC权限管理系统")
                        .contact(new Contact()
                                .name("DevLoveCode")
                                .email("dev@lovecode.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
```

**Step 2: 验证编译**

Run: `cd backend && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/config/WebMvcConfig.java
git commit -m "feat(data-scope): register DataScopeInterceptor in WebMvcConfig"
```

---

### Task 3.6: 补充 SysRole Entity 字段

**Files:**
- Modify: `backend/src/main/java/com/devlovecode/aiperm/modules/system/entity/SysRole.java`

**Step 1: 添加 dataScope 字段**

在 `SysRole` 类中添加字段：

```java
package com.devlovecode.aiperm.modules.system.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体类
 *
 * @author devlovecode
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRole extends BaseEntity {

    private String roleName;

    private String roleCode;

    private Integer sort;

    private Integer status;

    private String remark;

    private Integer isBuiltin;

    /**
     * 数据权限范围
     * 1-全部数据，2-本部门数据，3-本部门及下级部门数据，4-仅本人数据
     */
    private Integer dataScope;
}
```

**Step 2: 验证编译**

Run: `cd backend && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/system/entity/SysRole.java
git commit -m "feat(data-scope): add dataScope field to SysRole entity"
```

---

### Task 3.7: 更新角色相关 DTO 和 VO

**Files:**
- Modify: `backend/src/main/java/com/devlovecode/aiperm/modules/system/dto/RoleDTO.java`
- Modify: `backend/src/main/java/com/devlovecode/aiperm/modules/system/vo/RoleVO.java`

**Step 1: 先查看现有 DTO 和 VO 结构**

Run: `cat backend/src/main/java/com/devlovecode/aiperm/modules/system/dto/RoleDTO.java`
Run: `cat backend/src/main/java/com/devlovecode/aiperm/modules/system/vo/RoleVO.java`

**Step 2: 在 RoleDTO 中添加 dataScope 字段**

在 DTO 类中添加（根据实际结构调整位置）：

```java
@JsonView({Views.Create.class, Views.Update.class})
@Schema(description = "数据权限范围：1-全部，2-本部门，3-本部门及下级，4-仅本人")
private Integer dataScope;
```

**Step 3: 在 RoleVO 中添加 dataScope 字段**

在 VO 类中添加：

```java
@Schema(description = "数据权限范围：1-全部，2-本部门，3-本部门及下级，4-仅本人")
private Integer dataScope;
```

**Step 4: 验证编译**

Run: `cd backend && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/system/dto/RoleDTO.java
git add backend/src/main/java/com/devlovecode/aiperm/modules/system/vo/RoleVO.java
git commit -m "feat(data-scope): add dataScope to RoleDTO and RoleVO"
```

---

## Summary

| Phase | Task | Description |
|-------|------|-------------|
| 1 | 1.1 | 移除 build.gradle 中的多数据源依赖 |
| 1 | 1.2 | 简化 application.yaml 数据源配置 |
| 2 | 2.1 | 创建 ExcelExportHelper 工具类 |
| 3 | 3.1 | 创建 DataScopeEnum 枚举 |
| 3 | 3.2 | 创建 DataScopeHolder 持有者 |
| 3 | 3.3 | 创建 DataScopeService 服务 |
| 3 | 3.4 | 创建 DataScopeInterceptor 拦截器 |
| 3 | 3.5 | 注册拦截器到 WebMvcConfig |
| 3 | 3.6 | SysRole 添加 dataScope 字段 |
| 3 | 3.7 | 更新 RoleDTO 和 RoleVO |
