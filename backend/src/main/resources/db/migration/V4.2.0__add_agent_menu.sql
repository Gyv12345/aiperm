-- V4.2.0__add_agent_menu.sql

-- 添加 Agent 智能助手父菜单
INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, sort, status, visible)
VALUES (0, '智能助手', '/agent', NULL, NULL, 'M', 6, 0, 1);

-- 获取刚插入的菜单 ID（假设为 AUTO_INCREMENT 后的值）
SET @parent_menu_id = LAST_INSERT_ID();

-- 添加 LLM 提供商管理子菜单
INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, sort, status, visible)
VALUES (@parent_menu_id, 'LLM提供商', 'provider', 'agent/ProviderManage', 'agent:provider:list', 'C', 1, 0, 1);
