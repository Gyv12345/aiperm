package com.devlovecode.aiperm.modules.agent.repository;

import com.devlovecode.aiperm.modules.agent.entity.SysAgentSession;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class AgentSessionRepository {

    private final JdbcClient db;

    public AgentSessionRepository(JdbcClient db) {
        this.db = db;
    }

    public Optional<SysAgentSession> findById(String sessionId) {
        String sql = "SELECT * FROM sys_agent_session WHERE id = :id";
        return db.sql(sql).param("id", sessionId).query(SysAgentSession.class).optional();
    }

    public List<SysAgentSession> findByUserId(Long userId) {
        String sql = "SELECT * FROM sys_agent_session WHERE user_id = :userId AND status = 0 ORDER BY last_active DESC";
        return db.sql(sql).param("userId", userId).query(SysAgentSession.class).list();
    }

    public void insert(SysAgentSession session) {
        String sql = """
            INSERT INTO sys_agent_session (id, user_id, channel, status, last_active, create_time, update_time)
            VALUES (:id, :userId, :channel, :status, :lastActive, :createTime, :updateTime)
            """;
        db.sql(sql)
            .param("id", session.getId())
            .param("userId", session.getUserId())
            .param("channel", session.getChannel())
            .param("status", session.getStatus())
            .param("lastActive", session.getLastActive())
            .param("createTime", LocalDateTime.now())
            .param("updateTime", LocalDateTime.now())
            .update();
    }

    public void updateLastActive(String sessionId) {
        String sql = "UPDATE sys_agent_session SET last_active = :lastActive, update_time = :updateTime WHERE id = :id";
        db.sql(sql)
            .param("id", sessionId)
            .param("lastActive", LocalDateTime.now())
            .param("updateTime", LocalDateTime.now())
            .update();
    }

    public void expireSession(String sessionId) {
        String sql = "UPDATE sys_agent_session SET status = 1, update_time = :updateTime WHERE id = :id";
        db.sql(sql)
            .param("id", sessionId)
            .param("updateTime", LocalDateTime.now())
            .update();
    }

    public int deleteByUserId(Long userId) {
        String sql = "DELETE FROM sys_agent_session WHERE user_id = :userId";
        return db.sql(sql).param("userId", userId).update();
    }
}
