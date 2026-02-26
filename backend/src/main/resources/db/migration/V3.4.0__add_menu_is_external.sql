ALTER TABLE sys_menu
    ADD COLUMN is_external TINYINT DEFAULT 0 COMMENT '是否外链：0-否，1-是';
