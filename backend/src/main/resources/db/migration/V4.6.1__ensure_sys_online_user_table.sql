-- 补偿迁移：
-- 早期 sys_online_user 定义使用了较低版本号（V1.0.10），对于已经升级到更高版本的库不会再被自动执行。
-- 这里使用高版本迁移确保现有环境也能补齐在线用户表及 BaseEntity 公共字段。

CREATE TABLE IF NOT EXISTS `sys_online_user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `token` VARCHAR(100) NOT NULL COMMENT 'Token',
    `ip` VARCHAR(50) COMMENT '登录IP',
    `browser` VARCHAR(100) COMMENT '浏览器',
    `os` VARCHAR(100) COMMENT '操作系统',
    `login_time` DATETIME NOT NULL COMMENT '登录时间',
    `last_access_time` DATETIME COMMENT '最后访问时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    `version` INT DEFAULT 0 COMMENT '版本号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` VARCHAR(50) COMMENT '创建人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` VARCHAR(50) COMMENT '更新人',
    INDEX `idx_sys_online_user_user_id` (`user_id`),
    INDEX `idx_sys_online_user_token` (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='在线用户表';

ALTER TABLE `sys_online_user`
    COMMENT = '在线用户表';

SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_online_user'
      AND COLUMN_NAME = 'deleted'
);

SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE `sys_online_user` ADD COLUMN `deleted` TINYINT DEFAULT 0 COMMENT ''删除标记'' AFTER `last_access_time`',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_online_user'
      AND COLUMN_NAME = 'version'
);

SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE `sys_online_user` ADD COLUMN `version` INT DEFAULT 0 COMMENT ''版本号'' AFTER `deleted`',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_online_user'
      AND COLUMN_NAME = 'create_time'
);

SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE `sys_online_user` ADD COLUMN `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'' AFTER `version`',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_online_user'
      AND COLUMN_NAME = 'create_by'
);

SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE `sys_online_user` ADD COLUMN `create_by` VARCHAR(50) COMMENT ''创建人'' AFTER `create_time`',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_online_user'
      AND COLUMN_NAME = 'update_time'
);

SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE `sys_online_user` ADD COLUMN `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'' AFTER `create_by`',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_online_user'
      AND COLUMN_NAME = 'update_by'
);

SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE `sys_online_user` ADD COLUMN `update_by` VARCHAR(50) COMMENT ''更新人'' AFTER `update_time`',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
