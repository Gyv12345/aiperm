-- V4.3.0__add_llm_provider_protocol.sql

SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_llm_provider'
      AND COLUMN_NAME = 'protocol'
);

SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE `sys_llm_provider` ADD COLUMN `protocol` VARCHAR(20) NOT NULL DEFAULT ''openai'' COMMENT ''协议: openai/anthropic'' AFTER `display_name`',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 仅按已知命名模式做保守回填，其他提供商默认走 openai 兼容协议
UPDATE `sys_llm_provider`
SET `protocol` = 'anthropic'
WHERE (`name` LIKE '%anthropic%' OR `name` LIKE '%claude%')
  AND `deleted` = 0;
