-- 对齐 sys_login_log 与 BaseEntity 公共字段，避免 JPA 查询列缺失

SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_login_log'
      AND COLUMN_NAME = 'update_time'
);

SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE `sys_login_log` ADD COLUMN `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'' AFTER `create_time`',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_login_log'
      AND COLUMN_NAME = 'create_by'
);

SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE `sys_login_log` ADD COLUMN `create_by` VARCHAR(50) COMMENT ''创建人'' AFTER `create_time`',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_login_log'
      AND COLUMN_NAME = 'update_by'
);

SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE `sys_login_log` ADD COLUMN `update_by` VARCHAR(50) COMMENT ''更新人'' AFTER `create_by`',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_login_log'
      AND COLUMN_NAME = 'version'
);

SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE `sys_login_log` ADD COLUMN `version` INT DEFAULT 0 COMMENT ''版本号'' AFTER `deleted`',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
