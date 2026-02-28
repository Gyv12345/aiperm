package com.devlovecode.aiperm.modules.agent.repository;

import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.modules.agent.entity.SysAgentCache;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AgentCacheRepository extends BaseRepository<SysAgentCache> {

    public AgentCacheRepository(JdbcClient db) {
        super(db, "sys_agent_cache", SysAgentCache.class);
    }

    public List<SysAgentCache> findByUserId(Long userId) {
        String sql = "SELECT * FROM sys_agent_cache WHERE user_id = :userId AND deleted = 0";
        return db.sql(sql).param("userId", userId).query(SysAgentCache.class).list();
    }

    public void insert(SysAgentCache cache) {
        String sql = """
            INSERT INTO sys_agent_cache (user_id, question_hash, question, answer, embedding, hit_count, deleted, version, create_time, create_by, update_time, update_by)
            VALUES (:userId, :questionHash, :question, :answer, :embedding, 0, 0, 0, :createTime, :createBy, :updateTime, :updateBy)
            """;
        db.sql(sql)
            .param("userId", cache.getUserId())
            .param("questionHash", cache.getQuestionHash())
            .param("question", cache.getQuestion())
            .param("answer", cache.getAnswer())
            .param("embedding", cache.getEmbedding())
            .param("createTime", LocalDateTime.now())
            .param("createBy", cache.getCreateBy())
            .param("updateTime", LocalDateTime.now())
            .param("updateBy", cache.getUpdateBy())
            .update();
    }

    public void incrementHitCount(Long id) {
        String sql = "UPDATE sys_agent_cache SET hit_count = hit_count + 1 WHERE id = :id";
        db.sql(sql).param("id", id).update();
    }

    public int deleteStale(int daysUnused) {
        String sql = "DELETE FROM sys_agent_cache WHERE hit_count = 0 AND create_time < DATE_SUB(NOW(), INTERVAL :days DAY)";
        return db.sql(sql).param("days", daysUnused).update();
    }
}
