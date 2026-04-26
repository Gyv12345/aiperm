-- V4.7.0 完整补齐轻量审批桥接模块

ALTER TABLE `sys_im_config`
    ADD COLUMN IF NOT EXISTS `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER `extra_config`;

ALTER TABLE `sys_approval_scene`
    ADD COLUMN IF NOT EXISTS `business_type` VARCHAR(50) DEFAULT NULL COMMENT '业务类型' AFTER `scene_name`,
    ADD COLUMN IF NOT EXISTS `handler_bean_name` VARCHAR(200) DEFAULT NULL COMMENT '处理器Bean名称' AFTER `handler_class`,
    ADD COLUMN IF NOT EXISTS `auto_submit_enabled` TINYINT DEFAULT 1 COMMENT '是否自动提交审批' AFTER `enabled`,
    ADD COLUMN IF NOT EXISTS `allow_duplicate_pending` TINYINT DEFAULT 0 COMMENT '是否允许重复待审' AFTER `auto_submit_enabled`,
    ADD COLUMN IF NOT EXISTS `notify_template_code` VARCHAR(50) DEFAULT NULL COMMENT '通知模板编码' AFTER `timeout_action`,
    ADD COLUMN IF NOT EXISTS `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER `notify_template_code`;

UPDATE `sys_approval_scene`
SET `business_type` = COALESCE(NULLIF(`business_type`, ''), `scene_code`)
WHERE `business_type` IS NULL OR `business_type` = '';

UPDATE `sys_approval_scene`
SET `handler_bean_name` = COALESCE(NULLIF(`handler_bean_name`, ''), `handler_class`)
WHERE (`handler_bean_name` IS NULL OR `handler_bean_name` = '')
  AND `handler_class` IS NOT NULL
  AND `handler_class` <> '';

ALTER TABLE `sys_approval_instance`
    ADD COLUMN IF NOT EXISTS `initiator_name` VARCHAR(100) DEFAULT NULL COMMENT '发起人名称' AFTER `initiator_id`,
    ADD COLUMN IF NOT EXISTS `error_message` VARCHAR(500) DEFAULT NULL COMMENT '处理错误信息' AFTER `form_data`,
    ADD COLUMN IF NOT EXISTS `callback_raw` LONGTEXT DEFAULT NULL COMMENT '最近回调原文' AFTER `error_message`,
    ADD COLUMN IF NOT EXISTS `last_sync_time` DATETIME DEFAULT NULL COMMENT '最近同步时间' AFTER `result_time`,
    ADD COLUMN IF NOT EXISTS `active_instance_key` VARCHAR(120) DEFAULT NULL COMMENT '进行中实例唯一键' AFTER `last_sync_time`;

ALTER TABLE `sys_approval_instance`
    DROP INDEX `uk_business`;

CREATE UNIQUE INDEX `uk_active_instance_key` ON `sys_approval_instance` (`active_instance_key`);
CREATE INDEX `idx_approval_instance_initiator_id` ON `sys_approval_instance` (`initiator_id`);
CREATE INDEX `idx_approval_instance_scene_status` ON `sys_approval_instance` (`scene_code`, `status`);

UPDATE `sys_approval_instance`
SET `active_instance_key` = CONCAT(`business_type`, ':', `business_id`)
WHERE `status` = 'PENDING'
  AND (`active_instance_key` IS NULL OR `active_instance_key` = '');

CREATE TABLE IF NOT EXISTS `sys_approval_callback_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `platform` VARCHAR(20) NOT NULL COMMENT '平台',
    `scene_code` VARCHAR(50) DEFAULT NULL COMMENT '场景编码',
    `platform_instance_id` VARCHAR(100) DEFAULT NULL COMMENT '平台实例ID',
    `callback_status` VARCHAR(20) DEFAULT NULL COMMENT '回调状态',
    `handle_result` VARCHAR(20) DEFAULT NULL COMMENT '处理结果: SUCCESS/FAILED/IGNORED',
    `error_message` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `payload` LONGTEXT DEFAULT NULL COMMENT '原始回调报文',
    `processed_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `deleted` TINYINT DEFAULT 0,
    `version` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(50) DEFAULT NULL,
    `update_by` VARCHAR(50) DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_approval_callback_platform` (`platform`, `processed_time`),
    INDEX `idx_approval_callback_instance` (`platform_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批回调日志表';
