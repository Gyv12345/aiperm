-- 为用户管理添加按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(111, '用户查询', 11, '3', 1, NULL, NULL, 'system:user:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(112, '用户新增', 11, '3', 2, NULL, NULL, 'system:user:create', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(113, '用户修改', 11, '3', 3, NULL, NULL, 'system:user:update', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(114, '用户删除', 11, '3', 4, NULL, NULL, 'system:user:delete', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(115, '用户导出', 11, '3', 5, NULL, NULL, 'system:user:export', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 为角色管理添加按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(121, '角色查询', 12, '3', 1, NULL, NULL, 'system:role:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(122, '角色新增', 12, '3', 2, NULL, NULL, 'system:role:create', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(123, '角色修改', 12, '3', 3, NULL, NULL, 'system:role:update', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(124, '角色删除', 12, '3', 4, NULL, NULL, 'system:role:delete', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 为菜单管理添加按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(131, '菜单查询', 13, '3', 1, NULL, NULL, 'system:menu:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(132, '菜单新增', 13, '3', 2, NULL, NULL, 'system:menu:create', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(133, '菜单修改', 13, '3', 3, NULL, NULL, 'system:menu:update', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(134, '菜单删除', 13, '3', 4, NULL, NULL, 'system:menu:delete', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 为部门管理添加按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(141, '部门查询', 14, '3', 1, NULL, NULL, 'system:dept:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(142, '部门新增', 14, '3', 2, NULL, NULL, 'system:dept:create', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(143, '部门修改', 14, '3', 3, NULL, NULL, 'system:dept:update', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(144, '部门删除', 14, '3', 4, NULL, NULL, 'system:dept:delete', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 为岗位管理添加按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(151, '岗位查询', 15, '3', 1, NULL, NULL, 'system:post:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(152, '岗位新增', 15, '3', 2, NULL, NULL, 'system:post:create', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(153, '岗位修改', 15, '3', 3, NULL, NULL, 'system:post:update', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(154, '岗位删除', 15, '3', 4, NULL, NULL, 'system:post:delete', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 为字典管理添加按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(161, '字典查询', 16, '3', 1, NULL, NULL, 'system:dict:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(162, '字典新增', 16, '3', 2, NULL, NULL, 'system:dict:create', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(163, '字典修改', 16, '3', 3, NULL, NULL, 'system:dict:update', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(164, '字典删除', 16, '3', 4, NULL, NULL, 'system:dict:delete', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 为权限管理添加按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(171, '权限查询', 17, '3', 1, NULL, NULL, 'system:permission:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(172, '权限新增', 17, '3', 2, NULL, NULL, 'system:permission:create', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(173, '权限修改', 17, '3', 3, NULL, NULL, 'system:permission:update', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(174, '权限删除', 17, '3', 4, NULL, NULL, 'system:permission:delete', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 为公告通知添加按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(211, '公告查询', 21, '3', 1, NULL, NULL, 'enterprise:notice:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(212, '公告新增', 21, '3', 2, NULL, NULL, 'enterprise:notice:create', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(213, '公告修改', 21, '3', 3, NULL, NULL, 'enterprise:notice:update', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(214, '公告删除', 21, '3', 4, NULL, NULL, 'enterprise:notice:delete', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 为消息中心添加按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(221, '消息查询', 22, '3', 1, NULL, NULL, 'enterprise:message:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(222, '消息发送', 22, '3', 2, NULL, NULL, 'enterprise:message:send', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(223, '消息删除', 22, '3', 3, NULL, NULL, 'enterprise:message:delete', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 为定时任务添加按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(231, '任务查询', 23, '3', 1, NULL, NULL, 'enterprise:job:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(232, '任务新增', 23, '3', 2, NULL, NULL, 'enterprise:job:create', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(233, '任务修改', 23, '3', 3, NULL, NULL, 'enterprise:job:update', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(234, '任务删除', 23, '3', 4, NULL, NULL, 'enterprise:job:delete', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(235, '任务执行', 23, '3', 5, NULL, NULL, 'enterprise:job:execute', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 为参数配置添加按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(241, '参数查询', 24, '3', 1, NULL, NULL, 'enterprise:config:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(242, '参数新增', 24, '3', 2, NULL, NULL, 'enterprise:config:create', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(243, '参数修改', 24, '3', 3, NULL, NULL, 'enterprise:config:update', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(244, '参数删除', 24, '3', 4, NULL, NULL, 'enterprise:config:delete', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 为超级管理员角色分配所有按钮权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE menu_type = '3' AND deleted = 0;
