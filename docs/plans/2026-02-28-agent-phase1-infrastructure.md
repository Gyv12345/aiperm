# AI Agent Phase 1: 基础设施

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 创建 Agent 模块的数据库表、实体类和 Repository

**Architecture:** 遵循项目现有的分层架构，Entity 继承 BaseEntity，Repository 继承 BaseRepository

**Tech Stack:** Spring Boot 3.5 + Spring JdbcClient + MySQL + Flyway

---

## Task 1: 创建 Flyway 迁移脚本

**Files:**
- Create: `backend/src/main/resources/db/migration/V1.1.0__create_agent_tables.sql`

**Step 1: 创建迁移脚本**

```sql
-- V1.1.0__create_agent_tables.sql

-- Agent 会话表
CREATE TABLE IF NOT EXISTS `sys_agent_session` (
    `id`            VARCHAR(64)  NOT NULL COMMENT '会话ID (UUID)',
    `user_id`       BIGINT       NOT NULL COMMENT '用户ID',
    `channel`       VARCHAR(20)  NOT NULL COMMENT '渠道: WEB/WEWORK',
    `status`        INT          DEFAULT 0 COMMENT '状态: 0活跃, 1已过期',
    `last_active`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_last_active` (`last_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent会话表';

-- Agent 对话记录表
CREATE TABLE IF NOT EXISTS `sys_agent_message` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `session_id`    VARCHAR(64)  NOT NULL COMMENT '会话ID',
    `role`          VARCHAR(20)  NOT NULL COMMENT '角色: USER/ASSISTANT/TOOL',
    `content`       TEXT         NOT NULL COMMENT '消息内容',
    `tool_name`     VARCHAR(100) DEFAULT NULL COMMENT '调用的工具名',
    `tool_args`     TEXT         DEFAULT NULL COMMENT '工具参数 (JSON)',
    `need_confirm`  TINYINT      DEFAULT 0 COMMENT '是否需要确认',
    `confirmed`     TINYINT      DEFAULT NULL COMMENT '确认结果: 1同意, 0拒绝',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent对话记录表';

-- LLM 提供商配置表
CREATE TABLE IF NOT EXISTS `sys_llm_provider` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `name`          VARCHAR(50)  NOT NULL COMMENT '提供商名称: deepseek/qwen/openai',
    `display_name`  VARCHAR(100) NOT NULL COMMENT '显示名称',
    `api_key`       VARCHAR(255) NOT NULL COMMENT 'API Key (加密存储)',
    `base_url`      VARCHAR(255) DEFAULT NULL COMMENT 'API 地址',
    `model`         VARCHAR(100) NOT NULL COMMENT '模型名称',
    `is_default`    TINYINT      DEFAULT 0 COMMENT '是否默认: 0否, 1是',
    `status`        INT          DEFAULT 0 COMMENT '状态: 0启用, 1停用',
    `sort`          INT          DEFAULT 0 COMMENT '排序',
    `remark`        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted`       TINYINT      DEFAULT 0,
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `create_by`     VARCHAR(50)  DEFAULT NULL,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`     VARCHAR(50)  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM提供商配置表';

-- Agent 全局配置表
CREATE TABLE IF NOT EXISTS `sys_agent_config` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `config_key`    VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value`  VARCHAR(500) NOT NULL COMMENT '配置值',
    `remark`        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent全局配置表';

-- 初始配置数据
INSERT INTO `sys_agent_config` (`config_key`, `config_value`, `remark`) VALUES
('session_timeout', '30', '会话超时时间(分钟)'),
('max_history', '20', '最大对话历史条数'),
('default_provider_id', '1', '默认LLM提供商ID'),
('semantic_cache_enabled', 'false', '是否启用语义缓存'),
('semantic_cache_ttl', '60', '语义缓存 TTL (分钟)'),
('semantic_cache_threshold', '0.95', '语义相似度阈值');

-- 语义缓存表
CREATE TABLE IF NOT EXISTS `sys_agent_cache` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT       NOT NULL COMMENT '用户ID (0表示全局缓存)',
    `question_hash` VARCHAR(64)  NOT NULL COMMENT '问题哈希',
    `question`      VARCHAR(500) NOT NULL COMMENT '原始问题',
    `answer`        TEXT         NOT NULL COMMENT '缓存答案',
    `embedding`     BLOB         DEFAULT NULL COMMENT '向量',
    `hit_count`     INT          DEFAULT 0 COMMENT '命中次数',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_hash` (`user_id`, `question_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='语义缓存表';
```

**Step 2: 验证 SQL 语法**

检查版本号是否正确（需大于当前已有的迁移脚本版本）。

**Step 3: Commit**

```bash
git add backend/src/main/resources/db/migration/V1.1.0__create_agent_tables.sql
git commit -m "feat(agent): add database migration for agent tables"
```

---

## Task 2: 创建 Entity 类

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/entity/SysAgentSession.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/entity/SysAgentMessage.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/entity/SysLlmProvider.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/entity/SysAgentConfig.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/entity/SysAgentCache.java`

**Step 1: 创建 SysAgentSession**

```java
package com.devlovecode.aiperm.modules.agent.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Agent 会话实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysAgentSession extends BaseEntity {
    private String id;
    private Long userId;
    private String channel;
    private Integer status;
    private LocalDateTime lastActive;
}
```

**Step 2: 创建 SysAgentMessage**

```java
package com.devlovecode.aiperm.modules.agent.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Agent 消息实体
 */
@Data
public class SysAgentMessage {
    private Long id;
    private String sessionId;
    private String role;
    private String content;
    private String toolName;
    private String toolArgs;
    private Boolean needConfirm;
    private Boolean confirmed;
    private LocalDateTime createTime;
}
```

**Step 3: 创建 SysLlmProvider**

```java
package com.devlovecode.aiperm.modules.agent.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * LLM 提供商实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysLlmProvider extends BaseEntity {
    private String name;
    private String displayName;
    private String apiKey;
    private String baseUrl;
    private String model;
    private Boolean isDefault;
    private Integer status;
    private Integer sort;
    private String remark;
}
```

**Step 4: 创建 SysAgentConfig**

```java
package com.devlovecode.aiperm.modules.agent.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Agent 配置实体
 */
@Data
public class SysAgentConfig {
    private Long id;
    private String configKey;
    private String configValue;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

**Step 5: 创建 SysAgentCache**

```java
package com.devlovecode.aiperm.modules.agent.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Agent 语义缓存实体
 */
@Data
public class SysAgentCache {
    private Long id;
    private Long userId;
    private String questionHash;
    private String question;
    private String answer;
    private byte[] embedding;
    private Integer hitCount;
    private LocalDateTime createTime;
}
```

**Step 6: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/entity/
git commit -m "feat(agent): add entity classes for agent module"
```

---

## Task 3: 创建 Repository 类

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/repository/AgentSessionRepository.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/repository/AgentMessageRepository.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/repository/LlmProviderRepository.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/repository/AgentConfigRepository.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/repository/AgentCacheRepository.java`

**Step 1: 创建 AgentSessionRepository**

```java
package com.devlovecode.aiperm.modules.agent.repository;

import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.modules.agent.entity.SysAgentSession;
import org.springframework.jdbc.core.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class AgentSessionRepository extends BaseRepository<SysAgentSession> {

    public AgentSessionRepository(JdbcClient db) {
        super(db, "sys_agent_session", SysAgentSession.class);
    }

    public Optional<SysAgentSession> findById(String sessionId) {
        String sql = "SELECT * FROM sys_agent_session WHERE id = :id";
        return db.sql(sql).param("id", sessionId).query(SysAgentSession.class).optionalResult();
    }

    public List<SysAgentSession> findByUserId(Long userId) {
        String sql = "SELECT * FROM sys_agent_session WHERE user_id = :userId AND status = 0 ORDER BY last_active DESC";
        return db.sql(sql).param("userId", userId).query(SysAgentSession.class).list();
    }

    public void insert(SysAgentSession session) {
        String sql = """
            INSERT INTO sys_agent_session (id, user_id, channel, status, last_active, create_time, update_time)
            VALUES (:id, :userId, :channel, :status, :lastActive, :createTime, :updateTime)
            """;
        db.sql(sql)
            .param("id", session.getId())
            .param("userId", session.getUserId())
            .param("channel", session.getChannel())
            .param("status", session.getStatus())
            .param("lastActive", session.getLastActive())
            .param("createTime", LocalDateTime.now())
            .param("updateTime", LocalDateTime.now())
            .update();
    }

    public void updateLastActive(String sessionId) {
        String sql = "UPDATE sys_agent_session SET last_active = :lastActive, update_time = :updateTime WHERE id = :id";
        db.sql(sql)
            .param("id", sessionId)
            .param("lastActive", LocalDateTime.now())
            .param("updateTime", LocalDateTime.now())
            .update();
    }

    public void expireSession(String sessionId) {
        String sql = "UPDATE sys_agent_session SET status = 1, update_time = :updateTime WHERE id = :id";
        db.sql(sql)
            .param("id", sessionId)
            .param("updateTime", LocalDateTime.now())
            .update();
    }

    public int deleteByUserId(Long userId) {
        String sql = "DELETE FROM sys_agent_session WHERE user_id = :userId";
        return db.sql(sql).param("userId", userId).update();
    }
}
```

**Step 2: 创建 AgentMessageRepository**

```java
package com.devlovecode.aiperm.modules.agent.repository;

import com.devlovecode.aiperm.modules.agent.entity.SysAgentMessage;
import org.springframework.jdbc.core.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AgentMessageRepository {

    private final JdbcClient db;

    public AgentMessageRepository(JdbcClient db) {
        this.db = db;
    }

    public List<SysAgentMessage> findBySessionId(String sessionId) {
        String sql = "SELECT * FROM sys_agent_message WHERE session_id = :sessionId ORDER BY create_time ASC";
        return db.sql(sql).param("sessionId", sessionId).query(SysAgentMessage.class).list();
    }

    public void insert(SysAgentMessage message) {
        String sql = """
            INSERT INTO sys_agent_message (session_id, role, content, tool_name, tool_args, need_confirm, confirmed, create_time)
            VALUES (:sessionId, :role, :content, :toolName, :toolArgs, :needConfirm, :confirmed, :createTime)
            """;
        db.sql(sql)
            .param("sessionId", message.getSessionId())
            .param("role", message.getRole())
            .param("content", message.getContent())
            .param("toolName", message.getToolName())
            .param("toolArgs", message.getToolArgs())
            .param("needConfirm", message.getNeedConfirm() != null && message.getNeedConfirm() ? 1 : 0)
            .param("confirmed", message.getConfirmed() != null ? (message.getConfirmed() ? 1 : 0) : null)
            .param("createTime", LocalDateTime.now())
            .update();
    }

    public void updateConfirmed(Long id, Boolean confirmed) {
        String sql = "UPDATE sys_agent_message SET confirmed = :confirmed WHERE id = :id";
        db.sql(sql)
            .param("id", id)
            .param("confirmed", confirmed ? 1 : 0)
            .update();
    }

    public int deleteBySessionId(String sessionId) {
        String sql = "DELETE FROM sys_agent_message WHERE session_id = :sessionId";
        return db.sql(sql).param("sessionId", sessionId).update();
    }
}
```

**Step 3: 创建 LlmProviderRepository**

```java
package com.devlovecode.aiperm.modules.agent.repository;

import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.modules.agent.entity.SysLlmProvider;
import org.springframework.jdbc.core.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class LlmProviderRepository extends BaseRepository<SysLlmProvider> {

    public LlmProviderRepository(JdbcClient db) {
        super(db, "sys_llm_provider", SysLlmProvider.class);
    }

    public List<SysLlmProvider> findAllEnabled() {
        String sql = "SELECT * FROM sys_llm_provider WHERE status = 0 AND deleted = 0 ORDER BY sort ASC";
        return db.sql(sql).query(SysLlmProvider.class).list();
    }

    public Optional<SysLlmProvider> findDefault() {
        String sql = "SELECT * FROM sys_llm_provider WHERE is_default = 1 AND status = 0 AND deleted = 0 LIMIT 1";
        return db.sql(sql).query(SysLlmProvider.class).optionalResult();
    }

    public Optional<SysLlmProvider> findByName(String name) {
        String sql = "SELECT * FROM sys_llm_provider WHERE name = :name AND deleted = 0";
        return db.sql(sql).param("name", name).query(SysLlmProvider.class).optionalResult();
    }

    public void clearDefault() {
        String sql = "UPDATE sys_llm_provider SET is_default = 0, update_time = :updateTime WHERE deleted = 0";
        db.sql(sql).param("updateTime", LocalDateTime.now()).update();
    }

    public void setDefault(Long id) {
        String sql = "UPDATE sys_llm_provider SET is_default = 1, update_time = :updateTime WHERE id = :id AND deleted = 0";
        db.sql(sql)
            .param("id", id)
            .param("updateTime", LocalDateTime.now())
            .update();
    }
}
```

**Step 4: 创建 AgentConfigRepository**

```java
package com.devlovecode.aiperm.modules.agent.repository;

import com.devlovecode.aiperm.modules.agent.entity.SysAgentConfig;
import org.springframework.jdbc.core.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class AgentConfigRepository {

    private final JdbcClient db;

    public AgentConfigRepository(JdbcClient db) {
        this.db = db;
    }

    public List<SysAgentConfig> findAll() {
        String sql = "SELECT * FROM sys_agent_config ORDER BY id";
        return db.sql(sql).query(SysAgentConfig.class).list();
    }

    public Optional<SysAgentConfig> findByKey(String key) {
        String sql = "SELECT * FROM sys_agent_config WHERE config_key = :key";
        return db.sql(sql).param("key", key).query(SysAgentConfig.class).optionalResult();
    }

    public String getValue(String key, String defaultValue) {
        return findByKey(key)
            .map(SysAgentConfig::getConfigValue)
            .orElse(defaultValue);
    }

    public int getValueAsInt(String key, int defaultValue) {
        return findByKey(key)
            .map(c -> Integer.parseInt(c.getConfigValue()))
            .orElse(defaultValue);
    }

    public boolean getValueAsBoolean(String key, boolean defaultValue) {
        return findByKey(key)
            .map(c -> Boolean.parseBoolean(c.getConfigValue()))
            .orElse(defaultValue);
    }

    public double getValueAsDouble(String key, double defaultValue) {
        return findByKey(key)
            .map(c -> Double.parseDouble(c.getConfigValue()))
            .orElse(defaultValue);
    }

    public void updateValue(String key, String value) {
        String sql = "UPDATE sys_agent_config SET config_value = :value, update_time = :updateTime WHERE config_key = :key";
        db.sql(sql)
            .param("key", key)
            .param("value", value)
            .param("updateTime", LocalDateTime.now())
            .update();
    }
}
```

**Step 5: 创建 AgentCacheRepository**

```java
package com.devlovecode.aiperm.modules.agent.repository;

import com.devlovecode.aiperm.modules.agent.entity.SysAgentCache;
import org.springframework.jdbc.core.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AgentCacheRepository {

    private final JdbcClient db;

    public AgentCacheRepository(JdbcClient db) {
        this.db = db;
    }

    public List<SysAgentCache> findByUserId(Long userId) {
        String sql = "SELECT id, user_id, question_hash, question, answer, hit_count, create_time FROM sys_agent_cache WHERE user_id = :userId";
        return db.sql(sql).param("userId", userId).query(SysAgentCache.class).list();
    }

    public void insert(SysAgentCache cache) {
        String sql = """
            INSERT INTO sys_agent_cache (user_id, question_hash, question, answer, embedding, hit_count, create_time)
            VALUES (:userId, :questionHash, :question, :answer, :embedding, 0, :createTime)
            """;
        db.sql(sql)
            .param("userId", cache.getUserId())
            .param("questionHash", cache.getQuestionHash())
            .param("question", cache.getQuestion())
            .param("answer", cache.getAnswer())
            .param("embedding", cache.getEmbedding())
            .param("createTime", LocalDateTime.now())
            .update();
    }

    public void incrementHitCount(Long id) {
        String sql = "UPDATE sys_agent_cache SET hit_count = hit_count + 1 WHERE id = :id";
        db.sql(sql).param("id", id).update();
    }

    public int deleteStale(int daysUnused) {
        String sql = "DELETE FROM sys_agent_cache WHERE hit_count = 0 AND create_time < DATE_SUB(NOW(), INTERVAL :days DAY)";
        return db.sql(sql).param("days", daysUnused).update();
    }
}
```

**Step 6: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/repository/
git commit -m "feat(agent): add repository classes for agent module"
```

---

## Task 4: 编译验证

**Step 1: 编译后端**

```bash
cd backend && ./gradlew build -x test
```

Expected: BUILD SUCCESSFUL

**Step 2: 修复编译错误（如有）**

如果有任何编译错误，根据错误信息修复。

---

## Completion Checklist

- [ ] Flyway 迁移脚本已创建
- [ ] 5 个 Entity 类已创建
- [ ] 5 个 Repository 类已创建
- [ ] 编译通过

---

## Next Phase

继续执行 Phase 2: 工具层实现
