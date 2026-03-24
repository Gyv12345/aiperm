-- 修复历史数据：JPA 显式插入 null 时会绕过数据库默认值，导致逻辑删除过滤失效
UPDATE `sys_post`
SET `deleted` = 0
WHERE `deleted` IS NULL;

-- 兜底约束：确保后续未显式赋值时数据库层也会落到 0
ALTER TABLE `sys_post`
    MODIFY COLUMN `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除';
