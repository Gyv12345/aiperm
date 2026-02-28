package com.devlovecode.aiperm.modules.agent.repository;

import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.modules.agent.entity.SysLlmProvider;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class LlmProviderRepository extends BaseRepository<SysLlmProvider> {

    public LlmProviderRepository(JdbcClient db) {
        super(db, "sys_llm_provider", SysLlmProvider.class);
    }

    public List<SysLlmProvider> findAllEnabled() {
        String sql = "SELECT * FROM sys_llm_provider WHERE status = 0 AND deleted = 0 ORDER BY sort ASC";
        return db.sql(sql).query(SysLlmProvider.class).list();
    }

    public Optional<SysLlmProvider> findDefault() {
        String sql = "SELECT * FROM sys_llm_provider WHERE is_default = 1 AND status = 0 AND deleted = 0 LIMIT 1";
        return db.sql(sql).query(SysLlmProvider.class).optional();
    }

    public Optional<SysLlmProvider> findByName(String name) {
        String sql = "SELECT * FROM sys_llm_provider WHERE name = :name AND deleted = 0";
        return db.sql(sql).param("name", name).query(SysLlmProvider.class).optional();
    }

    public void clearDefault() {
        String sql = "UPDATE sys_llm_provider SET is_default = 0, update_time = :updateTime WHERE deleted = 0";
        db.sql(sql).param("updateTime", LocalDateTime.now()).update();
    }

    public void setDefault(Long id) {
        String sql = "UPDATE sys_llm_provider SET is_default = 1, update_time = :updateTime WHERE id = :id AND deleted = 0";
        db.sql(sql)
            .param("id", id)
            .param("updateTime", LocalDateTime.now())
            .update();
    }
}
