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
