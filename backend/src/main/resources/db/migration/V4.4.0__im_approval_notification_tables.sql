-- V4.4.0 企业 IM 审批流与消息推送集成（MVP）

-- 1. IM 平台配置
CREATE TABLE IF NOT EXISTS `sys_im_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `platform` VARCHAR(20) NOT NULL COMMENT 'WEWORK/DINGTALK/FEISHU',
    `enabled` TINYINT DEFAULT 0,
    `app_id` VARCHAR(100) DEFAULT NULL,
    `app_secret` VARCHAR(200) DEFAULT NULL,
    `corp_id` VARCHAR(100) DEFAULT NULL,
    `callback_token` VARCHAR(200) DEFAULT NULL,
    `callback_aes_key` VARCHAR(200) DEFAULT NULL,
    `extra_config` JSON DEFAULT NULL,
    `deleted` TINYINT DEFAULT 0,
    `version` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(50) DEFAULT NULL,
    `update_by` VARCHAR(50) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_platform` (`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IM平台配置表';

-- 2. 审批场景配置
CREATE TABLE IF NOT EXISTS `sys_approval_scene` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `scene_code` VARCHAR(50) NOT NULL,
    `scene_name` VARCHAR(100) NOT NULL,
    `platform` VARCHAR(20) NOT NULL,
    `template_id` VARCHAR(100) DEFAULT NULL,
    `enabled` TINYINT DEFAULT 1,
    `handler_class` VARCHAR(200) DEFAULT NULL,
    `timeout_hours` INT DEFAULT 72,
    `timeout_action` VARCHAR(20) DEFAULT 'NOTIFY',
    `deleted` TINYINT DEFAULT 0,
    `version` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(50) DEFAULT NULL,
    `update_by` VARCHAR(50) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_scene_code` (`scene_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批场景配置表';

-- 3. 审批实例记录
CREATE TABLE IF NOT EXISTS `sys_approval_instance` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `scene_code` VARCHAR(50) NOT NULL,
    `business_type` VARCHAR(50) NOT NULL,
    `business_id` BIGINT NOT NULL,
    `initiator_id` BIGINT NOT NULL,
    `platform` VARCHAR(20) NOT NULL,
    `platform_instance_id` VARCHAR(100) DEFAULT NULL,
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/CANCELED',
    `form_data` JSON DEFAULT NULL,
    `result_time` DATETIME DEFAULT NULL,
    `deleted` TINYINT DEFAULT 0,
    `version` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(50) DEFAULT NULL,
    `update_by` VARCHAR(50) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_business` (`business_type`, `business_id`),
    UNIQUE KEY `uk_platform_instance` (`platform_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批实例记录表';

-- 4. 消息模板
CREATE TABLE IF NOT EXISTS `sys_message_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `template_code` VARCHAR(50) NOT NULL,
    `template_name` VARCHAR(100) NOT NULL,
    `category` VARCHAR(20) DEFAULT NULL COMMENT 'APPROVAL/ALERT/BUSINESS',
    `platform` VARCHAR(20) DEFAULT NULL,
    `title` VARCHAR(200) DEFAULT NULL,
    `content` TEXT,
    `deleted` TINYINT DEFAULT 0,
    `version` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(50) DEFAULT NULL,
    `update_by` VARCHAR(50) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_code` (`template_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息模板表';

-- 5. 消息推送记录
CREATE TABLE IF NOT EXISTS `sys_message_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `template_code` VARCHAR(50) DEFAULT NULL,
    `platform` VARCHAR(20) NOT NULL,
    `receiver_id` BIGINT DEFAULT NULL,
    `platform_user_id` VARCHAR(100) DEFAULT NULL,
    `title` VARCHAR(200) DEFAULT NULL,
    `content` TEXT,
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED',
    `error_msg` TEXT,
    `send_time` DATETIME DEFAULT NULL,
    `deleted` TINYINT DEFAULT 0,
    `version` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(50) DEFAULT NULL,
    `update_by` VARCHAR(50) DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_receiver` (`receiver_id`),
    INDEX `idx_template_code` (`template_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息推送记录表';

-- 初始化 IM 平台配置
INSERT IGNORE INTO `sys_im_config` (`platform`, `enabled`)
VALUES
('WEWORK', 0),
('DINGTALK', 0),
('FEISHU', 0);

-- 初始化基础模板
INSERT IGNORE INTO `sys_message_template` (`template_code`, `template_name`, `category`, `platform`, `title`, `content`, `create_by`)
VALUES
('APPROVAL_SUBMIT', '审批发起通知', 'APPROVAL', NULL, '审批待处理通知', CONCAT('您有新的审批待处理：', CHAR(36), '{businessType}#', CHAR(36), '{businessId}'), 'system'),
('APPROVAL_PASSED', '审批通过通知', 'APPROVAL', NULL, '审批通过通知', CONCAT('您的审批已通过：', CHAR(36), '{businessType}#', CHAR(36), '{businessId}'), 'system'),
('APPROVAL_REJECTED', '审批拒绝通知', 'APPROVAL', NULL, '审批拒绝通知', CONCAT('您的审批已拒绝：', CHAR(36), '{businessType}#', CHAR(36), '{businessId}'), 'system');

-- 菜单：系统管理
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(18, 'IM平台配置', 10, 2, 8, 'im-config', 'system/im-config/index', 'system:im-config:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(19, '审批场景管理', 10, 2, 9, 'approval-scene', 'system/approval-scene/index', 'system:approval-scene:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(181, 'IM配置查询', 18, 3, 1, NULL, NULL, 'system:im-config:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(182, 'IM配置修改', 18, 3, 2, NULL, NULL, 'system:im-config:update', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(191, '审批场景查询', 19, 3, 1, NULL, NULL, 'system:approval-scene:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(192, '审批场景新增', 19, 3, 2, NULL, NULL, 'system:approval-scene:create', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(193, '审批场景修改', 19, 3, 3, NULL, NULL, 'system:approval-scene:update', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(194, '审批场景删除', 19, 3, 4, NULL, NULL, 'system:approval-scene:delete', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 菜单：企业管理
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(25, '消息模板', 20, 2, 5, 'message-template', 'enterprise/message-template/index', 'enterprise:message-template:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(26, '消息记录', 20, 2, 6, 'message-log', 'enterprise/message-log/index', 'enterprise:message-log:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(251, '模板查询', 25, 3, 1, NULL, NULL, 'enterprise:message-template:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(252, '模板新增', 25, 3, 2, NULL, NULL, 'enterprise:message-template:create', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(253, '模板修改', 25, 3, 3, NULL, NULL, 'enterprise:message-template:update', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(254, '模板删除', 25, 3, 4, NULL, NULL, 'enterprise:message-template:delete', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(261, '记录查询', 26, 3, 1, NULL, NULL, 'enterprise:message-log:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 菜单：审批中心
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(40, '审批中心', 0, 1, 7, '/approval', NULL, NULL, 'Checked', 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(41, '我的审批', 40, 2, 1, 'my', 'approval/my/index', 'approval:instance:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(42, '待我审批', 40, 2, 2, 'todo', 'approval/todo/index', 'approval:todo:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(411, '审批实例查询', 41, 3, 1, NULL, NULL, 'approval:instance:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(412, '发起审批', 41, 3, 2, NULL, NULL, 'approval:instance:submit', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 为超级管理员分配新增菜单/按钮权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id
FROM sys_menu
WHERE id IN (
    18, 19, 25, 26, 40, 41, 42,
    181, 182, 191, 192, 193, 194, 251, 252, 253, 254, 261, 411, 412
);
