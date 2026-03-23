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
}
