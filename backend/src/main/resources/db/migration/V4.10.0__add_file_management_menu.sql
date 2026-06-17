-- ============================================================
-- V4.10.0 新增文件管理菜单与按钮权限
-- ------------------------------------------------------------
-- sys_file 表已在 V2.0.0 建好，本脚本仅添加菜单：
--   1. 二级菜单「文件管理」（挂在系统管理 id=10 下）
--   2. 按钮权限：上传 / 删除
--   3. 给超管角色（id=1）授权
-- 幂等：ON DUPLICATE KEY UPDATE。
-- 前提：V4.9.0 已成功执行。
-- ============================================================

-- 1. 文件管理菜单（menu_type='2' 菜单）
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (300, '文件管理', 10, '2', 8, 'file', 'system/file/index', 'system:file:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 2. 按钮权限（menu_type='3' 按钮）
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(301, '文件上传', 300, '3', 1, NULL, NULL, 'system:file:upload', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(302, '文件删除', 300, '3', 2, NULL, NULL, 'system:file:delete', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 3. 给超管角色（id=1）授权文件管理相关权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 300), (1, 301), (1, 302);
