package com.devlovecode.aiperm.modules.agent.service;

import com.devlovecode.aiperm.modules.agent.dto.ChatMessage;
import com.devlovecode.aiperm.modules.agent.dto.LlmResponse;
import com.devlovecode.aiperm.modules.agent.entity.SysLlmProvider;
import com.devlovecode.aiperm.modules.agent.repository.AgentConfigRepository;
import com.devlovecode.aiperm.modules.agent.repository.LlmProviderRepository;
import com.devlovecode.aiperm.modules.agent.tool.ToolRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * LLM 调用服务
 * 支持多提供商，OpenAI 兼容 API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final LlmProviderRepository providerRepo;
    private final AgentConfigRepository configRepo;
    private final ToolRegistry toolRegistry;

    /**
     * 调用 LLM (非流式)
     */
    public LlmResponse chat(List<ChatMessage> messages) {
        SysLlmProvider provider = getProvider();
        if (provider == null) {
            return LlmResponse.text("系统未配置 LLM 提供商，请联系管理员");
        }

        try {
            String url = provider.getBaseUrl() + "/chat/completions";

            Map<String, Object> request = new HashMap<>();
            request.put("model", provider.getModel());
            request.put("messages", buildMessages(messages));
            request.put("tools", toolRegistry.getToolsSchema());

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(provider.getApiKey());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            log.debug("Calling LLM: {}", provider.getName());

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

            return parseResponse(response);
        } catch (Exception e) {
            log.error("LLM call failed", e);
            return LlmResponse.text("调用 AI 服务失败: " + e.getMessage());
        }
    }

    /**
     * 获取默认提供商
     */
    private SysLlmProvider getProvider() {
        long defaultId = configRepo.getValueAsLong("default_provider_id", 0);
        if (defaultId > 0) {
            return providerRepo.findById(defaultId).orElse(null);
        }
        return providerRepo.findDefault().orElse(null);
    }

    /**
     * 构建消息列表
     */
    private List<Map<String, Object>> buildMessages(List<ChatMessage> messages) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatMessage msg : messages) {
            Map<String, Object> m = new HashMap<>();
            m.put("role", msg.getRole());
            m.put("content", msg.getContent());
            result.add(m);
        }
        return result;
    }

    /**
     * 解析 LLM 响应
     */
    @SuppressWarnings("unchecked")
    private LlmResponse parseResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                return LlmResponse.text("AI 未返回有效响应");
            }

            Map<String, Object> choice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");

            List<Map<String, Object>> toolCalls = (List<Map<String, Object>>) message.get("tool_calls");
            if (toolCalls != null && !toolCalls.isEmpty()) {
                List<LlmResponse.ToolCall> calls = new ArrayList<>();
                for (Map<String, Object> tc : toolCalls) {
                    Map<String, Object> func = (Map<String, Object>) tc.get("function");
                    LlmResponse.ToolCall call = new LlmResponse.ToolCall();
                    call.setId((String) tc.get("id"));
                    call.setName((String) func.get("name"));
                    call.setArguments((String) func.get("arguments"));
                    calls.add(call);
                }
                return LlmResponse.toolCall(calls);
            }

            String content = (String) message.get("content");
            return LlmResponse.text(content != null ? content : "");
        } catch (Exception e) {
            log.error("Failed to parse LLM response", e);
            return LlmResponse.text("解析 AI 响应失败");
        }
    }

    /**
     * 构建 System Prompt
     */
    public String buildSystemPrompt(Long userId) {
        return """
            你是 aiperm 系统的智能助手，帮助用户管理角色、用户、部门、菜单等。

            可用工具:
            %s

            规则:
            1. 只能调用列出的工具
            2. 如果用户请求超出工具能力范围，明确告知
            3. 敏感操作会要求用户二次确认
            4. 用简洁的中文回复
            5. 如果需要调用工具，优先调用工具而不是猜测答案
            """.formatted(toolRegistry.getToolsDescription());
    }
}
