package com.devlovecode.aiperm.modules.notification.repository;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.common.repository.SqlBuilder;
import com.devlovecode.aiperm.modules.notification.entity.SysMessageLog;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class MessageLogRepository extends BaseRepository<SysMessageLog> {

    public MessageLogRepository(JdbcClient db) {
        super(db, "sys_message_log", SysMessageLog.class);
    }

    public Long insertPending(SysMessageLog entity) {
        String sql = """
            INSERT INTO sys_message_log (
                template_code, platform, receiver_id, platform_user_id, title, content,
                status, error_msg, send_time, deleted, version, create_time, create_by
            ) VALUES (
                :templateCode, :platform, :receiverId, :platformUserId, :title, :content,
                'PENDING', NULL, NULL, 0, 0, :createTime, :createBy
            )
            """;
        db.sql(sql)
                .param("templateCode", entity.getTemplateCode())
                .param("platform", entity.getPlatform())
                .param("receiverId", entity.getReceiverId())
                .param("platformUserId", entity.getPlatformUserId())
                .param("title", entity.getTitle())
                .param("content", entity.getContent())
                .param("createTime", LocalDateTime.now())
                .param("createBy", entity.getCreateBy())
                .update();
        return db.sql("SELECT LAST_INSERT_ID()").query(Long.class).single();
    }

    public int markSuccess(Long id) {
        String sql = """
            UPDATE sys_message_log
            SET status = 'SUCCESS', send_time = :sendTime, error_msg = NULL, update_time = :updateTime
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("sendTime", LocalDateTime.now())
                .param("updateTime", LocalDateTime.now())
                .param("id", id)
                .update();
    }

    public int markFailed(Long id, String errorMsg) {
        String sql = """
            UPDATE sys_message_log
            SET status = 'FAILED', error_msg = :errorMsg, update_time = :updateTime
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("errorMsg", errorMsg)
                .param("updateTime", LocalDateTime.now())
                .param("id", id)
                .update();
    }

    public PageResult<SysMessageLog> queryPage(String templateCode, String platform, String status, int page, int pageSize) {
        SqlBuilder sb = new SqlBuilder();
        sb.likeIf(templateCode != null && !templateCode.isBlank(), "template_code", templateCode)
                .whereIf(platform != null && !platform.isBlank(), "platform = ?", platform)
                .whereIf(status != null && !status.isBlank(), "status = ?", status);
        return queryPage(sb.getWhereClause(), sb.getParams(), page, pageSize);
    }
}
