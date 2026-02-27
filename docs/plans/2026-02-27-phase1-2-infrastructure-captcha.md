# Phase 1-2: 基础设施 + 验证码服务

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 建立多登录与2FA功能的数据库结构，集成短信/邮件服务，实现带限流的验证码发送接口。

---

## Task 1: 添加 Gradle 依赖

**Files:**
- Modify: `backend/build.gradle`

**Step 1: 在 `ext { }` 块添加版本变量**

在 `ext { }` 块（约第40行）的现有版本后面添加：

```groovy
set('sms4jVersion', '3.3.3')
```

**Step 2: 在 `dependencies { }` 中添加依赖**

在 `// Test` 注释之前添加：

```groovy
// 邮件发送
implementation 'org.springframework.boot:spring-boot-starter-mail'

// sms4j 短信发送（支持阿里云、腾讯云等）
implementation "org.dromara.sms4j:sms4j-spring-boot-starter:${sms4jVersion}"
```

**Step 3: 验证编译成功**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm/backend && ./gradlew dependencies --configuration compileClasspath | grep -E "sms4j|starter-mail"
```

期望输出包含：`org.dromara.sms4j:sms4j-spring-boot-starter:3.3.3` 和 `spring-boot-starter-mail`

**Step 4: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add backend/build.gradle
git commit -m "build: add sms4j and spring-boot-starter-mail dependencies"
```

---

## Task 2: 创建数据库迁移脚本（基础表）

**Files:**
- Create: `backend/src/main/resources/db/migration/V4.0.0__multi_login_2fa_tables.sql`

**Step 1: 创建 SQL 迁移文件**

```sql
-- V4.0.0: 多方式登录与2FA功能基础表

-- 1. 用户2FA绑定表
CREATE TABLE IF NOT EXISTS `sys_user_mfa` (
    `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`      BIGINT      NOT NULL COMMENT '用户ID',
    `mfa_type`     VARCHAR(20) NOT NULL DEFAULT 'TOTP' COMMENT 'MFA类型:TOTP',
    `secret_key`   VARCHAR(100) NOT NULL COMMENT 'TOTP密钥(Base32编码)',
    `bind_time`    DATETIME    DEFAULT NULL COMMENT '绑定时间',
    `status`       TINYINT     DEFAULT 1 COMMENT '状态:1已启用,0已禁用',
    `deleted`      TINYINT     DEFAULT 0 COMMENT '删除标记',
    `version`      INT         DEFAULT 0 COMMENT '乐观锁版本',
    `create_time`  DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`    VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `update_time`  DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by`    VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户MFA绑定表';

-- 2. 2FA策略配置表
CREATE TABLE IF NOT EXISTS `sys_mfa_policy` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`          VARCHAR(100) NOT NULL COMMENT '策略名称',
    `perm_pattern`  VARCHAR(200) DEFAULT NULL COMMENT '权限标识匹配(支持通配符*)',
    `api_pattern`   VARCHAR(200) DEFAULT NULL COMMENT 'API路径匹配(支持通配符*)',
    `enabled`       TINYINT      DEFAULT 1 COMMENT '是否启用:1是,0否',
    `deleted`       TINYINT      DEFAULT 0,
    `version`       INT          DEFAULT 0,
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `create_by`     VARCHAR(50)  DEFAULT NULL,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`     VARCHAR(50)  DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='2FA策略配置表';

-- 3. 用户第三方账号绑定表
CREATE TABLE IF NOT EXISTS `sys_user_oauth` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`         BIGINT       NOT NULL COMMENT '用户ID',
    `platform`        VARCHAR(20)  NOT NULL COMMENT '平台:WEWORK/DINGTALK/FEISHU',
    `open_id`         VARCHAR(100) NOT NULL COMMENT '第三方平台用户标识',
    `union_id`        VARCHAR(100) DEFAULT NULL COMMENT '企业统一标识(可选)',
    `nickname`        VARCHAR(100) DEFAULT NULL COMMENT '第三方昵称',
    `avatar`          VARCHAR(500) DEFAULT NULL COMMENT '第三方头像',
    `last_login_time` DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态:1正常,0已解绑',
    `deleted`         TINYINT      DEFAULT 0,
    `version`         INT          DEFAULT 0,
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `create_by`       VARCHAR(50)  DEFAULT NULL,
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`       VARCHAR(50)  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_platform_openid` (`platform`, `open_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户第三方账号绑定表';

-- 4. OAuth配置表
CREATE TABLE IF NOT EXISTS `sys_oauth_config` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `platform`     VARCHAR(20)  NOT NULL COMMENT '平台:WEWORK/DINGTALK/FEISHU',
    `enabled`      TINYINT      DEFAULT 0 COMMENT '是否启用:1是,0否',
    `corp_id`      VARCHAR(100) DEFAULT NULL COMMENT '企业ID',
    `agent_id`     VARCHAR(100) DEFAULT NULL COMMENT '应用ID',
    `app_key`      VARCHAR(200) DEFAULT NULL COMMENT 'AppKey',
    `app_secret`   VARCHAR(200) DEFAULT NULL COMMENT 'AppSecret',
    `callback_url` VARCHAR(500) DEFAULT NULL COMMENT '回调地址',
    `remark`       VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted`      TINYINT      DEFAULT 0,
    `version`      INT          DEFAULT 0,
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `create_by`    VARCHAR(50)  DEFAULT NULL,
    `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`    VARCHAR(50)  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_platform` (`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OAuth配置表';

-- 5. 验证码配置表
CREATE TABLE IF NOT EXISTS `sys_captcha_config` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `type`              VARCHAR(20)  NOT NULL COMMENT '类型:SMS/EMAIL',
    `enabled`           TINYINT      DEFAULT 0 COMMENT '是否启用:1是,0否',
    `sms_provider`      VARCHAR(50)  DEFAULT NULL COMMENT '短信服务商:ALIYUN/TENCENT',
    `sms_access_key`    VARCHAR(200) DEFAULT NULL COMMENT 'AccessKey',
    `sms_secret_key`    VARCHAR(200) DEFAULT NULL COMMENT 'SecretKey',
    `sms_sign_name`     VARCHAR(100) DEFAULT NULL COMMENT '签名名称',
    `sms_template_code` VARCHAR(100) DEFAULT NULL COMMENT '模板ID',
    `email_host`        VARCHAR(100) DEFAULT NULL COMMENT 'SMTP服务器',
    `email_port`        INT          DEFAULT NULL COMMENT '端口',
    `email_username`    VARCHAR(100) DEFAULT NULL COMMENT '邮箱账号',
    `email_password`    VARCHAR(200) DEFAULT NULL COMMENT '密码/授权码',
    `email_from`        VARCHAR(100) DEFAULT NULL COMMENT '发件人地址',
    `email_from_name`   VARCHAR(100) DEFAULT NULL COMMENT '发件人名称',
    `code_length`       INT          DEFAULT 6 COMMENT '验证码长度',
    `expire_minutes`    INT          DEFAULT 5 COMMENT '过期时间(分钟)',
    `daily_limit`       INT          DEFAULT 10 COMMENT '每日发送上限',
    `deleted`           TINYINT      DEFAULT 0,
    `version`           INT          DEFAULT 0,
    `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `create_by`         VARCHAR(50)  DEFAULT NULL,
    `update_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`         VARCHAR(50)  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证码配置表';

-- 6. 验证码发送记录表
CREATE TABLE IF NOT EXISTS `sys_captcha_log` (
    `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `type`         VARCHAR(20) NOT NULL COMMENT '类型:SMS/EMAIL',
    `target`       VARCHAR(100) NOT NULL COMMENT '目标(手机号/邮箱)',
    `code`         VARCHAR(10) NOT NULL COMMENT '验证码',
    `scene`        VARCHAR(50) DEFAULT NULL COMMENT '场景:LOGIN/BIND/RESET',
    `status`       TINYINT     DEFAULT NULL COMMENT '状态:1成功,0失败',
    `fail_reason`  VARCHAR(200) DEFAULT NULL COMMENT '失败原因',
    `ip`           VARCHAR(50) DEFAULT NULL COMMENT '请求IP',
    `create_time`  DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_target_time` (`target`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证码发送记录';

-- 7. 系统配置扩展（在 sys_config 中新增配置项）
INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_type`, `remark`)
VALUES
-- 登录方式开关
('login.sms.enabled',   '0', 'system', '短信验证码登录开关:0关闭,1开启'),
('login.email.enabled', '0', 'system', '邮箱验证码登录开关:0关闭,1开启'),
-- OAuth开关
('oauth.wework.enabled',  '0', 'system', '企业微信登录开关:0关闭,1开启'),
('oauth.dingtalk.enabled','0', 'system', '钉钉登录开关:0关闭,1开启'),
('oauth.feishu.enabled',  '0', 'system', '飞书登录开关:0关闭,1开启'),
-- 2FA配置
('mfa.enabled',                '1', 'system', '2FA功能总开关:0关闭,1开启'),
('mfa.verify_expire_minutes',  '30','system', '2FA验证有效期(分钟)'),
-- 通知服务开关
('notify.sms.enabled',   '0', 'system', '短信功能开关'),
('notify.email.enabled', '0', 'system', '邮件功能开关');

-- 8. 初始化 OAuth 配置（三个平台，默认禁用）
INSERT IGNORE INTO `sys_oauth_config` (`platform`, `enabled`) VALUES
('WEWORK',   0),
('DINGTALK', 0),
('FEISHU',   0);

-- 9. 初始化 验证码配置（SMS 和 EMAIL，默认禁用）
INSERT IGNORE INTO `sys_captcha_config` (`type`, `enabled`, `code_length`, `expire_minutes`, `daily_limit`) VALUES
('SMS',   0, 6, 5, 10),
('EMAIL', 0, 6, 5, 10);

-- 10. 初始化 2FA 策略示例数据
INSERT IGNORE INTO `sys_mfa_policy` (`name`, `perm_pattern`, `api_pattern`, `enabled`) VALUES
('修改密码', 'system:user:password', '/system/user/*/password', 1),
('删除用户', 'system:user:delete',   '/system/user/*', 1);
```

**Step 2: 验证文件存在**

```bash
ls -la /Users/shichenyang/IdeaProjects/aiperm/backend/src/main/resources/db/migration/V4.0.0__multi_login_2fa_tables.sql
```

期望输出：文件存在，大小 > 3KB

**Step 3: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add backend/src/main/resources/db/migration/V4.0.0__multi_login_2fa_tables.sql
git commit -m "db: add multi-login and 2FA tables migration V4.0.0"
```

---

## Task 3: 验证码枚举和接口定义

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/captcha/enums/CaptchaScene.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/captcha/service/CaptchaService.java`

**Step 1: 创建 CaptchaScene 枚举**

```java
package com.devlovecode.aiperm.modules.captcha.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CaptchaScene {
    LOGIN("LOGIN", "登录"),
    BIND("BIND", "绑定"),
    RESET("RESET", "重置密码");

    private final String code;
    private final String desc;
}
```

**Step 2: 创建 CaptchaService 接口**

```java
package com.devlovecode.aiperm.modules.captcha.service;

import com.devlovecode.aiperm.modules.captcha.enums.CaptchaScene;

public interface CaptchaService {
    /** 发送验证码（包含限流检查） */
    void send(String target, CaptchaScene scene, String ip);

    /** 验证验证码（验证成功后删除） */
    boolean verify(String target, String code, CaptchaScene scene);
}
```

**Step 3: 编译检查**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm/backend && ./gradlew compileJava 2>&1 | tail -5
```

期望输出：`BUILD SUCCESSFUL`

**Step 4: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add backend/src/main/java/com/devlovecode/aiperm/modules/captcha/
git commit -m "feat(captcha): add CaptchaScene enum and CaptchaService interface"
```

---

## Task 4: 验证码配置实体和 Repository

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/captcha/entity/SysCaptchaConfig.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/captcha/entity/SysCaptchaLog.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/captcha/repository/CaptchaConfigRepository.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/captcha/repository/CaptchaLogRepository.java`

**Step 1: 创建 SysCaptchaConfig 实体**

```java
package com.devlovecode.aiperm.modules.captcha.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysCaptchaConfig extends BaseEntity {
    private String type;              // SMS/EMAIL
    private Integer enabled;          // 是否启用
    private String smsProvider;       // 短信服务商
    private String smsAccessKey;
    private String smsSecretKey;
    private String smsSignName;
    private String smsTemplateCode;
    private String emailHost;
    private Integer emailPort;
    private String emailUsername;
    private String emailPassword;
    private String emailFrom;
    private String emailFromName;
    private Integer codeLength;       // 验证码长度
    private Integer expireMinutes;    // 过期时间
    private Integer dailyLimit;       // 每日上限
}
```

**Step 2: 创建 SysCaptchaLog 实体**

```java
package com.devlovecode.aiperm.modules.captcha.entity;

import lombok.Data;
import java.time.LocalDateTime;

// 注意：此表无软删除，不继承 BaseEntity
@Data
public class SysCaptchaLog {
    private Long id;
    private String type;
    private String target;
    private String code;
    private String scene;
    private Integer status;        // 1成功,0失败
    private String failReason;
    private String ip;
    private LocalDateTime createTime;
}
```

**Step 3: 创建 CaptchaConfigRepository**

```java
package com.devlovecode.aiperm.modules.captcha.repository;

import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.modules.captcha.entity.SysCaptchaConfig;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CaptchaConfigRepository extends BaseRepository<SysCaptchaConfig> {

    public CaptchaConfigRepository(JdbcClient db) {
        super(db, "sys_captcha_config", SysCaptchaConfig.class);
    }

    public Optional<SysCaptchaConfig> findByType(String type) {
        String sql = "SELECT * FROM sys_captcha_config WHERE type = :type AND deleted = 0";
        return db.sql(sql).param("type", type).query(SysCaptchaConfig.class).optional();
    }

    public int update(SysCaptchaConfig config) {
        String sql = """
            UPDATE sys_captcha_config
            SET enabled = :enabled,
                sms_provider = :smsProvider, sms_access_key = :smsAccessKey,
                sms_secret_key = :smsSecretKey, sms_sign_name = :smsSignName,
                sms_template_code = :smsTemplateCode,
                email_host = :emailHost, email_port = :emailPort,
                email_username = :emailUsername, email_password = :emailPassword,
                email_from = :emailFrom, email_from_name = :emailFromName,
                code_length = :codeLength, expire_minutes = :expireMinutes,
                daily_limit = :dailyLimit,
                update_time = NOW(), update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("enabled", config.getEnabled())
                .param("smsProvider", config.getSmsProvider())
                .param("smsAccessKey", config.getSmsAccessKey())
                .param("smsSecretKey", config.getSmsSecretKey())
                .param("smsSignName", config.getSmsSignName())
                .param("smsTemplateCode", config.getSmsTemplateCode())
                .param("emailHost", config.getEmailHost())
                .param("emailPort", config.getEmailPort())
                .param("emailUsername", config.getEmailUsername())
                .param("emailPassword", config.getEmailPassword())
                .param("emailFrom", config.getEmailFrom())
                .param("emailFromName", config.getEmailFromName())
                .param("codeLength", config.getCodeLength())
                .param("expireMinutes", config.getExpireMinutes())
                .param("dailyLimit", config.getDailyLimit())
                .param("updateBy", config.getUpdateBy())
                .param("id", config.getId())
                .update();
    }
}
```

**Step 4: 创建 CaptchaLogRepository**

```java
package com.devlovecode.aiperm.modules.captcha.repository;

import com.devlovecode.aiperm.modules.captcha.entity.SysCaptchaLog;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class CaptchaLogRepository {

    private final JdbcClient db;

    public void insert(SysCaptchaLog log) {
        String sql = """
            INSERT INTO sys_captcha_log (type, target, code, scene, status, fail_reason, ip, create_time)
            VALUES (:type, :target, :code, :scene, :status, :failReason, :ip, NOW())
            """;
        db.sql(sql)
                .param("type", log.getType())
                .param("target", log.getTarget())
                .param("code", log.getCode())
                .param("scene", log.getScene())
                .param("status", log.getStatus())
                .param("failReason", log.getFailReason())
                .param("ip", log.getIp())
                .update();
    }

    /** 查询今日发送次数 */
    public int countTodayByTarget(String target) {
        String sql = """
            SELECT COUNT(*) FROM sys_captcha_log
            WHERE target = :target AND status = 1
              AND create_time >= :startOfDay
            """;
        Integer count = db.sql(sql)
                .param("target", target)
                .param("startOfDay", LocalDate.now().atStartOfDay())
                .query(Integer.class).single();
        return count != null ? count : 0;
    }

    /** 查询最近一次发送时间（限流用） */
    public LocalDateTime findLastSendTime(String target) {
        String sql = """
            SELECT MAX(create_time) FROM sys_captcha_log
            WHERE target = :target AND status = 1
            """;
        return db.sql(sql).param("target", target)
                .query(LocalDateTime.class).optional().orElse(null);
    }
}
```

**Step 5: 编译验证**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm/backend && ./gradlew compileJava 2>&1 | tail -5
```

期望输出：`BUILD SUCCESSFUL`

**Step 6: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add backend/src/main/java/com/devlovecode/aiperm/modules/captcha/
git commit -m "feat(captcha): add CaptchaConfig and CaptchaLog entity + repository"
```

---

## Task 5: 短信验证码服务（SmsCaptchaService）

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/captcha/service/SmsCaptchaService.java`

**Step 1: 创建 SmsCaptchaService**

> 说明：sms4j 3.x 版本通过 `SmsBlend` 接口发送，需要动态配置。
> 简化策略：先从数据库读取配置，再动态构建 sms4j 的 SmsBlend。

```java
package com.devlovecode.aiperm.modules.captcha.service;

import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.captcha.entity.SysCaptchaConfig;
import com.devlovecode.aiperm.modules.captcha.entity.SysCaptchaLog;
import com.devlovecode.aiperm.modules.captcha.enums.CaptchaScene;
import com.devlovecode.aiperm.modules.captcha.repository.CaptchaConfigRepository;
import com.devlovecode.aiperm.modules.captcha.repository.CaptchaLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.aliyun.config.AlibabaConfig;
import org.dromara.sms4j.aliyun.service.AlibabaSmsImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("smsCaptchaService")
@RequiredArgsConstructor
public class SmsCaptchaService implements CaptchaService {

    private final CaptchaConfigRepository captchaConfigRepo;
    private final CaptchaLogRepository captchaLogRepo;
    private final StringRedisTemplate redisTemplate;

    private static final String CAPTCHA_KEY_PREFIX = "captcha:sms:";
    private static final String RATE_LIMIT_PREFIX = "captcha:rate:sms:";
    private static final int RATE_LIMIT_SECONDS = 60; // 60秒内只能发1次

    @Override
    public void send(String target, CaptchaScene scene, String ip) {
        // 1. 获取短信配置
        SysCaptchaConfig config = captchaConfigRepo.findByType("SMS")
                .orElseThrow(() -> new BusinessException("短信服务未配置"));
        if (config.getEnabled() == null || config.getEnabled() != 1) {
            throw new BusinessException("短信服务未启用");
        }

        // 2. 限流：60秒内只能发1次
        String rateLimitKey = RATE_LIMIT_PREFIX + target;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey))) {
            throw new BusinessException("发送太频繁，请60秒后重试");
        }

        // 3. 每日上限检查
        int dailyLimit = config.getDailyLimit() != null ? config.getDailyLimit() : 10;
        if (captchaLogRepo.countTodayByTarget(target) >= dailyLimit) {
            throw new BusinessException("今日发送次数已达上限");
        }

        // 4. 生成验证码
        int length = config.getCodeLength() != null ? config.getCodeLength() : 6;
        String code = generateCode(length);

        // 5. 发送短信
        SysCaptchaLog logRecord = new SysCaptchaLog();
        logRecord.setType("SMS");
        logRecord.setTarget(target);
        logRecord.setCode(code);
        logRecord.setScene(scene.getCode());
        logRecord.setIp(ip);

        try {
            doSendSms(config, target, code);
            logRecord.setStatus(1);
        } catch (Exception e) {
            log.error("短信发送失败，target={}", target, e);
            logRecord.setStatus(0);
            logRecord.setFailReason(e.getMessage());
            captchaLogRepo.insert(logRecord);
            throw new BusinessException("短信发送失败：" + e.getMessage());
        }
        captchaLogRepo.insert(logRecord);

        // 6. 存入 Redis，设置过期时间
        int expireMinutes = config.getExpireMinutes() != null ? config.getExpireMinutes() : 5;
        String cacheKey = CAPTCHA_KEY_PREFIX + target + ":" + scene.getCode();
        redisTemplate.opsForValue().set(cacheKey, code, expireMinutes, TimeUnit.MINUTES);

        // 7. 设置限流 Key
        redisTemplate.opsForValue().set(rateLimitKey, "1", RATE_LIMIT_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public boolean verify(String target, String code, CaptchaScene scene) {
        String cacheKey = CAPTCHA_KEY_PREFIX + target + ":" + scene.getCode();
        String storedCode = redisTemplate.opsForValue().get(cacheKey);
        if (storedCode == null) {
            return false;
        }
        if (storedCode.equals(code)) {
            redisTemplate.delete(cacheKey); // 验证成功，删除验证码
            return true;
        }
        return false;
    }

    private void doSendSms(SysCaptchaConfig config, String phone, String code) {
        // 目前仅支持阿里云，后续扩展其他服务商
        if (!"ALIYUN".equalsIgnoreCase(config.getSmsProvider())) {
            throw new BusinessException("暂不支持的短信服务商：" + config.getSmsProvider());
        }
        AlibabaConfig alibabaConfig = new AlibabaConfig();
        alibabaConfig.setAccessKeyId(config.getSmsAccessKey());
        alibabaConfig.setAccessKeySecret(config.getSmsSecretKey());
        alibabaConfig.setSignature(config.getSmsSignName());
        alibabaConfig.setTemplateId(config.getSmsTemplateCode());

        AlibabaSmsImpl smsService = new AlibabaSmsImpl(alibabaConfig);
        Map<String, String> params = new LinkedHashMap<>();
        params.put("code", code);
        smsService.sendMessage(phone, params);
    }

    private String generateCode(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
```

**Step 2: 编译验证**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm/backend && ./gradlew compileJava 2>&1 | tail -10
```

期望输出：`BUILD SUCCESSFUL`（如果 sms4j 有 API 变化，根据错误调整 API 调用方式）

**Step 3: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add backend/src/main/java/com/devlovecode/aiperm/modules/captcha/service/SmsCaptchaService.java
git commit -m "feat(captcha): implement SmsCaptchaService with rate limiting"
```

---

## Task 6: 邮件验证码服务（EmailCaptchaService）

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/captcha/service/EmailCaptchaService.java`

**Step 1: 创建 EmailCaptchaService**

```java
package com.devlovecode.aiperm.modules.captcha.service;

import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.captcha.entity.SysCaptchaConfig;
import com.devlovecode.aiperm.modules.captcha.entity.SysCaptchaLog;
import com.devlovecode.aiperm.modules.captcha.enums.CaptchaScene;
import com.devlovecode.aiperm.modules.captcha.repository.CaptchaConfigRepository;
import com.devlovecode.aiperm.modules.captcha.repository.CaptchaLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("emailCaptchaService")
@RequiredArgsConstructor
public class EmailCaptchaService implements CaptchaService {

    private final CaptchaConfigRepository captchaConfigRepo;
    private final CaptchaLogRepository captchaLogRepo;
    private final StringRedisTemplate redisTemplate;

    private static final String CAPTCHA_KEY_PREFIX = "captcha:email:";
    private static final String RATE_LIMIT_PREFIX = "captcha:rate:email:";
    private static final int RATE_LIMIT_SECONDS = 60;

    @Override
    public void send(String target, CaptchaScene scene, String ip) {
        SysCaptchaConfig config = captchaConfigRepo.findByType("EMAIL")
                .orElseThrow(() -> new BusinessException("邮件服务未配置"));
        if (config.getEnabled() == null || config.getEnabled() != 1) {
            throw new BusinessException("邮件服务未启用");
        }

        String rateLimitKey = RATE_LIMIT_PREFIX + target;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey))) {
            throw new BusinessException("发送太频繁，请60秒后重试");
        }

        int dailyLimit = config.getDailyLimit() != null ? config.getDailyLimit() : 10;
        if (captchaLogRepo.countTodayByTarget(target) >= dailyLimit) {
            throw new BusinessException("今日发送次数已达上限");
        }

        int length = config.getCodeLength() != null ? config.getCodeLength() : 6;
        String code = generateCode(length);

        SysCaptchaLog logRecord = new SysCaptchaLog();
        logRecord.setType("EMAIL");
        logRecord.setTarget(target);
        logRecord.setCode(code);
        logRecord.setScene(scene.getCode());
        logRecord.setIp(ip);

        try {
            doSendEmail(config, target, code, scene);
            logRecord.setStatus(1);
        } catch (Exception e) {
            log.error("邮件发送失败，target={}", target, e);
            logRecord.setStatus(0);
            logRecord.setFailReason(e.getMessage());
            captchaLogRepo.insert(logRecord);
            throw new BusinessException("邮件发送失败：" + e.getMessage());
        }
        captchaLogRepo.insert(logRecord);

        int expireMinutes = config.getExpireMinutes() != null ? config.getExpireMinutes() : 5;
        String cacheKey = CAPTCHA_KEY_PREFIX + target + ":" + scene.getCode();
        redisTemplate.opsForValue().set(cacheKey, code, expireMinutes, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(rateLimitKey, "1", RATE_LIMIT_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public boolean verify(String target, String code, CaptchaScene scene) {
        String cacheKey = CAPTCHA_KEY_PREFIX + target + ":" + scene.getCode();
        String storedCode = redisTemplate.opsForValue().get(cacheKey);
        if (storedCode == null) {
            return false;
        }
        if (storedCode.equals(code)) {
            redisTemplate.delete(cacheKey);
            return true;
        }
        return false;
    }

    private void doSendEmail(SysCaptchaConfig config, String to, String code, CaptchaScene scene) throws Exception {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.getEmailHost());
        sender.setPort(config.getEmailPort() != null ? config.getEmailPort() : 465);
        sender.setUsername(config.getEmailUsername());
        sender.setPassword(config.getEmailPassword());
        sender.setDefaultEncoding("UTF-8");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.timeout", "5000");
        sender.setJavaMailProperties(props);

        var message = sender.createMimeMessage();
        var helper = new MimeMessageHelper(message, false, "UTF-8");
        String fromName = config.getEmailFromName() != null ? config.getEmailFromName() : "AIPerm";
        helper.setFrom(config.getEmailFrom(), fromName);
        helper.setTo(to);
        helper.setSubject("【" + fromName + "】验证码");
        helper.setText(buildEmailContent(code, scene, config.getExpireMinutes()), true);

        sender.send(message);
    }

    private String buildEmailContent(String code, CaptchaScene scene, Integer expireMinutes) {
        int expire = expireMinutes != null ? expireMinutes : 5;
        return "<div style='font-family:sans-serif;padding:20px'>"
                + "<h3>您的验证码</h3>"
                + "<p>验证场景：" + scene.getDesc() + "</p>"
                + "<p style='font-size:32px;font-weight:bold;color:#2563eb;letter-spacing:8px'>" + code + "</p>"
                + "<p>验证码有效期 " + expire + " 分钟，请勿泄露给他人。</p>"
                + "</div>";
    }

    private String generateCode(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
```

**Step 2: 编译验证**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm/backend && ./gradlew compileJava 2>&1 | tail -5
```

期望输出：`BUILD SUCCESSFUL`

**Step 3: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add backend/src/main/java/com/devlovecode/aiperm/modules/captcha/service/EmailCaptchaService.java
git commit -m "feat(captcha): implement EmailCaptchaService with dynamic SMTP config"
```

---

## Task 7: 验证码发送 DTO 和 Controller

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/captcha/dto/SendCaptchaDTO.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/captcha/controller/CaptchaController.java`

**Step 1: 创建 SendCaptchaDTO**

```java
package com.devlovecode.aiperm.modules.captcha.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "发送验证码请求")
public class SendCaptchaDTO {

    @Schema(description = "接收目标（手机号或邮箱）", example = "13800138000")
    @NotBlank(message = "接收目标不能为空")
    private String target;

    @Schema(description = "验证码类型：SMS/EMAIL")
    @NotBlank(message = "验证码类型不能为空")
    private String type;

    @Schema(description = "使用场景：LOGIN/BIND/RESET", example = "LOGIN")
    @NotBlank(message = "使用场景不能为空")
    private String scene;
}
```

**Step 2: 创建 CaptchaController**

```java
package com.devlovecode.aiperm.modules.captcha.controller;

import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.modules.captcha.dto.SendCaptchaDTO;
import com.devlovecode.aiperm.modules.captcha.enums.CaptchaScene;
import com.devlovecode.aiperm.modules.captcha.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "验证码管理")
@RestController
@RequestMapping("/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    // 按 type 名称注入：smsCaptchaService / emailCaptchaService
    private final Map<String, CaptchaService> captchaServices;

    @Operation(summary = "发送验证码（短信或邮件）")
    @PostMapping("/send")
    public R<Void> send(@RequestBody @Valid SendCaptchaDTO dto, HttpServletRequest request) {
        CaptchaService service = getService(dto.getType());
        CaptchaScene scene = CaptchaScene.valueOf(dto.getScene().toUpperCase());
        service.send(dto.getTarget(), scene, getClientIp(request));
        return R.ok();
    }

    private CaptchaService getService(String type) {
        String beanName = type.toLowerCase() + "CaptchaService";
        CaptchaService service = captchaServices.get(beanName);
        if (service == null) {
            throw new com.devlovecode.aiperm.common.exception.BusinessException("不支持的验证码类型：" + type);
        }
        return service;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
```

**Step 3: 编译验证**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm/backend && ./gradlew compileJava 2>&1 | tail -5
```

期望输出：`BUILD SUCCESSFUL`

**Step 4: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add backend/src/main/java/com/devlovecode/aiperm/modules/captcha/
git commit -m "feat(captcha): add CaptchaController with SMS/Email send endpoint"
```

---

## Task 8: 前端验证码 API（captcha.ts）

**Files:**
- Create: `frontend/src/api/captcha.ts`

**Step 1: 创建 captcha.ts**

```typescript
/**
 * 验证码模块 API
 * 对应后端 CaptchaController (/captcha)
 */
import request from '@/utils/request'

// ==================== 类型定义 ====================

export type CaptchaType = 'SMS' | 'EMAIL'
export type CaptchaScene = 'LOGIN' | 'BIND' | 'RESET'

/** 发送验证码请求 */
export interface SendCaptchaDTO {
  target: string       // 手机号或邮箱
  type: CaptchaType
  scene: CaptchaScene
}

// ==================== API 函数 ====================

export const captchaApi = {
  /** 发送验证码（短信或邮件） */
  send: (data: SendCaptchaDTO) =>
    request.post<void>('/captcha/send', data),
}
```

**Step 2: 验证文件存在**

```bash
ls -la /Users/shichenyang/IdeaProjects/aiperm/frontend/src/api/captcha.ts
```

**Step 3: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend/src/api/captcha.ts
git commit -m "feat(captcha): add frontend captcha API file"
```

---

## Phase 1-2 完成验收

验证所有后端代码编译通过：

```bash
cd /Users/shichenyang/IdeaProjects/aiperm/backend && ./gradlew build -x test 2>&1 | tail -10
```

期望输出：`BUILD SUCCESSFUL`

验证新建的前端文件：

```bash
ls /Users/shichenyang/IdeaProjects/aiperm/frontend/src/api/captcha.ts
ls /Users/shichenyang/IdeaProjects/aiperm/backend/src/main/java/com/devlovecode/aiperm/modules/captcha/
```
