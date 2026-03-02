-- V4.2.0 添加 Agent 智能助手菜单
-- menu_type: 1=目录, 2=菜单, 3=按钮

-- 智能助手目录
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (30, '智能助手', 0, 1, 6, '/agent', NULL, NULL, 'ChatDotRound', 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- LLM 提供商管理
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (31, 'LLM提供商', 30, 2, 1, 'provider', 'agent/ProviderManage', 'agent:provider:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- LLM 提供商按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (311, '新增提供商', 31, 3, 1, NULL, NULL, 'agent:provider:create', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (312, '编辑提供商', 31, 3, 2, NULL, NULL, 'agent:provider:update', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (313, '删除提供商', 31, 3, 3, NULL, NULL, 'agent:provider:delete', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);
