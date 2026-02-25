-- 公告通知表
CREATE TABLE sys_notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    type INT DEFAULT 1 COMMENT '类型(1通知 2公告)',
    status INT DEFAULT 0 COMMENT '状态(0草稿 1发布)',
    publish_time DATETIME COMMENT '发布时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    version INT DEFAULT 0 COMMENT '版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by VARCHAR(50) COMMENT '创建人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by VARCHAR(50) COMMENT '更新人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告通知表';

-- 消息中心表
CREATE TABLE sys_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    sender_id BIGINT COMMENT '发送人ID',
    receiver_id BIGINT COMMENT '接收人ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    is_read INT DEFAULT 0 COMMENT '是否已读(0未读 1已读)',
    read_time DATETIME COMMENT '阅读时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    version INT DEFAULT 0 COMMENT '版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by VARCHAR(50) COMMENT '创建人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by VARCHAR(50) COMMENT '更新人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息中心表';

-- 添加索引
CREATE INDEX idx_notice_status ON sys_notice(status);
CREATE INDEX idx_notice_type ON sys_notice(type);
CREATE INDEX idx_notice_publish_time ON sys_notice(publish_time);

CREATE INDEX idx_message_receiver ON sys_message(receiver_id);
CREATE INDEX idx_message_sender ON sys_message(sender_id);
CREATE INDEX idx_message_is_read ON sys_message(is_read);
