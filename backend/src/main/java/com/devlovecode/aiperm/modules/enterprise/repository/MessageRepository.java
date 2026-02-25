package com.devlovecode.aiperm.modules.enterprise.repository;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.common.repository.SqlBuilder;
import com.devlovecode.aiperm.modules.enterprise.entity.SysMessage;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class MessageRepository extends BaseRepository<SysMessage> {

    public MessageRepository(JdbcClient db) {
        super(db, "sys_message", SysMessage.class);
    }

    /**
     * 插入消息
     */
    public void insert(SysMessage entity) {
        String sql = """
            INSERT INTO sys_message (sender_id, receiver_id, title, content, is_read, deleted, version, create_time, create_by)
            VALUES (:senderId, :receiverId, :title, :content, 0, 0, 0, :createTime, :createBy)
            """;
        db.sql(sql)
                .param("senderId", entity.getSenderId())
                .param("receiverId", entity.getReceiverId())
                .param("title", entity.getTitle())
                .param("content", entity.getContent())
                .param("createTime", LocalDateTime.now())
                .param("createBy", entity.getCreateBy())
                .update();
    }

    /**
     * 标记消息为已读
     */
    public int markAsRead(Long id) {
        String sql = """
            UPDATE sys_message
            SET is_read = 1, read_time = :readTime, update_time = :updateTime
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("readTime", LocalDateTime.now())
                .param("updateTime", LocalDateTime.now())
                .param("id", id)
                .update();
    }

    /**
     * 批量标记消息为已读
     */
    public int markAllAsRead(Long receiverId) {
        String sql = """
            UPDATE sys_message
            SET is_read = 1, read_time = :readTime, update_time = :updateTime
            WHERE receiver_id = :receiverId AND is_read = 0 AND deleted = 0
            """;
        return db.sql(sql)
                .param("readTime", LocalDateTime.now())
                .param("updateTime", LocalDateTime.now())
                .param("receiverId", receiverId)
                .update();
    }

    /**
     * 批量标记指定消息为已读
     */
    public int markAsReadByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        String sql = """
            UPDATE sys_message
            SET is_read = 1, read_time = :readTime, update_time = :updateTime
            WHERE id IN (:ids) AND deleted = 0
            """;
        return db.sql(sql)
                .param("readTime", LocalDateTime.now())
                .param("updateTime", LocalDateTime.now())
                .param("ids", ids)
                .update();
    }

    /**
     * 分页查询用户消息
     */
    public PageResult<SysMessage> queryPage(Long receiverId, Integer isRead, int pageNum, int pageSize) {
        SqlBuilder sb = new SqlBuilder();
        sb.whereIf(receiverId != null, "receiver_id = ?", receiverId)
          .whereIf(isRead != null, "is_read = ?", isRead);

        return queryPage(sb.getWhereClause(), sb.getParams(), pageNum, pageSize);
    }

    /**
     * 统计未读消息数量
     */
    public int countUnread(Long receiverId) {
        String sql = "SELECT COUNT(*) FROM sys_message WHERE receiver_id = :receiverId AND is_read = 0 AND deleted = 0";
        Integer count = db.sql(sql).param("receiverId", receiverId).query(Integer.class).single();
        return count != null ? count : 0;
    }

    /**
     * 根据ID查询
     */
    public Optional<SysMessage> findById(Long id) {
        String sql = "SELECT * FROM sys_message WHERE id = :id AND deleted = 0";
        return db.sql(sql).param("id", id).query(SysMessage.class).optional();
    }

    /**
     * 查询用户的所有消息
     */
    public List<SysMessage> findByReceiverId(Long receiverId) {
        String sql = "SELECT * FROM sys_message WHERE receiver_id = :receiverId AND deleted = 0 ORDER BY create_time DESC";
        return db.sql(sql).param("receiverId", receiverId).query(SysMessage.class).list();
    }
}
