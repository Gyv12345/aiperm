-- V4.8.1 隐藏后端无对应接口的孤儿菜单
--
-- 背景：以下 4 个菜单在 sys_menu 中存在，但其对应的后端 Controller/表结构
-- 已被早期迁移删除，前端无法实现真实页面，点击会 404：
--   17 权限管理    —— sys_permission 表在 V2.0.0 已 DROP（权限合并进 sys_menu.perms）
--   25 消息模板    —— 无独立 Controller（仅 SysMessageController，无模板/记录接口）
--   26 消息记录    —— 同上，无独立接口
--   31 LLM提供商  —— agent 模块在 V4.5.0 已整体移除
--
-- 处理方式：visible 置 0（隐藏，不删除数据，可逆），侧边栏不再展示死链。
-- 幂等，可重复执行。

UPDATE `sys_menu`
SET `visible` = 0
WHERE `id` IN (17, 25, 26, 31);
