-- V4.1.0__create_agent_tables.sql

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
    `protocol`      VARCHAR(20)  NOT NULL DEFAULT 'openai' COMMENT '协议: openai/anthropic',
    `api_key`       VARCHAR(255) NOT NULL COMMENT 'API Key (加密存储)',
    `base_url`      VARCHAR(255) DEFAULT NULL COMMENT 'API 地址',
    `model`         VARCHAR(100) NOT NULL COMMENT '模型名称',
    `is_default`    TINYINT      DEFAULT 0 COMMENT '是否默认: 0否, 1是',
    `status`        INT          DEFAULT 0 COMMENT '状态: 0启用, 1停用',
    `sort`          INT          DEFAULT 0 COMMENT '排序',
    `remark`        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted`       TINYINT      DEFAULT 0,
    `version`       INT          DEFAULT 0,
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
    `deleted`       TINYINT      DEFAULT 0,
    `version`       INT          DEFAULT 0,
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `create_by`     VARCHAR(50)  DEFAULT NULL,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`     VARCHAR(50)  DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_user_hash` (`user_id`, `question_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='语义缓存表';
