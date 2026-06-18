-- ============================================================
-- V4.11.0 sys_dept.leader 由用户名(username)改为用户ID(user.id)
-- ------------------------------------------------------------
-- 背景：部门负责人 leader 原存 sys_user.username 字符串，
-- 前端改为按用户下拉选择（value=user id）后，后端 SysDept.leader /
-- DeptDTO.leader 字段类型由 String 改为 Long。本脚本同步数据库：
--   1. 先把现有 leader(username) 通过 sys_user.username 关联转成 user.id
--   2. 无法匹配的脏数据（用户已删/拼写错误）置 NULL，避免类型转换失败
--   3. 最后把列类型 VARCHAR(50) 改为 BIGINT
-- 顺序敏感：必须先清掉非数字脏值，再改列类型，否则 ALTER 会报错。
-- ============================================================

-- 1. username → user.id：用 JOIN 把有效用户名替换成用户ID
UPDATE `sys_dept` d
JOIN `sys_user` u ON d.leader = u.username
SET d.leader = u.id
WHERE d.leader IS NOT NULL
  AND d.leader <> ''
  AND u.deleted = 0;

-- 2. 兜底：仍为非纯数字的 leader（匹配不上的脏值、空字符串）置 NULL。
--    先清空字符串，再用 REGEXP 清掉含非数字字符的脏值，
--    确保下一步 ALTER 列类型（VARCHAR→BIGINT）不会因 '' 报 Error 1366。
UPDATE `sys_dept`
SET `leader` = NULL
WHERE `leader` IS NOT NULL
  AND `leader` = '';

UPDATE `sys_dept`
SET `leader` = NULL
WHERE `leader` IS NOT NULL
  AND `leader` <> ''
  AND `leader` REGEXP '[^0-9]';

-- 3. 修改列类型：VARCHAR(50) → BIGINT
ALTER TABLE `sys_dept` MODIFY COLUMN `leader` BIGINT DEFAULT NULL COMMENT '负责人用户ID（关联 sys_user.id）';
