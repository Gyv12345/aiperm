package com.devlovecode.aiperm.modules.agent.repository;

import com.devlovecode.aiperm.modules.agent.entity.SysAgentMessage;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AgentMessageRepository {

    private final JdbcClient db;

    public AgentMessageRepository(JdbcClient db) {
        this.db = db;
    }

    public List<SysAgentMessage> findBySessionId(String sessionId) {
        String sql = "SELECT * FROM sys_agent_message WHERE session_id = :sessionId ORDER BY create_time ASC";
        return db.sql(sql).param("sessionId", sessionId).query(SysAgentMessage.class).list();
    }

    public void insert(SysAgentMessage message) {
        String sql = """
            INSERT INTO sys_agent_message (session_id, role, content, tool_name, tool_args, need_confirm, confirmed, create_time)
            VALUES (:sessionId, :role, :content, :toolName, :toolArgs, :needConfirm, :confirmed, :createTime)
            """;
        db.sql(sql)
            .param("sessionId", message.getSessionId())
            .param("role", message.getRole())
            .param("content", message.getContent())
            .param("toolName", message.getToolName())
            .param("toolArgs", message.getToolArgs())
            .param("needConfirm", message.getNeedConfirm() != null && message.getNeedConfirm() ? 1 : 0)
            .param("confirmed", message.getConfirmed() != null ? (message.getConfirmed() ? 1 : 0) : null)
            .param("createTime", LocalDateTime.now())
            .update();
    }

    public void updateConfirmed(Long id, Boolean confirmed) {
        String sql = "UPDATE sys_agent_message SET confirmed = :confirmed WHERE id = :id";
        db.sql(sql)
            .param("id", id)
            .param("confirmed", confirmed ? 1 : 0)
            .update();
    }

    public int deleteBySessionId(String sessionId) {
        String sql = "DELETE FROM sys_agent_message WHERE session_id = :sessionId";
        return db.sql(sql).param("sessionId", sessionId).update();
    }
}
