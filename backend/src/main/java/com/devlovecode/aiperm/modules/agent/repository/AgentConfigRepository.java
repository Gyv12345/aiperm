package com.devlovecode.aiperm.modules.agent.repository;

import com.devlovecode.aiperm.modules.agent.entity.SysAgentConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Agent 配置仓储
 */
@Repository
public interface AgentConfigRepository extends JpaRepository<SysAgentConfig, Long> {

    /**
     * 根据配置键查询
     */
    Optional<SysAgentConfig> findByConfigKey(String configKey);

    /**
     * 更新配置值
     */
    @Modifying
    @Query("UPDATE SysAgentConfig c SET c.configValue = :value, c.updateTime = :updateTime WHERE c.configKey = :key")
    int updateValue(@Param("key") String key, @Param("value") String value, @Param("updateTime") LocalDateTime updateTime);

    // ========== 便捷方法 ==========

    default String getValue(String key) {
        return findByConfigKey(key).map(SysAgentConfig::getConfigValue).orElse(null);
    }

    default int getValueAsInt(String key, int defaultValue) {
        String value = getValue(key);
        if (value == null || value.isBlank()) return defaultValue;
        try { return Integer.parseInt(value.trim()); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    default long getValueAsLong(String key, long defaultValue) {
        String value = getValue(key);
        if (value == null || value.isBlank()) return defaultValue;
        try { return Long.parseLong(value.trim()); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    default double getValueAsDouble(String key, double defaultValue) {
        String value = getValue(key);
        if (value == null || value.isBlank()) return defaultValue;
        try { return Double.parseDouble(value.trim()); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    default boolean getValueAsBoolean(String key, boolean defaultValue) {
        String value = getValue(key);
        if (value == null || value.isBlank()) return defaultValue;
        return "true".equalsIgnoreCase(value.trim()) || "1".equals(value.trim());
    }
}
