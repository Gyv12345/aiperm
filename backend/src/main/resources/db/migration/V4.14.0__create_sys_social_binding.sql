-- 第三方账号绑定表（JustAuth 社交登录）
-- 单表方案：记录「系统用户 ↔ 第三方账号」的绑定关系
-- - 一个第三方账号(source+openid)只能绑一个系统用户（uk_source_openid）
-- - 一个系统用户在每个平台最多绑一个账号（uk_user_source）
-- - 不存储 access_token：首期仅需确定用户身份，无需调用第三方 API，避免令牌入库
CREATE TABLE IF NOT EXISTS `sys_social_binding` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       NOT NULL COMMENT '系统用户ID(sys_user.id)',
    `source`      VARCHAR(32)  NOT NULL COMMENT '第三方来源:GITHUB/GITEE',
    `openid`      VARCHAR(128) NOT NULL COMMENT '第三方唯一ID(JustAuth uuid)',
    `nickname`    VARCHAR(100) DEFAULT NULL COMMENT '第三方昵称',
    `avatar`      VARCHAR(500) DEFAULT NULL COMMENT '第三方头像URL',
    `email`       VARCHAR(100) DEFAULT NULL COMMENT '第三方邮箱',
    `deleted`     TINYINT      DEFAULT 0 COMMENT '删除标记',
    `version`     INT          DEFAULT 0 COMMENT '乐观锁版本',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`   VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by`   VARCHAR(50)  DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_source_openid` (`source`, `openid`),
    UNIQUE KEY `uk_user_source` (`user_id`, `source`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方账号绑定';
