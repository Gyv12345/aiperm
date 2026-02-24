-- ===================================================
-- V2.0.0 合并 sys_permission 和 sys_menu
-- ===================================================

-- 1. 给 sys_menu 添加 perms 权限标识字段
ALTER TABLE `sys_menu`
    ADD COLUMN `perms` VARCHAR(100) DEFAULT NULL COMMENT '权限标识（按钮用，如 system:user:add）'
        AFTER `component`;

-- 2. 删除角色-权限关联表（不再使用）
DROP TABLE IF EXISTS `sys_role_permission`;

-- 3. 删除独立权限表（不再使用）
DROP TABLE IF EXISTS `sys_permission`;

-- 4. 新增操作日志表
CREATE TABLE IF NOT EXISTS `sys_oper_log` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志主键',
    `title`          VARCHAR(50)  DEFAULT '' COMMENT '操作模块',
    `oper_type`      TINYINT      DEFAULT 0 COMMENT '操作类型：1-新增 2-修改 3-删除 4-查询',
    `method`         VARCHAR(200) DEFAULT '' COMMENT '方法名',
    `request_method` VARCHAR(10)  DEFAULT '' COMMENT 'HTTP请求方式',
    `oper_url`       VARCHAR(255) DEFAULT '' COMMENT '请求URL',
    `oper_ip`        VARCHAR(128) DEFAULT '' COMMENT '操作IP',
    `oper_param`     TEXT         COMMENT '请求参数',
    `json_result`    TEXT         COMMENT '返回参数',
    `status`         TINYINT      DEFAULT 0 COMMENT '操作状态：0-成功 1-失败',
    `error_msg`      VARCHAR(2000) DEFAULT '' COMMENT '错误消息',
    `cost_time`      BIGINT       DEFAULT 0 COMMENT '消耗时间（ms）',
    `oper_user`      VARCHAR(50)  DEFAULT '' COMMENT '操作人账号',
    `oper_name`      VARCHAR(50)  DEFAULT '' COMMENT '操作人名称',
    `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_oper_type` (`oper_type`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志记录';

-- 5. 新增文件记录表
CREATE TABLE IF NOT EXISTS `sys_file` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    `file_name`     VARCHAR(200)  NOT NULL COMMENT '存储文件名',
    `original_name` VARCHAR(200)  DEFAULT NULL COMMENT '原始文件名',
    `file_path`     VARCHAR(500)  DEFAULT NULL COMMENT '存储路径（本地相对路径）',
    `file_url`      VARCHAR(500)  DEFAULT NULL COMMENT '访问URL',
    `file_size`     BIGINT        DEFAULT NULL COMMENT '文件大小（字节）',
    `file_type`     VARCHAR(100)  DEFAULT NULL COMMENT 'MIME类型',
    `storage_type`  VARCHAR(20)   DEFAULT 'local' COMMENT '存储类型：local/aliyun',
    `deleted`       TINYINT       DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
    `create_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `create_by`     VARCHAR(50)   DEFAULT NULL COMMENT '上传人',
    PRIMARY KEY (`id`),
    KEY `idx_storage_type` (`storage_type`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件记录表';
