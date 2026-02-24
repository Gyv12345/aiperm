# aiperm RBAC 脚手架重构实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将 aiperm 重构为 AI 友好的 RBAC 通用脚手架，消除重复代码、合并 menu/permission 表、新增日志/OSS 模块、完善 AI 开发规范。

**Architecture:** 技术栈不变（Spring Boot 3.5 + Sa-Token + MyBatis-Plus + Vue3 + Orval）。重构分为 7 个独立任务，按顺序执行：先清理重复代码，再数据库迁移，再新增功能模块，最后更新规范文档。

**Tech Stack:** Java 21, Spring Boot 3.5.11, Sa-Token 1.39.0, MyBatis-Plus 3.5.9, Flyway, 阿里云 OSS SDK, Spring AOP, MapStruct-Plus

---

## 前置检查

确认本地环境正常：
```bash
# 后端可以编译
cd /Users/shichenyang/IdeaProjects/aiperm
./gradlew compileJava

# 预期：BUILD SUCCESSFUL
```

---

## Task 1: 清除重复的 common/entity 包和 common/handler

**目标：** 删除 `common/entity/`（3个文件）和 `common/handler/GlobalExceptionHandler.java`，修复唯一引用它们的文件。

**Files:**
- Delete: `src/main/java/com/devlovecode/aiperm/common/entity/BaseEntity.java`
- Delete: `src/main/java/com/devlovecode/aiperm/common/entity/PageResult.java`
- Delete: `src/main/java/com/devlovecode/aiperm/common/entity/R.java`
- Delete: `src/main/java/com/devlovecode/aiperm/common/handler/GlobalExceptionHandler.java`

**Step 1: 确认 common/entity 只被 common/handler 引用（不被业务代码引用）**

```bash
grep -r "common\.entity" src/main/java/ --include="*.java"
```

预期输出：只有 `common/handler/GlobalExceptionHandler.java` 和 `common/entity/` 自身的文件。

**Step 2: 删除文件**

```bash
rm src/main/java/com/devlovecode/aiperm/common/entity/BaseEntity.java
rm src/main/java/com/devlovecode/aiperm/common/entity/PageResult.java
rm src/main/java/com/devlovecode/aiperm/common/entity/R.java
rmdir src/main/java/com/devlovecode/aiperm/common/entity/
rm src/main/java/com/devlovecode/aiperm/common/handler/GlobalExceptionHandler.java
rmdir src/main/java/com/devlovecode/aiperm/common/handler/
```

**Step 3: 验证编译通过**

```bash
./gradlew compileJava
```

预期：`BUILD SUCCESSFUL`

**Step 4: 修复 GlobalExceptionHandler 中的 ErrorCode 引用错误**

打开 `src/main/java/com/devlovecode/aiperm/common/exception/GlobalExceptionHandler.java`，
将第 99 行 `ErrorCode.SERVER_ERROR` 改为 `ErrorCode.SYSTEM_ERROR`（枚举中实际的名称）。

**Step 5: 再次验证编译**

```bash
./gradlew compileJava
```

预期：`BUILD SUCCESSFUL`

**Step 6: Commit**

```bash
git add -A
git commit -m "refactor: 删除重复的 common/entity 包和 common/handler，修复 ErrorCode 引用"
```

---

## Task 2: 添加 Flyway 并创建 V2 迁移脚本（合并 menu/permission 表）

**目标：** 集成 Flyway 自动化数据库版本管理，执行 V2 迁移：删除 `sys_permission`/`sys_role_permission` 表，给 `sys_menu` 增加 `perms` 字段。

**Files:**
- Modify: `build.gradle`
- Modify: `src/main/resources/application.yaml`
- Create: `src/main/resources/db/migration/V2.0.0__merge_menu_permission.sql`

**Step 1: 在 build.gradle 添加 Flyway 依赖**

在 `dependencies` 块末尾（`testRuntimeOnly` 行之前）添加：

```groovy
// Flyway 数据库版本管理
implementation 'org.flywaydb:flyway-core'
implementation 'org.flywaydb:flyway-mysql'
```

**Step 2: 在 application.yaml 添加 Flyway 配置**

在文件末尾追加：

```yaml
# Flyway 数据库迁移配置
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 1
    validate-on-migrate: true
```

注意：`baseline-on-migrate: true` + `baseline-version: 1` 表示把现有 V1 脚本标记为已执行基线，不会重新执行。

**Step 3: 创建 V2 迁移脚本**

创建文件 `src/main/resources/db/migration/V2.0.0__merge_menu_permission.sql`，内容：

```sql
-- ===================================================
-- V2.0.0 合并 sys_permission 和 sys_menu
-- ===================================================

-- 1. 给 sys_menu 添加 perms 权限标识字段
ALTER TABLE `sys_menu`
    ADD COLUMN `perms` VARCHAR(100) DEFAULT NULL COMMENT '权限标识（按钮用，如 system:user:add）'
        AFTER `component`;

-- 2. 删除角色-权限关联表（不再使用）
DROP TABLE IF EXISTS `sys_role_permission`;

-- 3. 删除独立权限表（不再使用）
DROP TABLE IF EXISTS `sys_permission`;

-- 4. 新增操作日志表
CREATE TABLE IF NOT EXISTS `sys_oper_log` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志主键',
    `title`          VARCHAR(50)  DEFAULT '' COMMENT '操作模块',
    `oper_type`      TINYINT      DEFAULT 0 COMMENT '操作类型：1-新增 2-修改 3-删除 4-查询',
    `method`         VARCHAR(200) DEFAULT '' COMMENT '方法名',
    `request_method` VARCHAR(10)  DEFAULT '' COMMENT 'HTTP请求方式',
    `oper_url`       VARCHAR(255) DEFAULT '' COMMENT '请求URL',
    `oper_ip`        VARCHAR(128) DEFAULT '' COMMENT '操作IP',
    `oper_param`     TEXT         COMMENT '请求参数',
    `json_result`    TEXT         COMMENT '返回参数',
    `status`         TINYINT      DEFAULT 0 COMMENT '操作状态：0-成功 1-失败',
    `error_msg`      VARCHAR(2000) DEFAULT '' COMMENT '错误消息',
    `cost_time`      BIGINT       DEFAULT 0 COMMENT '消耗时间（ms）',
    `oper_user`      VARCHAR(50)  DEFAULT '' COMMENT '操作人账号',
    `oper_name`      VARCHAR(50)  DEFAULT '' COMMENT '操作人名称',
    `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_oper_type` (`oper_type`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志记录';

-- 5. 新增文件记录表
CREATE TABLE IF NOT EXISTS `sys_file` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    `file_name`     VARCHAR(200)  NOT NULL COMMENT '存储文件名',
    `original_name` VARCHAR(200)  DEFAULT NULL COMMENT '原始文件名',
    `file_path`     VARCHAR(500)  DEFAULT NULL COMMENT '存储路径（本地相对路径）',
    `file_url`      VARCHAR(500)  DEFAULT NULL COMMENT '访问URL',
    `file_size`     BIGINT        DEFAULT NULL COMMENT '文件大小（字节）',
    `file_type`     VARCHAR(100)  DEFAULT NULL COMMENT 'MIME类型',
    `storage_type`  VARCHAR(20)   DEFAULT 'local' COMMENT '存储类型：local/aliyun',
    `deleted`       TINYINT       DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
    `create_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `create_by`     VARCHAR(50)   DEFAULT NULL COMMENT '上传人',
    PRIMARY KEY (`id`),
    KEY `idx_storage_type` (`storage_type`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件记录表';
```

**Step 4: 启动服务验证 Flyway 迁移执行成功**

```bash
./gradlew bootRun
```

观察启动日志，应看到：
```
Flyway Community Edition ... by Redgate
Database: jdbc:mysql://localhost:3306/aiperm
Successfully validated 2 migrations
Current version of schema `aiperm`: 1
Migrating schema `aiperm` to version "2.0.0 - merge menu permission"
Successfully applied 1 migration to schema `aiperm`
```

**Step 5: 验证数据库变更**

```bash
python3 .claude/skills/db-query/scripts/query.py "SHOW TABLES"
```

预期：`sys_permission` 和 `sys_role_permission` 不再出现，`sys_oper_log` 和 `sys_file` 出现。

**Step 6: 删除 Java 中已废弃的 SysPermission 相关代码**

删除以下文件：
```bash
rm src/main/java/com/devlovecode/aiperm/modules/system/entity/SysPermission.java
rm src/main/java/com/devlovecode/aiperm/modules/system/mapper/SysPermissionMapper.java
rm src/main/java/com/devlovecode/aiperm/modules/system/service/ISysPermissionService.java
rm src/main/java/com/devlovecode/aiperm/modules/system/service/impl/SysPermissionServiceImpl.java
rm src/main/java/com/devlovecode/aiperm/modules/system/controller/SysPermissionController.java
rm src/main/resources/mapper/system/SysPermissionMapper.xml
```

**Step 7: 给 SysMenu 实体添加 perms 字段**

打开 `src/main/java/com/devlovecode/aiperm/modules/system/entity/SysMenu.java`，
在 `visible` 字段之前添加：

```java
@Schema(description = "权限标识（按钮用，如 system:user:add）")
@TableField("perms")
private String perms;
```

**Step 8: 验证编译通过**

```bash
./gradlew compileJava
```

预期：`BUILD SUCCESSFUL`

**Step 9: Commit**

```bash
git add -A
git commit -m "feat: 集成 Flyway，合并 sys_menu/sys_permission，删除 SysPermission 模块"
```

---

## Task 3: 新增 @Log 注解和 LogAspect AOP 切面

**目标：** 创建 `@Log` 注解和 `LogAspect` 切面，在 Controller 方法执行后自动将操作信息写入 `sys_oper_log`。

**Files:**
- Create: `src/main/java/com/devlovecode/aiperm/common/enums/OperType.java`
- Create: `src/main/java/com/devlovecode/aiperm/common/annotation/Log.java`
- Create: `src/main/java/com/devlovecode/aiperm/common/aspect/LogAspect.java`

**Step 1: 创建 OperType 枚举**

创建 `src/main/java/com/devlovecode/aiperm/common/enums/OperType.java`：

```java
package com.devlovecode.aiperm.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OperType {
    OTHER(0, "其他"),
    CREATE(1, "新增"),
    UPDATE(2, "修改"),
    DELETE(3, "删除"),
    QUERY(4, "查询"),
    EXPORT(5, "导出"),
    IMPORT(6, "导入"),
    UPLOAD(7, "上传");

    private final int code;
    private final String desc;
}
```

**Step 2: 创建 @Log 注解**

创建 `src/main/java/com/devlovecode/aiperm/common/annotation/Log.java`：

```java
package com.devlovecode.aiperm.common.annotation;

import com.devlovecode.aiperm.common.enums.OperType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /** 操作模块名称，如"用户管理" */
    String title() default "";

    /** 操作类型 */
    OperType operType() default OperType.OTHER;

    /** 是否保存请求参数，默认 true */
    boolean saveRequestParam() default true;

    /** 是否保存响应结果，默认 false（避免响应体过大） */
    boolean saveResponseResult() default false;
}
```

**Step 3: 创建 LogAspect 切面**

创建 `src/main/java/com/devlovecode/aiperm/common/aspect/LogAspect.java`：

```java
package com.devlovecode.aiperm.common.aspect;

import cn.hutool.json.JSONUtil;
import com.devlovecode.aiperm.common.annotation.Log;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class LogAspect {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Around("@annotation(com.devlovecode.aiperm.common.annotation.Log)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Log logAnnotation = method.getAnnotation(Log.class);

        // 获取请求信息
        HttpServletRequest request = null;
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            request = attributes.getRequest();
        }

        Object result = null;
        String errorMsg = null;
        int status = 0;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            status = 1;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            // 发布日志事件（异步处理，不影响主流程）
            LogEvent event = new LogEvent(
                logAnnotation.title(),
                logAnnotation.operType().getCode(),
                joinPoint.getTarget().getClass().getName() + "." + method.getName(),
                request != null ? request.getMethod() : "",
                request != null ? request.getRequestURI() : "",
                request != null ? getIp(request) : "",
                logAnnotation.saveRequestParam() ? buildParams(joinPoint.getArgs()) : "",
                logAnnotation.saveResponseResult() && result != null ? JSONUtil.toJsonStr(result) : "",
                status,
                errorMsg,
                costTime
            );
            eventPublisher.publishEvent(event);
        }
    }

    private String buildParams(Object[] args) {
        try {
            return JSONUtil.toJsonStr(args);
        } catch (Exception e) {
            return "";
        }
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
```

**Step 4: 创建 LogEvent 数据类**

创建 `src/main/java/com/devlovecode/aiperm/common/aspect/LogEvent.java`：

```java
package com.devlovecode.aiperm.common.aspect;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LogEvent {
    private String title;
    private int operType;
    private String method;
    private String requestMethod;
    private String operUrl;
    private String operIp;
    private String operParam;
    private String jsonResult;
    private int status;
    private String errorMsg;
    private long costTime;
}
```

**Step 5: 验证编译通过**

```bash
./gradlew compileJava
```

预期：`BUILD SUCCESSFUL`

**Step 6: Commit**

```bash
git add -A
git commit -m "feat: 新增 @Log 注解和 LogAspect AOP 切面，支持操作日志自动采集"
```

---

## Task 4: 新增操作日志模块（log）

**目标：** 创建完整的 `modules/log/` 模块，包含 Entity/Mapper/Service/Controller，并实现 LogEvent 监听器将日志异步写入数据库。

**Files:**
- Create: `src/main/java/com/devlovecode/aiperm/modules/log/entity/SysOperLog.java`
- Create: `src/main/java/com/devlovecode/aiperm/modules/log/mapper/SysOperLogMapper.java`
- Create: `src/main/java/com/devlovecode/aiperm/modules/log/service/ISysOperLogService.java`
- Create: `src/main/java/com/devlovecode/aiperm/modules/log/service/impl/SysOperLogServiceImpl.java`
- Create: `src/main/java/com/devlovecode/aiperm/modules/log/listener/LogEventListener.java`
- Create: `src/main/java/com/devlovecode/aiperm/modules/log/controller/SysOperLogController.java`
- Create: `src/main/resources/mapper/log/SysOperLogMapper.xml`

**Step 1: 创建 SysOperLog 实体**

```java
// src/main/java/com/devlovecode/aiperm/modules/log/entity/SysOperLog.java
package com.devlovecode.aiperm.modules.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_oper_log")
@Schema(description = "操作日志")
public class SysOperLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "操作模块")
    private String title;

    @Schema(description = "操作类型：1-新增 2-修改 3-删除 4-查询")
    private Integer operType;

    @Schema(description = "方法名")
    private String method;

    @Schema(description = "HTTP请求方式")
    private String requestMethod;

    @Schema(description = "请求URL")
    private String operUrl;

    @Schema(description = "操作IP")
    private String operIp;

    @Schema(description = "请求参数")
    private String operParam;

    @Schema(description = "返回参数")
    private String jsonResult;

    @Schema(description = "操作状态：0-成功 1-失败")
    private Integer status;

    @Schema(description = "错误消息")
    private String errorMsg;

    @Schema(description = "消耗时间（ms）")
    private Long costTime;

    @Schema(description = "操作人账号")
    private String operUser;

    @Schema(description = "操作人名称")
    private String operName;

    @Schema(description = "操作时间")
    private LocalDateTime createTime;
}
```

**Step 2: 创建 Mapper 接口**

```java
// src/main/java/com/devlovecode/aiperm/modules/log/mapper/SysOperLogMapper.java
package com.devlovecode.aiperm.modules.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devlovecode.aiperm.modules.log.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {
}
```

**Step 3: 创建 Mapper XML**

创建 `src/main/resources/mapper/log/SysOperLogMapper.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.devlovecode.aiperm.modules.log.mapper.SysOperLogMapper">
</mapper>
```

**Step 4: 创建 Service 接口和实现**

```java
// src/main/java/com/devlovecode/aiperm/modules/log/service/ISysOperLogService.java
package com.devlovecode.aiperm.modules.log.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.devlovecode.aiperm.modules.log.entity.SysOperLog;

public interface ISysOperLogService extends IService<SysOperLog> {
}
```

```java
// src/main/java/com/devlovecode/aiperm/modules/log/service/impl/SysOperLogServiceImpl.java
package com.devlovecode.aiperm.modules.log.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devlovecode.aiperm.modules.log.entity.SysOperLog;
import com.devlovecode.aiperm.modules.log.mapper.SysOperLogMapper;
import com.devlovecode.aiperm.modules.log.service.ISysOperLogService;
import org.springframework.stereotype.Service;

@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog>
        implements ISysOperLogService {
}
```

**Step 5: 创建 LogEventListener 异步监听器**

```java
// src/main/java/com/devlovecode/aiperm/modules/log/listener/LogEventListener.java
package com.devlovecode.aiperm.modules.log.listener;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.aspect.LogEvent;
import com.devlovecode.aiperm.modules.log.entity.SysOperLog;
import com.devlovecode.aiperm.modules.log.service.ISysOperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogEventListener {

    private final ISysOperLogService operLogService;

    @Async
    @EventListener
    public void onLogEvent(LogEvent event) {
        try {
            SysOperLog operLog = new SysOperLog();
            operLog.setTitle(event.getTitle());
            operLog.setOperType(event.getOperType());
            operLog.setMethod(event.getMethod());
            operLog.setRequestMethod(event.getRequestMethod());
            operLog.setOperUrl(event.getOperUrl());
            operLog.setOperIp(event.getOperIp());
            operLog.setOperParam(event.getOperParam());
            operLog.setJsonResult(event.getJsonResult());
            operLog.setStatus(event.getStatus());
            operLog.setErrorMsg(event.getErrorMsg());
            operLog.setCostTime(event.getCostTime());
            operLog.setCreateTime(LocalDateTime.now());

            // 尝试获取当前登录用户（可能未登录）
            try {
                String loginId = StpUtil.getLoginIdAsString();
                operLog.setOperUser(loginId);
            } catch (Exception ignored) {
                operLog.setOperUser("anonymous");
            }

            operLogService.save(operLog);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }
}
```

**Step 6: 在主启动类上开启异步支持**

打开 `src/main/java/com/devlovecode/aiperm/AipermApplication.java`，添加 `@EnableAsync`：

```java
@EnableAsync
@SpringBootApplication
public class AipermApplication { ... }
```

**Step 7: 创建操作日志查询 Controller**

```java
// src/main/java/com/devlovecode/aiperm/modules/log/controller/SysOperLogController.java
package com.devlovecode.aiperm.modules.log.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.modules.log.entity.SysOperLog;
import com.devlovecode.aiperm.modules.log.service.ISysOperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "操作日志管理")
@RestController
@RequestMapping("/system/oper-log")
@SaCheckLogin
@RequiredArgsConstructor
public class SysOperLogController {

    private final ISysOperLogService operLogService;

    @Operation(summary = "分页查询操作日志")
    @SaCheckPermission("log:oper:list")
    @GetMapping("/page")
    public R<PageResult<SysOperLog>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<SysOperLog>()
                .like(title != null, SysOperLog::getTitle, title)
                .eq(status != null, SysOperLog::getStatus, status)
                .orderByDesc(SysOperLog::getCreateTime);

        Page<SysOperLog> pageResult = operLogService.page(new Page<>(page, pageSize), wrapper);
        return R.ok(PageResult.of(pageResult));
    }

    @Operation(summary = "删除操作日志")
    @SaCheckPermission("log:oper:delete")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        operLogService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "清空操作日志")
    @SaCheckPermission("log:oper:delete")
    @DeleteMapping("/clean")
    public R<Void> clean() {
        operLogService.remove(null);
        return R.ok();
    }
}
```

**Step 8: 验证编译通过**

```bash
./gradlew compileJava
```

**Step 9: 启动服务并验证 @Log 注解工作**

```bash
./gradlew bootRun
```

调用任意一个有 `@Log` 注解的 Controller 接口，然后查询：
```bash
python3 .claude/skills/db-query/scripts/query.py "SELECT * FROM sys_oper_log LIMIT 5"
```

预期：有日志记录写入。

**Step 10: Commit**

```bash
git add -A
git commit -m "feat: 新增操作日志模块，@Log 注解异步写入 sys_oper_log"
```

---

## Task 5: 新增 OSS 文件存储模块

**目标：** 创建 `modules/oss/` 模块，支持本地存储和阿里云 OSS，通过配置 `storage-type` 切换，提供文件上传/下载接口。

**Files:**
- Modify: `build.gradle`（添加阿里云 OSS SDK）
- Modify: `src/main/resources/application.yaml`（添加 OSS 配置）
- Create: `src/main/java/com/devlovecode/aiperm/modules/oss/config/OssProperties.java`
- Create: `src/main/java/com/devlovecode/aiperm/modules/oss/service/OssService.java`
- Create: `src/main/java/com/devlovecode/aiperm/modules/oss/service/impl/LocalOssServiceImpl.java`
- Create: `src/main/java/com/devlovecode/aiperm/modules/oss/service/impl/AliyunOssServiceImpl.java`
- Create: `src/main/java/com/devlovecode/aiperm/modules/oss/domain/OssResult.java`
- Create: `src/main/java/com/devlovecode/aiperm/modules/oss/controller/OssController.java`

**Step 1: 添加阿里云 OSS SDK 依赖（可选，通过 compileOnly 避免强依赖）**

在 `build.gradle` 中添加：

```groovy
// 阿里云 OSS SDK（可选）
implementation('com.aliyun.oss:aliyun-sdk-oss:3.18.1') {
    exclude group: 'org.slf4j', module: 'slf4j-api'
}
```

**Step 2: 在 application.yaml 添加 OSS 配置**

```yaml
# OSS 文件存储配置
oss:
  storage-type: local   # local / aliyun
  local:
    path: ./uploads     # 本地存储根目录
    access-url: http://localhost:8080/api/files  # 本地文件访问URL前缀
  aliyun:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    access-key-id: your-access-key-id
    access-key-secret: your-access-key-secret
    bucket-name: your-bucket-name
    access-url: https://your-bucket.oss-cn-hangzhou.aliyuncs.com
```

**Step 3: 创建 OssProperties 配置类**

```java
// src/main/java/com/devlovecode/aiperm/modules/oss/config/OssProperties.java
package com.devlovecode.aiperm.modules.oss.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    /** 存储类型：local / aliyun */
    private String storageType = "local";

    private Local local = new Local();
    private Aliyun aliyun = new Aliyun();

    @Data
    public static class Local {
        private String path = "./uploads";
        private String accessUrl = "http://localhost:8080/api/files";
    }

    @Data
    public static class Aliyun {
        private String endpoint;
        private String accessKeyId;
        private String accessKeySecret;
        private String bucketName;
        private String accessUrl;
    }
}
```

**Step 4: 创建 OssResult 返回对象**

```java
// src/main/java/com/devlovecode/aiperm/modules/oss/domain/OssResult.java
package com.devlovecode.aiperm.modules.oss.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OssResult {
    /** 存储文件名（UUID生成）*/
    private String fileName;
    /** 原始文件名 */
    private String originalName;
    /** 文件访问URL */
    private String url;
    /** 文件大小（字节）*/
    private Long size;
    /** MIME类型 */
    private String contentType;
}
```

**Step 5: 创建 OssService 接口**

```java
// src/main/java/com/devlovecode/aiperm/modules/oss/service/OssService.java
package com.devlovecode.aiperm.modules.oss.service;

import com.devlovecode.aiperm.modules.oss.domain.OssResult;
import org.springframework.web.multipart.MultipartFile;

public interface OssService {
    /** 上传文件，返回访问信息 */
    OssResult upload(MultipartFile file);

    /** 删除文件（传入 fileName，即 OssResult.fileName）*/
    void delete(String fileName);
}
```

**Step 6: 创建本地存储实现**

```java
// src/main/java/com/devlovecode/aiperm/modules/oss/service/impl/LocalOssServiceImpl.java
package com.devlovecode.aiperm.modules.oss.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.devlovecode.aiperm.common.enums.ErrorCode;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.oss.config.OssProperties;
import com.devlovecode.aiperm.modules.oss.domain.OssResult;
import com.devlovecode.aiperm.modules.oss.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "oss.storage-type", havingValue = "local", matchIfMissing = true)
public class LocalOssServiceImpl implements OssService {

    private final OssProperties ossProperties;

    @Override
    public OssResult upload(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String ext = StrUtil.isNotBlank(originalName) && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : "";
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = UUID.randomUUID().toString(true) + ext;
        String relativePath = datePath + "/" + fileName;
        String fullPath = ossProperties.getLocal().getPath() + "/" + relativePath;

        try {
            File dest = new File(fullPath);
            FileUtil.mkParentDirs(dest);
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("本地文件上传失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        String url = ossProperties.getLocal().getAccessUrl() + "/" + relativePath;
        return OssResult.builder()
                .fileName(relativePath)
                .originalName(originalName)
                .url(url)
                .size(file.getSize())
                .contentType(file.getContentType())
                .build();
    }

    @Override
    public void delete(String fileName) {
        String fullPath = ossProperties.getLocal().getPath() + "/" + fileName;
        FileUtil.del(fullPath);
    }
}
```

**Step 7: 创建阿里云 OSS 实现（占位，需配置后启用）**

```java
// src/main/java/com/devlovecode/aiperm/modules/oss/service/impl/AliyunOssServiceImpl.java
package com.devlovecode.aiperm.modules.oss.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.devlovecode.aiperm.common.enums.ErrorCode;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.oss.config.OssProperties;
import com.devlovecode.aiperm.modules.oss.domain.OssResult;
import com.devlovecode.aiperm.modules.oss.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "oss.storage-type", havingValue = "aliyun")
public class AliyunOssServiceImpl implements OssService {

    private final OssProperties ossProperties;

    @Override
    public OssResult upload(MultipartFile file) {
        OssProperties.Aliyun config = ossProperties.getAliyun();
        String originalName = file.getOriginalFilename();
        String ext = StrUtil.isNotBlank(originalName) && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : "";
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = datePath + "/" + UUID.randomUUID().toString(true) + ext;

        OSS ossClient = new OSSClientBuilder().build(config.getEndpoint(), config.getAccessKeyId(), config.getAccessKeySecret());
        try {
            ossClient.putObject(config.getBucketName(), fileName, file.getInputStream());
        } catch (Exception e) {
            log.error("阿里云 OSS 上传失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        } finally {
            ossClient.shutdown();
        }

        String url = config.getAccessUrl() + "/" + fileName;
        return OssResult.builder()
                .fileName(fileName)
                .originalName(originalName)
                .url(url)
                .size(file.getSize())
                .contentType(file.getContentType())
                .build();
    }

    @Override
    public void delete(String fileName) {
        OssProperties.Aliyun config = ossProperties.getAliyun();
        OSS ossClient = new OSSClientBuilder().build(config.getEndpoint(), config.getAccessKeyId(), config.getAccessKeySecret());
        try {
            ossClient.deleteObject(config.getBucketName(), fileName);
        } finally {
            ossClient.shutdown();
        }
    }
}
```

**Step 8: 创建 OssController 并配置静态资源映射**

```java
// src/main/java/com/devlovecode/aiperm/modules/oss/controller/OssController.java
package com.devlovecode.aiperm.modules.oss.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.oss.domain.OssResult;
import com.devlovecode.aiperm.modules.oss.service.OssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文件管理")
@RestController
@RequestMapping("/oss")
@SaCheckLogin
@RequiredArgsConstructor
public class OssController {

    private final OssService ossService;

    @Operation(summary = "上传文件")
    @Log(title = "文件管理", operType = OperType.UPLOAD)
    @PostMapping("/upload")
    public R<OssResult> upload(@RequestParam("file") MultipartFile file) {
        return R.ok(ossService.upload(file));
    }

    @Operation(summary = "删除文件")
    @Log(title = "文件管理", operType = OperType.DELETE)
    @DeleteMapping
    public R<Void> delete(@RequestParam String fileName) {
        ossService.delete(fileName);
        return R.ok();
    }
}
```

在 `config/WebMvcConfig.java` 中添加本地文件访问映射：

打开现有的 `WebMvcConfig.java`，在 `addResourceHandlers` 方法中添加：

```java
// 本地 OSS 文件访问映射
registry.addResourceHandler("/files/**")
        .addResourceLocations("file:./uploads/");
```

**Step 9: 验证编译并测试上传**

```bash
./gradlew compileJava
./gradlew bootRun
```

用 curl 测试：
```bash
curl -X POST http://localhost:8080/api/oss/upload \
  -H "Authorization: <登录后的token>" \
  -F "file=@/tmp/test.png"
```

预期：返回包含 `url` 字段的 JSON，本地 `./uploads/` 目录下有文件。

**Step 10: Commit**

```bash
git add -A
git commit -m "feat: 新增 OSS 文件存储模块，支持本地存储和阿里云 OSS 切换"
```

---

## Task 6: 新增字典管理 demo 模块

**目标：** 创建完整的字典管理模块（`SysDict`），作为 AI 开发新业务功能的标准参照。包含字典类型和字典数据两个子表。

**Files:**
- Create: `src/main/resources/db/migration/V2.1.0__add_dict_tables.sql`
- Create: `src/main/java/com/devlovecode/aiperm/modules/system/entity/SysDictType.java`
- Create: `src/main/java/com/devlovecode/aiperm/modules/system/entity/SysDictData.java`
- Create: `src/main/java/com/devlovecode/aiperm/modules/system/mapper/SysDictTypeMapper.java`
- Create: `src/main/java/com/devlovecode/aiperm/modules/system/mapper/SysDictDataMapper.java`
- Create: `src/main/java/com/devlovecode/aiperm/modules/system/service/ISysDictTypeService.java`
- Create: `src/main/java/com/devlovecode/aiperm/modules/system/service/impl/SysDictTypeServiceImpl.java`
- Create: `src/main/java/com/devlovecode/aiperm/modules/system/controller/SysDictTypeController.java`
- Create: `src/main/java/com/devlovecode/aiperm/modules/system/dto/request/DictTypeCreateRequest.java`
- Create: `src/main/java/com/devlovecode/aiperm/modules/system/vo/DictTypeVO.java`
- Create: `src/main/resources/mapper/system/SysDictTypeMapper.xml`

**Step 1: 创建字典表迁移脚本**

创建 `src/main/resources/db/migration/V2.1.0__add_dict_tables.sql`：

```sql
-- 字典类型表
CREATE TABLE IF NOT EXISTS `sys_dict_type` (
    `id`        BIGINT      NOT NULL AUTO_INCREMENT COMMENT '字典类型ID',
    `dict_name` VARCHAR(100) NOT NULL COMMENT '字典名称',
    `dict_type` VARCHAR(100) NOT NULL COMMENT '字典类型（唯一标识，如 sys_gender）',
    `status`    TINYINT     DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `remark`    VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted`   TINYINT     DEFAULT 0,
    `version`   INT         DEFAULT 0,
    `create_time` DATETIME  DEFAULT CURRENT_TIMESTAMP,
    `create_by` VARCHAR(50) DEFAULT NULL,
    `update_time` DATETIME  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by` VARCHAR(50) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_type` (`dict_type`)
) COMMENT='字典类型表';

-- 字典数据表
CREATE TABLE IF NOT EXISTS `sys_dict_data` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '字典数据ID',
    `dict_type`  VARCHAR(100) NOT NULL COMMENT '字典类型',
    `dict_label` VARCHAR(100) NOT NULL COMMENT '字典标签（显示值）',
    `dict_value` VARCHAR(100) NOT NULL COMMENT '字典键值（存储值）',
    `sort`       INT         DEFAULT 0 COMMENT '排序',
    `status`     TINYINT     DEFAULT 1,
    `remark`     VARCHAR(500) DEFAULT NULL,
    `deleted`    TINYINT     DEFAULT 0,
    `version`    INT         DEFAULT 0,
    `create_time` DATETIME   DEFAULT CURRENT_TIMESTAMP,
    `create_by`  VARCHAR(50) DEFAULT NULL,
    `update_time` DATETIME   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`  VARCHAR(50) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_dict_type` (`dict_type`)
) COMMENT='字典数据表';

-- 初始化内置字典
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`) VALUES
('用户性别', 'sys_gender', 1),
('系统开关', 'sys_switch', 1),
('通用状态', 'sys_status', 1);

INSERT INTO `sys_dict_data` (`dict_type`, `dict_label`, `dict_value`, `sort`) VALUES
('sys_gender', '未知', '0', 0), ('sys_gender', '男', '1', 1), ('sys_gender', '女', '2', 2),
('sys_switch', '开启', '1', 0), ('sys_switch', '关闭', '0', 1),
('sys_status', '启用', '1', 0), ('sys_status', '禁用', '0', 1);
```

**Step 2-10: 创建完整的 SysDictType/SysDictData CRUD 代码**

> 按照标准分层约定（Entity → Mapper → Service → Controller → DTO → VO → Converter）完整实现字典类型的 CRUD，以及字典数据的 CRUD。Controller 上每个写操作加 `@Log` 注解。
>
> 这个模块的目的是**作为 AI 参照**，代码结构需要完整规范。后续所有新业务模块都以此为模板。

**Step 11: 验证编译和启动**

```bash
./gradlew bootRun
```

访问 Swagger：http://localhost:8080/api/swagger-ui.html，确认字典管理接口出现。

**Step 12: Commit**

```bash
git add -A
git commit -m "feat: 新增字典管理模块（SysDict），作为 AI 开发的标准参照模块"
```

---

## Task 7: 更新 CLAUDE.md 和 aiperm-dev 技能

**目标：** 将所有约定写入 `CLAUDE.md` 后端规范章节，更新 `aiperm-dev` 技能文件，使 AI 读取后能无歧义地开发任何新业务模块。

**Files:**
- Modify: `CLAUDE.md`（当前项目目录下）
- Modify: `.claude/skills/aiperm-dev/skill.md`（或对应技能文件）

**Step 1: 在 CLAUDE.md 新增"后端开发规范"章节**

在现有 `CLAUDE.md` 的末尾添加以下章节：

```markdown
## 后端开发规范（AI 必读）

### 标准分层约定

| 层 | 类名规范 | 继承/实现 |
|----|---------|----------|
| Entity | `SysXxx` | `extends BaseEntity`（common/domain） |
| Mapper | `SysXxxMapper` | `extends BaseMapper<SysXxx>` + `@Mapper` |
| Service 接口 | `ISysXxxService` | `extends IService<SysXxx>` |
| Service 实现 | `SysXxxServiceImpl` | `extends ServiceImpl<Mapper, Entity>` + `@Service` |
| Controller | `SysXxxController` | `@RestController @RequestMapping("/system/xxx")` |
| 请求 DTO | `XxxCreateRequest` / `XxxUpdateRequest` / `XxxQueryRequest` | — |
| 响应 VO | `XxxVO` | — |
| 转换器 | `XxxConverter` | MapStruct-Plus `@Mapper` 接口 |

### 文件位置约定

```
modules/system/
├── entity/        SysXxx.java
├── mapper/        SysXxxMapper.java
├── service/       ISysXxxService.java
│   └── impl/      SysXxxServiceImpl.java
├── controller/    SysXxxController.java
├── dto/request/   XxxCreateRequest.java / XxxUpdateRequest.java / XxxQueryRequest.java
├── vo/            XxxVO.java
└── converter/     XxxConverter.java
resources/mapper/system/SysXxxMapper.xml
```

### 权限码约定

格式：`模块:资源:操作`

示例：
- `system:user:list` / `system:user:create` / `system:user:update` / `system:user:delete`
- `system:role:list` / `system:role:create`
- `system:dict:list` / `system:dict:create`
- `log:oper:list` / `log:oper:delete`
- `oss:file:upload` / `oss:file:delete`

### @Log 注解使用（每个写操作必须加）

```java
@Log(title = "用户管理", operType = OperType.CREATE)
@PostMapping
public R<Void> create(@RequestBody @Valid UserCreateRequest req) { ... }

@Log(title = "用户管理", operType = OperType.UPDATE)
@PutMapping("/{id}")
public R<Void> update(@PathVariable Long id, @RequestBody @Valid UserUpdateRequest req) { ... }

@Log(title = "用户管理", operType = OperType.DELETE)
@DeleteMapping("/{id}")
public R<Void> delete(@PathVariable Long id) { ... }
```

### 新增业务模块检查清单

开发新模块前必须逐项确认：

- [ ] 参照 `modules/system/` 下的字典管理（SysDictType）作为模板
- [ ] 建表 SQL 写入新 Flyway 迁移文件 `Vx.x.x__描述.sql`
- [ ] Entity 继承 `BaseEntity`，加 `@TableName("表名")` `@Schema(description = "xxx")`
- [ ] Controller 每个写操作加 `@Log(title = "模块名", operType = OperType.xxx)`
- [ ] Controller 加 `@SaCheckLogin` + 方法级 `@SaCheckPermission("模块:资源:操作")`
- [ ] 按钮权限写入 sys_menu 初始化数据（`menu_type='F'`, `perms='module:res:action'`）
- [ ] 后端完成后运行 `cd frontend && pnpm run generate:api`
- [ ] 前端使用生成的 API 函数，禁止手写 axios 调用

### OSS 上传使用示例

```java
@Autowired
private OssService ossService;

// 上传文件
OssResult result = ossService.upload(multipartFile);
String fileUrl = result.getUrl();  // 完整访问地址
String fileName = result.getFileName();  // 存储文件名（用于删除）

// 删除文件
ossService.delete(fileName);
```
```

**Step 2: 更新 aiperm-dev 技能文件**

找到技能文件路径：
```bash
find ~/.claude -name "*.md" | xargs grep -l "aiperm" 2>/dev/null
```

在技能文件中补充：
1. 完整的 7 步开发流程（建表→Entity→Mapper→Service→Controller→DTO/VO→前端生成）
2. 命名规范完整表格
3. 指向字典管理模块作为参照：`modules/system/entity/SysDictType.java`
4. 所有注解使用示例（`@Log`、`@SaCheckPermission`、`@SaCheckLogin`）
5. 前后端 API 联调流程（后端 Swagger → Orval 生成 → 前端使用）

**Step 3: 验证项目整体编译和启动**

```bash
./gradlew build
./gradlew bootRun
```

访问 http://localhost:8080/api/swagger-ui.html，确认以下模块全部出现：
- 认证管理（auth）
- 用户/角色/菜单/部门/岗位管理（system）
- 字典管理（system/dict）
- 操作日志（log）
- 文件管理（oss）

**Step 4: 最终 Commit**

```bash
git add -A
git commit -m "docs: 更新 CLAUDE.md 后端开发规范，更新 aiperm-dev 技能文件

- 新增标准分层约定、权限码规范、@Log 使用示例
- 新增新模块开发检查清单
- 技能文件补充完整 7 步开发流程和示例代码"
```

---

## 完成验证

全部 7 个任务完成后，执行最终验证：

```bash
# 1. 编译通过
./gradlew build

# 2. 确认无重复包
find src/main/java -name "*.java" | xargs grep -l "common\.entity" | grep -v "common/entity"
# 预期：无输出

# 3. 确认数据库表结构
python3 .claude/skills/db-query/scripts/query.py "SHOW TABLES"
# 预期：无 sys_permission，有 sys_oper_log, sys_file, sys_dict_type, sys_dict_data

# 4. 前端 API 重新生成
cd frontend && pnpm run generate:api
# 预期：成功生成，无错误
```
