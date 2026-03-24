-- 对齐 sys_login_log 与 BaseEntity 公共字段，避免 JPA 查询列缺失

ALTER TABLE `sys_login_log`
    ADD COLUMN IF NOT EXISTS `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `create_time`,
    ADD COLUMN IF NOT EXISTS `create_by` VARCHAR(50) COMMENT '创建人' AFTER `create_time`,
    ADD COLUMN IF NOT EXISTS `update_by` VARCHAR(50) COMMENT '更新人' AFTER `create_by`,
    ADD COLUMN IF NOT EXISTS `version` INT DEFAULT 0 COMMENT '版本号' AFTER `deleted`;
