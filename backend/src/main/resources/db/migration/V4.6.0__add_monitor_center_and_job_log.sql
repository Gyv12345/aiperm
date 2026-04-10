-- 监控中心菜单、任务日志表、导入导出权限

CREATE TABLE IF NOT EXISTS sys_job_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    job_id BIGINT NOT NULL COMMENT '任务ID',
    job_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    job_group VARCHAR(50) COMMENT '任务分组',
    bean_class VARCHAR(200) COMMENT '执行类',
    trigger_source VARCHAR(50) COMMENT '触发来源',
    status INT DEFAULT 1 COMMENT '执行状态:1成功 0失败',
    message VARCHAR(500) COMMENT '执行消息',
    exception_info TEXT COMMENT '异常堆栈',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    cost_time BIGINT DEFAULT 0 COMMENT '耗时(ms)',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    version INT DEFAULT 0 COMMENT '版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by VARCHAR(50) COMMENT '创建人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by VARCHAR(50) COMMENT '更新人',
    INDEX idx_sys_job_log_job_id (job_id),
    INDEX idx_sys_job_log_status (status),
    INDEX idx_sys_job_log_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务执行日志表';

-- 监控中心目录
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (40, '监控中心', 0, '1', 4, '/monitor', NULL, NULL, 'Monitor', 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 监控中心菜单
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(41, '在线用户', 40, '2', 1, 'online', 'monitor/online/index', 'monitor:online:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(42, '服务监控', 40, '2', 2, 'server', 'monitor/server/index', 'monitor:server:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(43, '缓存监控', 40, '2', 3, 'cache', 'monitor/cache/index', 'monitor:cache:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(44, '登录日志', 40, '2', 4, 'login-log', 'monitor/login-log/index', 'monitor:login-log:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(45, '操作日志', 40, '2', 5, 'oper-log', 'monitor/oper-log/index', 'log:oper:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(46, '任务日志', 40, '2', 6, 'job-log', 'monitor/job-log/index', 'monitor:job-log:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 在线用户按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(411, '在线用户查询', 41, '3', 1, NULL, NULL, 'monitor:online:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(412, '在线用户导出', 41, '3', 2, NULL, NULL, 'monitor:online:export', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(413, '在线用户强退', 41, '3', 3, NULL, NULL, 'monitor:online:forceLogout', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 服务/缓存监控按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(421, '服务监控查询', 42, '3', 1, NULL, NULL, 'monitor:server:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(431, '缓存监控查询', 43, '3', 1, NULL, NULL, 'monitor:cache:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 登录日志按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(441, '登录日志查询', 44, '3', 1, NULL, NULL, 'monitor:login-log:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(442, '登录日志导出', 44, '3', 2, NULL, NULL, 'monitor:login-log:export', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(443, '登录日志删除', 44, '3', 3, NULL, NULL, 'monitor:login-log:delete', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 操作日志按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(451, '操作日志查询', 45, '3', 1, NULL, NULL, 'log:oper:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(452, '操作日志导出', 45, '3', 2, NULL, NULL, 'log:oper:export', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(453, '操作日志删除', 45, '3', 3, NULL, NULL, 'log:oper:delete', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 任务日志按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(461, '任务日志查询', 46, '3', 1, NULL, NULL, 'monitor:job-log:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(462, '任务日志导出', 46, '3', 2, NULL, NULL, 'monitor:job-log:export', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(463, '任务日志删除', 46, '3', 3, NULL, NULL, 'monitor:job-log:delete', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 用户/字典导入导出按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES
(116, '用户导入', 11, '3', 6, NULL, NULL, 'system:user:import', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(165, '字典导出', 16, '3', 5, NULL, NULL, 'system:dict:export', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system'),
(166, '字典导入', 16, '3', 6, NULL, NULL, 'system:dict:import', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 分配给超级管理员
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id
FROM sys_menu
WHERE id IN (40, 41, 42, 43, 44, 45, 46,
             411, 412, 413, 421, 431,
             441, 442, 443, 451, 452, 453,
             461, 462, 463, 116, 165, 166);
