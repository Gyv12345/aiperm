-- V4.5.0 移除 Agent 模块相关数据库对象

-- 1) 下线 Agent 菜单与按钮权限
UPDATE sys_role_menu
SET deleted = 1
WHERE menu_id IN (30, 31, 311, 312, 313)
  AND deleted = 0;

UPDATE sys_menu
SET deleted = 1,
    status = 0,
    update_time = NOW(),
    update_by = 'system'
WHERE id IN (30, 31, 311, 312, 313)
  AND deleted = 0;

-- 2) 删除 Agent 相关业务表
DROP TABLE IF EXISTS `sys_agent_cache`;
DROP TABLE IF EXISTS `sys_agent_message`;
DROP TABLE IF EXISTS `sys_agent_session`;
DROP TABLE IF EXISTS `sys_agent_config`;
DROP TABLE IF EXISTS `sys_llm_provider`;
