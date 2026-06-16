-- V4.8.0 修正并补全「监控中心」菜单及按钮权限
--
-- 背景：id 40~46 的「监控中心」菜单记录被审批模块污染——path/component/perms/icon
-- 被改成了审批相关值（如 40.path='/approval'、41.perms='approval:instance:list'），
-- 导致监控菜单指向审批页面，且权限码全是 approval:*，在线用户页报「无权限」。
--
-- 本脚本全字段修正这些记录（ON DUPLICATE KEY UPDATE 覆盖 path/component/perms/icon
-- 等关键字段），确保监控菜单与权限码恢复为正确的 monitor:* 值。
-- 同时补齐按钮权限并分配给超管（role_id=1）。幂等，可重复执行。

-- 监控中心目录（修正 path=/monitor、icon=Monitor）
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (40, '监控中心', 0, '1', 4, '/monitor', NULL, NULL, 'Monitor', 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE
    menu_name = VALUES(menu_name),
    parent_id = VALUES(parent_id),
    menu_type = VALUES(menu_type),
    sort = VALUES(sort),
    path = VALUES(path),
    component = VALUES(component),
    perms = VALUES(perms),
    icon = VALUES(icon),
    visible = VALUES(visible),
    status = VALUES(status);

-- 监控中心菜单（修正 path/component/perms 回 monitor:* ）
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(41, '在线用户', 40, '2', 1, 'online', 'monitor/online/index', 'monitor:online:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(42, '服务监控', 40, '2', 2, 'server', 'monitor/server/index', 'monitor:server:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(43, '缓存监控', 40, '2', 3, 'cache', 'monitor/cache/index', 'monitor:cache:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(44, '登录日志', 40, '2', 4, 'login-log', 'monitor/login-log/index', 'monitor:login-log:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(45, '操作日志', 40, '2', 5, 'oper-log', 'monitor/oper-log/index', 'log:oper:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(46, '任务日志', 40, '2', 6, 'job-log', 'monitor/job-log/index', 'monitor:job-log:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE
    menu_name = VALUES(menu_name),
    parent_id = VALUES(parent_id),
    menu_type = VALUES(menu_type),
    sort = VALUES(sort),
    path = VALUES(path),
    component = VALUES(component),
    perms = VALUES(perms),
    visible = VALUES(visible),
    status = VALUES(status);

-- 在线用户按钮权限（修正 perms 回 monitor:online:*，并修正 parent_id 与软删除）
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(411, '在线用户查询', 41, '3', 1, NULL, NULL, 'monitor:online:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(412, '在线用户导出', 41, '3', 2, NULL, NULL, 'monitor:online:export', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(413, '在线用户强退', 41, '3', 3, NULL, NULL, 'monitor:online:forceLogout', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE
    menu_name = VALUES(menu_name),
    parent_id = VALUES(parent_id),
    menu_type = VALUES(menu_type),
    sort = VALUES(sort),
    perms = VALUES(perms),
    visible = VALUES(visible),
    status = VALUES(status),
    deleted = 0;

-- 服务/缓存监控按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(421, '服务监控查询', 42, '3', 1, NULL, NULL, 'monitor:server:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(431, '缓存监控查询', 43, '3', 1, NULL, NULL, 'monitor:cache:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE
    menu_name = VALUES(menu_name),
    parent_id = VALUES(parent_id),
    perms = VALUES(perms),
    deleted = 0;

-- 登录日志按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(441, '登录日志查询', 44, '3', 1, NULL, NULL, 'monitor:login-log:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(442, '登录日志导出', 44, '3', 2, NULL, NULL, 'monitor:login-log:export', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(443, '登录日志删除', 44, '3', 3, NULL, NULL, 'monitor:login-log:delete', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE
    menu_name = VALUES(menu_name),
    parent_id = VALUES(parent_id),
    perms = VALUES(perms),
    deleted = 0;

-- 操作日志按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(451, '操作日志查询', 45, '3', 1, NULL, NULL, 'log:oper:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(452, '操作日志导出', 45, '3', 2, NULL, NULL, 'log:oper:export', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(453, '操作日志删除', 45, '3', 3, NULL, NULL, 'log:oper:delete', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE
    menu_name = VALUES(menu_name),
    parent_id = VALUES(parent_id),
    perms = VALUES(perms),
    deleted = 0;

-- 任务日志按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(461, '任务日志查询', 46, '3', 1, NULL, NULL, 'monitor:job-log:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(462, '任务日志导出', 46, '3', 2, NULL, NULL, 'monitor:job-log:export', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(463, '任务日志删除', 46, '3', 3, NULL, NULL, 'monitor:job-log:delete', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE
    menu_name = VALUES(menu_name),
    parent_id = VALUES(parent_id),
    perms = VALUES(perms),
    deleted = 0;

-- 分配给超级管理员（role_id = 1）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id
FROM sys_menu
WHERE id IN (40, 41, 42, 43, 44, 45, 46,
             411, 412, 413, 421, 431,
             441, 442, 443, 451, 452, 453,
             461, 462, 463);
