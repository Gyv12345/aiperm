-- 菜单初始化数据
-- 仪表板
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (1, '仪表板', 0, '1', 1, '/dashboard', NULL, NULL, 'Odometer', 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (2, '首页', 1, '2', 1, 'index', 'dashboard/index', NULL, NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 系统管理
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (10, '系统管理', 0, '1', 2, '/system', NULL, NULL, 'Setting', 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (11, '用户管理', 10, '2', 1, 'user', 'system/user/index', 'system:user:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (12, '角色管理', 10, '2', 2, 'role', 'system/role/index', 'system:role:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (13, '菜单管理', 10, '2', 3, 'menu', 'system/menu/index', 'system:menu:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (14, '部门管理', 10, '2', 4, 'dept', 'system/dept/index', 'system:dept:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (15, '岗位管理', 10, '2', 5, 'post', 'system/post/index', 'system:post:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (16, '字典管理', 10, '2', 6, 'dict', 'system/dict/index', 'system:dict:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (17, '权限管理', 10, '2', 7, 'permission', 'system/permission/index', 'system:permission:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 企业管理
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (20, '企业管理', 0, '1', 3, '/enterprise', NULL, NULL, 'OfficeBuilding', 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (21, '公告通知', 20, '2', 1, 'notice', 'enterprise/notice/index', 'enterprise:notice:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (22, '消息中心', 20, '2', 2, 'message', 'enterprise/message/index', 'enterprise:message:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (23, '定时任务', 20, '2', 3, 'job', 'enterprise/job/index', 'enterprise:job:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (24, '参数配置', 20, '2', 4, 'config', 'enterprise/config/index', 'enterprise:config:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 为超级管理员角色分配所有菜单（假设角色ID=1是超级管理员）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE deleted = 0;
