ALTER TABLE sys_dict_data
    ADD COLUMN list_class VARCHAR(50) DEFAULT '' COMMENT '样式属性（tag类型或十六进制颜色）';
