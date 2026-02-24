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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型表';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典数据表';

-- 初始化内置字典
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`) VALUES
('用户性别', 'sys_gender', 1),
('系统开关', 'sys_switch', 1),
('通用状态', 'sys_status', 1);

INSERT INTO `sys_dict_data` (`dict_type`, `dict_label`, `dict_value`, `sort`) VALUES
('sys_gender', '未知', '0', 0), ('sys_gender', '男', '1', 1), ('sys_gender', '女', '2', 2),
('sys_switch', '开启', '1', 0), ('sys_switch', '关闭', '0', 1),
('sys_status', '启用', '1', 0), ('sys_status', '禁用', '0', 1);
