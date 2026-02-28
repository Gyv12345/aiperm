# AI Agent Phase 7: 语义缓存

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现 Embedding 服务和语义缓存功能，降低 LLM 调用成本

**Architecture:** Embedding API + 余弦相似度计算 + MySQL 存储

**Tech Stack:** Spring Boot 3.5 + RestTemplate + MySQL

---

## Task 1: 创建 EmbeddingService

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/service/EmbeddingService.java`

**Step 1: 创建 EmbeddingService**

```java
package com.devlovecode.aiperm.modules.agent.service;

import com.devlovecode.aiperm.modules.agent.entity.SysLlmProvider;
import com.devlovecode.aiperm.modules.agent.repository.AgentConfigRepository;
import com.devlovecode.aiperm.modules.agent.repository.LlmProviderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Embedding 服务
 * 调用 LLM 提供商的 Embedding API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final LlmProviderRepository providerRepo;
    private final AgentConfigRepository configRepo;

    // 缓存最近的 embedding，避免重复计算
    private final Map<String, float[]> embeddingCache = new LinkedHashMap<String, float[]>(100, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, float[]> eldest) {
            return size() > 100;
        }
    };

    /**
     * 生成文本的向量表示
     */
    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            return new float[0];
        }

        // 检查缓存
        String cacheKey = text.trim().toLowerCase();
        if (embeddingCache.containsKey(cacheKey)) {
            return embeddingCache.get(cacheKey);
        }

        try {
            SysLlmProvider provider = getProvider();
            if (provider == null) {
                log.warn("No LLM provider configured for embedding");
                return new float[0];
            }

            String url = provider.getBaseUrl() + "/embeddings";

            Map<String, Object> request = new HashMap<>();
            request.put("model", getEmbeddingModel(provider));
            request.put("input", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(provider.getApiKey());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

            float[] embedding = parseEmbedding(response);

            // 缓存结果
            embeddingCache.put(cacheKey, embedding);

            return embedding;
        } catch (Exception e) {
            log.error("Failed to generate embedding", e);
            return new float[0];
        }
    }

    /**
     * 计算余弦相似度
     */
    public double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length == 0 || b.length != a.length) {
            return 0;
        }

        double dotProduct = 0;
        double normA = 0;
        double normB = 0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) {
            return 0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private SysLlmProvider getProvider() {
        Long defaultId = configRepo.getValueAsLong("default_provider_id", 0);
        if (defaultId > 0) {
            return providerRepo.findById(defaultId).orElse(null);
        }
        return providerRepo.findDefault().orElse(null);
    }

    private String getEmbeddingModel(SysLlmProvider provider) {
        // 根据提供商选择合适的 embedding 模型
        return switch (provider.getName()) {
            case "deepseek" -> "deepseek-embed";
            case "qwen" -> "text-embedding-v3";
            case "openai" -> "text-embedding-3-small";
            default -> "text-embedding-3-small";
        };
    }

    @SuppressWarnings("unchecked")
    private float[] parseEmbedding(Map<String, Object> response) {
        try {
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            if (data == null || data.isEmpty()) {
                return new float[0];
            }

            List<Double> embeddingList = (List<Double>) data.get(0).get("embedding");
            float[] embedding = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                embedding[i] = embeddingList.get(i).floatValue();
            }

            return embedding;
        } catch (Exception e) {
            log.error("Failed to parse embedding response", e);
            return new float[0];
        }
    }
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/service/EmbeddingService.java
git commit -m "feat(agent): add EmbeddingService for vector generation"
```

---

## Task 2: 创建 SemanticCacheService

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/service/SemanticCacheService.java`

**Step 1: 创建 SemanticCacheService**

```java
package com.devlovecode.aiperm.modules.agent.service;

import com.devlovecode.aiperm.modules.agent.entity.SysAgentCache;
import com.devlovecode.aiperm.modules.agent.repository.AgentCacheRepository;
import com.devlovecode.aiperm.modules.agent.repository.AgentConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.List;
import java.util.Optional;

/**
 * 语义缓存服务
 * 通过 Embedding 相似度匹配，缓存常见问题的答案
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SemanticCacheService {

    private final EmbeddingService embeddingService;
    private final AgentCacheRepository cacheRepo;
    private final AgentConfigRepository configRepo;

    /**
     * 查找相似问题的缓存答案
     *
     * @param userId 用户 ID
     * @param question 问题文本
     * @return 缓存结果（如果找到）
     */
    public Optional<CacheResult> findSimilar(Long userId, String question) {
        if (!isCacheEnabled()) {
            return Optional.empty();
        }

        double threshold = configRepo.getValueAsDouble("semantic_cache_threshold", 0.95);

        // 生成查询向量
        float[] queryVector = embeddingService.embed(question);
        if (queryVector.length == 0) {
            return Optional.empty();
        }

        // 查询该用户的缓存
        List<SysAgentCache> caches = cacheRepo.findByUserId(userId);

        SysAgentCache bestMatch = null;
        double bestScore = 0;

        for (SysAgentCache cache : caches) {
            float[] cacheVector = cache.getEmbedding();
            if (cacheVector == null || cacheVector.length == 0) {
                continue;
            }

            double score = embeddingService.cosineSimilarity(queryVector, cacheVector);
            if (score > bestScore && score >= threshold) {
                bestScore = score;
                bestMatch = cache;
            }
        }

        if (bestMatch != null) {
            // 更新命中次数
            cacheRepo.incrementHitCount(bestMatch.getId());
            log.info("Semantic cache hit: score={}, question={}", bestScore, question);
            return Optional.of(new CacheResult(bestMatch.getAnswer(), bestScore));
        }

        return Optional.empty();
    }

    /**
     * 存入缓存
     *
     * @param userId 用户 ID
     * @param question 问题
     * @param answer 答案
     */
    public void store(Long userId, String question, String answer) {
        if (!isCacheEnabled()) {
            return;
        }

        // 只缓存纯文本答案（不缓存工具调用）
        if (answer == null || answer.isBlank()) {
            return;
        }

        float[] embedding = embeddingService.embed(question);
        if (embedding.length == 0) {
            return;
        }

        SysAgentCache cache = new SysAgentCache();
        cache.setUserId(userId);
        cache.setQuestion(question);
        cache.setQuestionHash(hashQuestion(question));
        cache.setAnswer(answer);
        cache.setEmbedding(embedding);

        cacheRepo.insert(cache);
        log.debug("Stored semantic cache for question: {}", question);
    }

    /**
     * 定时清理过期缓存
     * 每天凌晨 3 点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanup() {
        if (!isCacheEnabled()) {
            return;
        }

        // 删除 7 天未命中的缓存
        int deleted = cacheRepo.deleteStale(7);
        if (deleted > 0) {
            log.info("Cleaned up {} stale semantic cache entries", deleted);
        }
    }

    private boolean isCacheEnabled() {
        return configRepo.getValueAsBoolean("semantic_cache_enabled", false);
    }

    private String hashQuestion(String question) {
        try {
            String normalized = question.trim().toLowerCase().replaceAll("\\s+", " ");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(normalized.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(question.hashCode());
        }
    }

    /**
     * 缓存结果
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class CacheResult {
        private String answer;
        private double score;
    }
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/service/SemanticCacheService.java
git commit -m "feat(agent): add SemanticCacheService for caching responses"
```

---

## Task 3: 更新 AgentConfigRepository

**Files:**
- Modify: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/repository/AgentConfigRepository.java`

**Step 1: 添加 getValueAsLong 方法**

```java
// 在 AgentConfigRepository 中添加

public Long getValueAsLong(String key, Long defaultValue) {
    return findByKey(key)
        .map(c -> Long.parseLong(c.getConfigValue()))
        .orElse(defaultValue);
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/repository/AgentConfigRepository.java
git commit -m "feat(agent): add getValueAsLong to AgentConfigRepository"
```

---

## Task 4: 集成语义缓存到 AgentService

**Files:**
- Modify: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/service/AgentService.java`

**Step 1: 添加语义缓存支持**

```java
// 在 AgentService 中注入 SemanticCacheService

private final SemanticCacheService semanticCacheService;

// 在 chatStream 方法开头添加

public void chatStream(String sessionId, Long userId, String message, StreamCallback callback) {
    try {
        // 1. 尝试语义缓存
        if (semanticCacheEnabled()) {
            Optional<SemanticCacheService.CacheResult> cached =
                semanticCacheService.findSimilar(userId, message);
            if (cached.isPresent()) {
                callback.onText("(缓存) " + cached.get().getAnswer());
                callback.onDone();
                return;
            }
        }

        // ... 原有逻辑 ...

        // 在响应完成后，如果是纯文本回复，存入缓存
        // 需要在 processResponse 中收集完整回复后调用
        // semanticCacheService.store(userId, originalMessage, fullResponse);

    } catch (Exception e) {
        // ...
    }
}

private boolean semanticCacheEnabled() {
    return configRepo.getValueAsBoolean("semantic_cache_enabled", false);
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/service/AgentService.java
git commit -m "feat(agent): integrate semantic cache into AgentService"
```

---

## Task 5: 创建缓存管理接口

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/service/AgentConfigService.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/controller/AgentConfigController.java`

**Step 1: 创建 AgentConfigService**

```java
package com.devlovecode.aiperm.modules.agent.service;

import com.devlovecode.aiperm.modules.agent.entity.SysAgentConfig;
import com.devlovecode.aiperm.modules.agent.repository.AgentCacheRepository;
import com.devlovecode.aiperm.modules.agent.repository.AgentConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        configRepo.updateValue(key, value);
    }

    public int getCacheCount() {
        // 简单统计，实际可以用 SQL COUNT
        return cacheRepo.findByUserId(0L).size();
    }

    public void clearCache() {
        // 清理所有缓存（管理功能）
        // 实际实现可以用 DELETE FROM sys_agent_cache
    }
}
```

**Step 2: 创建 AgentConfigController**

```java
package com.devlovecode.aiperm.modules.agent.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.modules.agent.service.AgentConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Agent配置管理")
@RestController
@RequestMapping("/agent/config")
@SaCheckLogin
@RequiredArgsConstructor
public class AgentConfigController {

    private final AgentConfigService configService;

    @Operation(summary = "获取所有配置")
    @SaCheckPermission("agent:config:query")
    @GetMapping
    public R<Map<String, String>> getAll() {
        return R.ok(configService.getAllConfigs());
    }

    @Operation(summary = "更新配置")
    @SaCheckPermission("agent:config:update")
    @PutMapping("/{key}")
    public R<Void> update(@PathVariable String key, @RequestBody Map<String, String> body) {
        configService.updateConfig(key, body.get("value"));
        return R.ok();
    }
}
```

**Step 3: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/service/AgentConfigService.java
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/controller/AgentConfigController.java
git commit -m "feat(agent): add AgentConfig management"
```

---

## Task 6: 编译验证

**Step 1: 编译后端**

```bash
cd backend && ./gradlew build -x test
```

Expected: BUILD SUCCESSFUL

**Step 2: 修复编译错误（如有）**

---

## Completion Checklist

- [ ] EmbeddingService 已创建
- [ ] SemanticCacheService 已创建
- [ ] AgentConfigRepository 已更新
- [ ] AgentService 已集成缓存
- [ ] AgentConfigService/Controller 已创建
- [ ] 编译通过

---

## 实现完成

所有 7 个阶段的计划已完成。可以按顺序执行实现。

**执行顺序：**
1. Phase 1: 基础设施
2. Phase 2: 工具层
3. Phase 3: 服务层
4. Phase 4: 控制器层
5. Phase 5: 前端
6. Phase 6: 提供商管理
7. Phase 7: 语义缓存
