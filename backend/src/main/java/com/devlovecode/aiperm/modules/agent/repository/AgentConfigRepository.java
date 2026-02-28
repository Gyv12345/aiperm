package com.devlovecode.aiperm.modules.agent.repository;

import com.devlovecode.aiperm.modules.agent.entity.SysAgentConfig;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class AgentConfigRepository {

    private final JdbcClient db;

    public AgentConfigRepository(JdbcClient db) {
        this.db = db;
    }

    public List<SysAgentConfig> findAll() {
        String sql = "SELECT * FROM sys_agent_config ORDER BY id";
        return db.sql(sql).query(SysAgentConfig.class).list();
    }

    public Optional<SysAgentConfig> findByKey(String key) {
        String sql = "SELECT * FROM sys_agent_config WHERE config_key = :key";
        return db.sql(sql).param("key", key).query(SysAgentConfig.class).optional();
    }

    public String getValue(String key, String defaultValue) {
        return findByKey(key)
            .map(SysAgentConfig::getConfigValue)
            .orElse(defaultValue);
    }

    public int getValueAsInt(String key, int defaultValue) {
        return findByKey(key)
            .map(c -> Integer.parseInt(c.getConfigValue()))
            .orElse(defaultValue);
    }

    public boolean getValueAsBoolean(String key, boolean defaultValue) {
        return findByKey(key)
            .map(c -> Boolean.parseBoolean(c.getConfigValue()))
            .orElse(defaultValue);
    }

    public double getValueAsDouble(String key, double defaultValue) {
        return findByKey(key)
            .map(c -> Double.parseDouble(c.getConfigValue()))
            .orElse(defaultValue);
    }

    public void updateValue(String key, String value) {
        String sql = "UPDATE sys_agent_config SET config_value = :value, update_time = :updateTime WHERE config_key = :key";
        db.sql(sql)
            .param("key", key)
            .param("value", value)
            .param("updateTime", LocalDateTime.now())
            .update();
    }
}
