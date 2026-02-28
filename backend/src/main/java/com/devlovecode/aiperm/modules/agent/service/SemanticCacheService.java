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
            float[] cacheVector = bytesToFloats(cache.getEmbedding());
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
        cache.setEmbedding(floatsToBytes(embedding));

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
     * float[] 转 byte[] 用于存储
     */
    private byte[] floatsToBytes(float[] floats) {
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4);
        for (float f : floats) {
            buffer.putFloat(f);
        }
        return buffer.array();
    }

    /**
     * byte[] 转 float[] 用于读取
     */
    private float[] bytesToFloats(byte[] bytes) {
        if (bytes == null || bytes.length % 4 != 0) {
            return new float[0];
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        float[] floats = new float[bytes.length / 4];
        for (int i = 0; i < floats.length; i++) {
            floats[i] = buffer.getFloat();
        }
        return floats;
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
