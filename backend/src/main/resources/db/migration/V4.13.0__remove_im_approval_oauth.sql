-- =====================================================================
-- V4.13.0  移除第三方 IM 对接（企微/钉钉/飞书）与审批桥接模块
-- 配合后端删除 modules/approval 与 modules/auth/oauth 两个模块
-- =====================================================================

-- ① 物理删除 8 张表（按逻辑从属顺序；均无 DB 级外键约束）
--   审批 / IM 推送体系
DROP TABLE IF EXISTS `sys_message_log`;
DROP TABLE IF EXISTS `sys_message_template`;
DROP TABLE IF EXISTS `sys_approval_callback_log`;
DROP TABLE IF EXISTS `sys_approval_instance`;
DROP TABLE IF EXISTS `sys_approval_scene`;
DROP TABLE IF EXISTS `sys_im_config`;
--   第三方 OAuth 登录体系
DROP TABLE IF EXISTS `sys_user_oauth`;
DROP TABLE IF EXISTS `sys_oauth_config`;

-- ② 删除审批 / IM 相关菜单（硬删：与 DROP 表保持一致）
--   ⚠️ 仅按以下 id 删除！id 40/41/42/411/412 已被 V4.8.0 覆写为"监控中心"，
--      绝不可删；禁止用 path / permission 作删除条件
DELETE FROM `sys_menu`
WHERE `id` IN (18,181,182,19,191,192,193,194,25,251,252,253,254,26,261);

-- ③ 清理角色-菜单关联
DELETE FROM `sys_role_menu`
WHERE `menu_id` IN (18,181,182,19,191,192,193,194,25,251,252,253,254,26,261);

-- ④ 删除 sys_config 中相关开关
DELETE FROM `sys_config`
WHERE `config_key` IN (
    'oauth.wework.enabled',
    'oauth.dingtalk.enabled',
    'oauth.feishu.enabled',
    'approval.module.enabled'
);
