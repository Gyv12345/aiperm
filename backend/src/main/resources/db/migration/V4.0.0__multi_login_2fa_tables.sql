-- V4.0.0: 多方式登录与2FA功能基础表

-- 1. 用户2FA绑定表
CREATE TABLE IF NOT EXISTS `sys_user_mfa` (
    `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`      BIGINT      NOT NULL COMMENT '用户ID',
    `mfa_type`     VARCHAR(20) NOT NULL DEFAULT 'TOTP' COMMENT 'MFA类型:TOTP',
    `secret_key`   VARCHAR(100) NOT NULL COMMENT 'TOTP密钥(Base32编码)',
    `bind_time`    DATETIME    DEFAULT NULL COMMENT '绑定时间',
    `status`       TINYINT     DEFAULT 1 COMMENT '状态:1已启用,0已禁用',
    `deleted`      TINYINT     DEFAULT 0 COMMENT '删除标记',
    `version`      INT         DEFAULT 0 COMMENT '乐观锁版本',
    `create_time`  DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`    VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `update_time`  DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by`    VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户MFA绑定表';

-- 2. 2FA策略配置表
CREATE TABLE IF NOT EXISTS `sys_mfa_policy` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`          VARCHAR(100) NOT NULL COMMENT '策略名称',
    `perm_pattern`  VARCHAR(200) DEFAULT NULL COMMENT '权限标识匹配(支持通配符*)',
    `api_pattern`   VARCHAR(200) DEFAULT NULL COMMENT 'API路径匹配(支持通配符*)',
    `enabled`       TINYINT      DEFAULT 1 COMMENT '是否启用:1是,0否',
    `deleted`       TINYINT      DEFAULT 0,
    `version`       INT          DEFAULT 0,
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `create_by`     VARCHAR(50)  DEFAULT NULL,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`     VARCHAR(50)  DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='2FA策略配置表';

-- 3. 用户第三方账号绑定表
CREATE TABLE IF NOT EXISTS `sys_user_oauth` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`         BIGINT       NOT NULL COMMENT '用户ID',
    `platform`        VARCHAR(20)  NOT NULL COMMENT '平台:WEWORK/DINGTALK/FEISHU',
    `open_id`         VARCHAR(100) NOT NULL COMMENT '第三方平台用户标识',
    `union_id`        VARCHAR(100) DEFAULT NULL COMMENT '企业统一标识(可选)',
    `nickname`        VARCHAR(100) DEFAULT NULL COMMENT '第三方昵称',
    `avatar`          VARCHAR(500) DEFAULT NULL COMMENT '第三方头像',
    `last_login_time` DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态:1正常,0已解绑',
    `deleted`         TINYINT      DEFAULT 0,
    `version`         INT          DEFAULT 0,
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `create_by`       VARCHAR(50)  DEFAULT NULL,
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`       VARCHAR(50)  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_platform_openid` (`platform`, `open_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户第三方账号绑定表';

-- 4. OAuth配置表
CREATE TABLE IF NOT EXISTS `sys_oauth_config` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `platform`     VARCHAR(20)  NOT NULL COMMENT '平台:WEWORK/DINGTALK/FEISHU',
    `enabled`      TINYINT      DEFAULT 0 COMMENT '是否启用:1是,0否',
    `corp_id`      VARCHAR(100) DEFAULT NULL COMMENT '企业ID',
    `agent_id`     VARCHAR(100) DEFAULT NULL COMMENT '应用ID',
    `app_key`      VARCHAR(200) DEFAULT NULL COMMENT 'AppKey',
    `app_secret`   VARCHAR(200) DEFAULT NULL COMMENT 'AppSecret',
    `callback_url` VARCHAR(500) DEFAULT NULL COMMENT '回调地址',
    `remark`       VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted`      TINYINT      DEFAULT 0,
    `version`      INT          DEFAULT 0,
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `create_by`    VARCHAR(50)  DEFAULT NULL,
    `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`    VARCHAR(50)  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_platform` (`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OAuth配置表';

-- 5. 验证码配置表
CREATE TABLE IF NOT EXISTS `sys_captcha_config` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `type`              VARCHAR(20)  NOT NULL COMMENT '类型:SMS/EMAIL',
    `enabled`           TINYINT      DEFAULT 0 COMMENT '是否启用:1是,0否',
    `sms_provider`      VARCHAR(50)  DEFAULT NULL COMMENT '短信服务商:ALIYUN/TENCENT',
    `sms_access_key`    VARCHAR(200) DEFAULT NULL COMMENT 'AccessKey',
    `sms_secret_key`    VARCHAR(200) DEFAULT NULL COMMENT 'SecretKey',
    `sms_sign_name`     VARCHAR(100) DEFAULT NULL COMMENT '签名名称',
    `sms_template_code` VARCHAR(100) DEFAULT NULL COMMENT '模板ID',
    `email_host`        VARCHAR(100) DEFAULT NULL COMMENT 'SMTP服务器',
    `email_port`        INT          DEFAULT NULL COMMENT '端口',
    `email_username`    VARCHAR(100) DEFAULT NULL COMMENT '邮箱账号',
    `email_password`    VARCHAR(200) DEFAULT NULL COMMENT '密码/授权码',
    `email_from`        VARCHAR(100) DEFAULT NULL COMMENT '发件人地址',
    `email_from_name`   VARCHAR(100) DEFAULT NULL COMMENT '发件人名称',
    `code_length`       INT          DEFAULT 6 COMMENT '验证码长度',
    `expire_minutes`    INT          DEFAULT 5 COMMENT '过期时间(分钟)',
    `daily_limit`       INT          DEFAULT 10 COMMENT '每日发送上限',
    `deleted`           TINYINT      DEFAULT 0,
    `version`           INT          DEFAULT 0,
    `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `create_by`         VARCHAR(50)  DEFAULT NULL,
    `update_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`         VARCHAR(50)  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证码配置表';

-- 6. 验证码发送记录表
CREATE TABLE IF NOT EXISTS `sys_captcha_log` (
    `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `type`         VARCHAR(20) NOT NULL COMMENT '类型:SMS/EMAIL',
    `target`       VARCHAR(100) NOT NULL COMMENT '目标(手机号/邮箱)',
    `code`         VARCHAR(10) NOT NULL COMMENT '验证码',
    `scene`        VARCHAR(50) DEFAULT NULL COMMENT '场景:LOGIN/BIND/RESET',
    `status`       TINYINT     DEFAULT NULL COMMENT '状态:1成功,0失败',
    `fail_reason`  VARCHAR(200) DEFAULT NULL COMMENT '失败原因',
    `ip`           VARCHAR(50) DEFAULT NULL COMMENT '请求IP',
    `create_time`  DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_target_time` (`target`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证码发送记录';

-- 7. 系统配置扩展（在 sys_config 中新增配置项）
INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_type`, `remark`)
VALUES
-- 登录方式开关
('login.sms.enabled',   '0', 'system', '短信验证码登录开关:0关闭,1开启'),
('login.email.enabled', '0', 'system', '邮箱验证码登录开关:0关闭,1开启'),
-- OAuth开关
('oauth.wework.enabled',  '0', 'system', '企业微信登录开关:0关闭,1开启'),
('oauth.dingtalk.enabled','0', 'system', '钉钉登录开关:0关闭,1开启'),
('oauth.feishu.enabled',  '0', 'system', '飞书登录开关:0关闭,1开启'),
-- 2FA配置
('mfa.enabled',                '1', 'system', '2FA功能总开关:0关闭,1开启'),
('mfa.verify_expire_minutes',  '30','system', '2FA验证有效期(分钟)'),
-- 通知服务开关
('notify.sms.enabled',   '0', 'system', '短信功能开关'),
('notify.email.enabled', '0', 'system', '邮件功能开关');

-- 8. 初始化 OAuth 配置（三个平台，默认禁用）
INSERT IGNORE INTO `sys_oauth_config` (`platform`, `enabled`) VALUES
('WEWORK',   0),
('DINGTALK', 0),
('FEISHU',   0);

-- 9. 初始化 验证码配置（SMS 和 EMAIL，默认禁用）
INSERT IGNORE INTO `sys_captcha_config` (`type`, `enabled`, `code_length`, `expire_minutes`, `daily_limit`) VALUES
('SMS',   0, 6, 5, 10),
('EMAIL', 0, 6, 5, 10);

-- 10. 初始化 2FA 策略示例数据
INSERT IGNORE INTO `sys_mfa_policy` (`name`, `perm_pattern`, `api_pattern`, `enabled`) VALUES
('修改密码', 'system:user:password', '/system/user/*/password', 1),
('删除用户', 'system:user:delete',   '/system/user/*', 1);
