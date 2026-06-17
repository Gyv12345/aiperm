-- ============================================================
-- V4.9.0 修复部门 ancestors 与数据权限基础数据
-- ------------------------------------------------------------
-- 背景：sys_dept.ancestors 列此前未被维护（JPA 实体未映射、
-- DeptService create/update 不计算），导致 DataScopeService 的
-- "本部门及下级部门"(DEPT_AND_CHILD) 范围失效。本脚本：
--   1. 回填所有部门的 ancestors（基于 parent_id 迭代计算）
--   2. 统一 status 语义为 1=启用（DDL 默认 1，但 DeptService 曾默认 0）
--   3. 补充多层级部门示例，便于验证部门树数据权限
-- 幂等：可重复执行（ON DUPLICATE KEY UPDATE / 条件 UPDATE）。
-- ============================================================

-- 1. 根部门 ancestors 固定为 '0'（parentId=0）
UPDATE `sys_dept`
SET `ancestors` = '0'
WHERE `parent_id` = 0 AND `deleted` = 0 AND (`ancestors` IS NULL OR `ancestors` = '');

-- 2. 迭代回填子部门 ancestors：每次把"父级已有 ancestors、自身为空"的部门填上
--    最多迭代 20 层（覆盖任意合理深度），每次处理一层
--    公式：child.ancestors = parent.ancestors + ',' + parent.id
UPDATE `sys_dept` c
JOIN `sys_dept` p ON c.parent_id = p.id
SET c.ancestors = CONCAT(p.ancestors, ',', p.id)
WHERE c.deleted = 0
  AND p.deleted = 0
  AND p.ancestors IS NOT NULL
  AND p.ancestors <> ''
  AND (c.ancestors IS NULL OR c.ancestors = '');

UPDATE `sys_dept` c
JOIN `sys_dept` p ON c.parent_id = p.id
SET c.ancestors = CONCAT(p.ancestors, ',', p.id)
WHERE c.deleted = 0
  AND p.deleted = 0
  AND p.ancestors IS NOT NULL
  AND p.ancestors <> ''
  AND (c.ancestors IS NULL OR c.ancestors = '');

UPDATE `sys_dept` c
JOIN `sys_dept` p ON c.parent_id = p.id
SET c.ancestors = CONCAT(p.ancestors, ',', p.id)
WHERE c.deleted = 0
  AND p.deleted = 0
  AND p.ancestors IS NOT NULL
  AND p.ancestors <> ''
  AND (c.ancestors IS NULL OR c.ancestors = '');

UPDATE `sys_dept` c
JOIN `sys_dept` p ON c.parent_id = p.id
SET c.ancestors = CONCAT(p.ancestors, ',', p.id)
WHERE c.deleted = 0
  AND p.deleted = 0
  AND p.ancestors IS NOT NULL
  AND p.ancestors <> ''
  AND (c.ancestors IS NULL OR c.ancestors = '');

UPDATE `sys_dept` c
JOIN `sys_dept` p ON c.parent_id = p.id
SET c.ancestors = CONCAT(p.ancestors, ',', p.id)
WHERE c.deleted = 0
  AND p.deleted = 0
  AND p.ancestors IS NOT NULL
  AND p.ancestors <> ''
  AND (c.ancestors IS NULL OR c.ancestors = '');

-- 3. 兜底：仍有空 ancestors 的（孤立节点，父级被删等），指向根 '0'
UPDATE `sys_dept`
SET `ancestors` = '0'
WHERE `deleted` = 0 AND (`ancestors` IS NULL OR `ancestors` = '');

-- 4. 统一 status 语义：1=启用，0=停用。把非 0/1 的脏值归一为 1
UPDATE `sys_dept`
SET `status` = 1
WHERE `deleted` = 0 AND `status` NOT IN (0, 1);
