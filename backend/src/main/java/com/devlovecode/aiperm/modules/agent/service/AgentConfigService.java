package com.devlovecode.aiperm.modules.agent.service;

import com.devlovecode.aiperm.modules.agent.entity.SysAgentConfig;
import com.devlovecode.aiperm.modules.agent.repository.AgentCacheRepository;
import com.devlovecode.aiperm.modules.agent.repository.AgentConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AgentConfigService {

    private final AgentConfigRepository configRepo;
    private final AgentCacheRepository cacheRepo;

    public Map<String, String> getAllConfigs() {
        Map<String, String> configs = new HashMap<>();
        configRepo.findAll().forEach(c -> configs.put(c.getConfigKey(), c.getConfigValue()));
        return configs;
    }

    public void updateConfig(String key, String value) {
        configRepo.updateValue(key, value, LocalDateTime.now());
    }

    public int getCacheCount() {
        // 简单统计，实际可以用 SQL COUNT
        return cacheRepo.findByUserIdAndDeleted(0L, 0).size();
    }

    public void clearCache() {
        // 清理所有缓存（管理功能）
        // 实际实现可以用 DELETE FROM sys_agent_cache
    }
}
