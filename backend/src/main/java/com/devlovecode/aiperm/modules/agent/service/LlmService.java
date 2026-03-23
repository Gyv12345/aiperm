package com.devlovecode.aiperm.modules.agent.service;

import com.devlovecode.aiperm.modules.agent.dto.ChatMessage;
import com.devlovecode.aiperm.modules.agent.dto.LlmResponse;
import com.devlovecode.aiperm.modules.agent.entity.SysLlmProvider;
import com.devlovecode.aiperm.modules.agent.repository.AgentConfigRepository;
import com.devlovecode.aiperm.modules.agent.repository.LlmProviderRepository;
import com.devlovecode.aiperm.modules.agent.tool.AgentTool;
import com.devlovecode.aiperm.modules.agent.tool.ToolRegistry;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonBooleanSchema;
import dev.langchain4j.model.chat.request.json.JsonEnumSchema;
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema;
import dev.langchain4j.model.chat.request.json.JsonNumberSchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

/**
 * LLM 调用服务
 * 支持多提供商，OpenAI 兼容 API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

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
            List<ToolSpecification> toolSpecifications = buildToolSpecifications();
            ChatModel model = buildChatModel(provider, toolSpecifications);

            ChatRequest request = ChatRequest.builder()
                    .messages(buildMessages(messages))
                    .toolSpecifications(toolSpecifications)
                    .build();

            log.debug("Calling LLM via LangChain4j: {}", provider.getName());
            ChatResponse response = model.chat(request);
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
        return providerRepo.findByIsDefaultAndStatusAndDeleted(true, 1, 0).orElse(null);
    }

    /**
     * 构建消息列表
     */
    private List<dev.langchain4j.data.message.ChatMessage> buildMessages(List<ChatMessage> messages) {
        List<dev.langchain4j.data.message.ChatMessage> result = new ArrayList<>();
        Set<String> validToolCallIds = new HashSet<>();
        for (ChatMessage msg : messages) {
            switch (msg.getRole()) {
                case "system" -> result.add(SystemMessage.from(msg.getContent()));
                case "user" -> result.add(UserMessage.from(msg.getContent()));
                case "assistant" -> {
                    if (msg.getToolCalls() != null && !msg.getToolCalls().isEmpty()) {
                        List<ToolExecutionRequest> toolExecutionRequests = new ArrayList<>();
                        for (Map<String, Object> tc : msg.getToolCalls()) {
                            String id = (String) tc.get("id");
                            @SuppressWarnings("unchecked")
                            Map<String, Object> function = (Map<String, Object>) tc.get("function");
                            String name = function != null ? (String) function.get("name") : null;
                            String arguments = function != null ? (String) function.get("arguments") : null;
                            if (id != null && name != null) {
                                toolExecutionRequests.add(ToolExecutionRequest.builder()
                                        .id(id)
                                        .name(name)
                                        .arguments(arguments != null ? arguments : "{}")
                                        .build());
                            }
                        }
                        result.add(AiMessage.from(toolExecutionRequests));
                    } else {
                        result.add(AiMessage.from(msg.getContent() != null ? msg.getContent() : ""));
                    }
                }
                case "tool" -> {
                    if (msg.getToolCallId() == null || msg.getToolCallId().isBlank()) {
                        continue;
                    }
                    if (!validToolCallIds.contains(msg.getToolCallId())) {
                        // 跳过历史中不完整的 tool 消息，避免 provider 400
                        log.warn("Skip orphan tool message, tool_call_id={}", msg.getToolCallId());
                        continue;
                    }
                    result.add(ToolExecutionResultMessage.from(
                            msg.getToolCallId(),
                            msg.getToolName() != null ? msg.getToolName() : "",
                            msg.getContent() != null ? msg.getContent() : ""
                    ));
                }
                default -> log.warn("Unknown chat role, skip: {}", msg.getRole());
            }

            if ("assistant".equals(msg.getRole()) && msg.getToolCalls() != null && !msg.getToolCalls().isEmpty()) {
                validToolCallIds.clear();
                for (Map<String, Object> tc : msg.getToolCalls()) {
                    Object id = tc.get("id");
                    if (id instanceof String idStr && !idStr.isBlank()) {
                        validToolCallIds.add(idStr);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 解析 LLM 响应
     */
    private LlmResponse parseResponse(ChatResponse response) {
        try {
            if (response == null || response.aiMessage() == null) {
                return LlmResponse.text("AI 未返回有效响应");
            }

            AiMessage aiMessage = response.aiMessage();
            if (aiMessage.hasToolExecutionRequests()) {
                List<LlmResponse.ToolCall> calls = new ArrayList<>();
                for (ToolExecutionRequest tc : aiMessage.toolExecutionRequests()) {
                    LlmResponse.ToolCall call = new LlmResponse.ToolCall();
                    call.setId(tc.id());
                    call.setName(tc.name());
                    call.setArguments(tc.arguments());
                    calls.add(call);
                }
                return LlmResponse.toolCall(calls);
            }

            String content = aiMessage.text();
            return LlmResponse.text(content != null ? content : "");
        } catch (Exception e) {
            log.error("Failed to parse LLM response", e);
            return LlmResponse.text("解析 AI 响应失败");
        }
    }

    /**
     * 构建 LangChain4j 工具定义
     */
    private List<ToolSpecification> buildToolSpecifications() {
        List<ToolSpecification> specs = new ArrayList<>();
        for (AgentTool tool : toolRegistry.getWhitelistedTools()) {
            try {
                Map<String, Object> schema = objectMapper.readValue(tool.getParameterSchema(), Map.class);
                JsonObjectSchema parameters = toJsonObjectSchema(schema);
                specs.add(ToolSpecification.builder()
                        .name(tool.getName())
                        .description(tool.getDescription())
                        .parameters(parameters)
                        .build());
            } catch (Exception e) {
                log.warn("Failed to parse tool schema, use empty schema. tool={}", tool.getName(), e);
                specs.add(ToolSpecification.builder()
                        .name(tool.getName())
                        .description(tool.getDescription())
                        .parameters(JsonObjectSchema.builder().build())
                        .build());
            }
        }
        return specs;
    }

    @SuppressWarnings("unchecked")
    private JsonObjectSchema toJsonObjectSchema(Map<String, Object> schema) {
        JsonObjectSchema.Builder builder = JsonObjectSchema.builder();

        Object desc = schema.get("description");
        if (desc instanceof String s && !s.isBlank()) {
            builder.description(s);
        }

        Object propertiesObj = schema.get("properties");
        if (propertiesObj instanceof Map<?, ?> properties) {
            for (Map.Entry<?, ?> entry : properties.entrySet()) {
                String name = String.valueOf(entry.getKey());
                if (entry.getValue() instanceof Map<?, ?> propertyMapRaw) {
                    Map<String, Object> propertyMap = new HashMap<>();
                    for (Map.Entry<?, ?> e : propertyMapRaw.entrySet()) {
                        propertyMap.put(String.valueOf(e.getKey()), e.getValue());
                    }
                    builder.addProperty(name, toJsonSchemaElement(propertyMap));
                }
            }
        }

        Object requiredObj = schema.get("required");
        if (requiredObj instanceof List<?> requiredList) {
            List<String> required = new ArrayList<>();
            for (Object item : requiredList) {
                required.add(String.valueOf(item));
            }
            builder.required(required);
        }

        Object additionalProps = schema.get("additionalProperties");
        if (additionalProps instanceof Boolean b) {
            builder.additionalProperties(b);
        }

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private JsonSchemaElement toJsonSchemaElement(Map<String, Object> propertySchema) {
        String type = propertySchema.get("type") instanceof String s ? s : "string";
        String description = propertySchema.get("description") instanceof String s ? s : null;

        if (propertySchema.get("enum") instanceof List<?> enumValuesRaw && !enumValuesRaw.isEmpty()) {
            List<String> enumValues = enumValuesRaw.stream().map(String::valueOf).toList();
            JsonEnumSchema.Builder builder = JsonEnumSchema.builder().enumValues(enumValues);
            if (description != null) {
                builder.description(description);
            }
            return builder.build();
        }

        return switch (type) {
            case "object" -> toJsonObjectSchema(propertySchema);
            case "array" -> {
                JsonArraySchema.Builder builder = JsonArraySchema.builder();
                if (description != null) {
                    builder.description(description);
                }
                Object items = propertySchema.get("items");
                if (items instanceof Map<?, ?> itemsRaw) {
                    Map<String, Object> itemSchema = new HashMap<>();
                    for (Map.Entry<?, ?> e : itemsRaw.entrySet()) {
                        itemSchema.put(String.valueOf(e.getKey()), e.getValue());
                    }
                    builder.items(toJsonSchemaElement(itemSchema));
                } else {
                    builder.items(JsonStringSchema.builder().build());
                }
                yield builder.build();
            }
            case "integer" -> {
                JsonIntegerSchema.Builder builder = JsonIntegerSchema.builder();
                if (description != null) {
                    builder.description(description);
                }
                yield builder.build();
            }
            case "number" -> {
                JsonNumberSchema.Builder builder = JsonNumberSchema.builder();
                if (description != null) {
                    builder.description(description);
                }
                yield builder.build();
            }
            case "boolean" -> {
                JsonBooleanSchema.Builder builder = JsonBooleanSchema.builder();
                if (description != null) {
                    builder.description(description);
                }
                yield builder.build();
            }
            default -> {
                JsonStringSchema.Builder builder = JsonStringSchema.builder();
                if (description != null) {
                    builder.description(description);
                }
                yield builder.build();
            }
        };
    }

    private ChatModel buildChatModel(SysLlmProvider provider, List<ToolSpecification> toolSpecifications) {
        String protocol = provider.getProtocol() != null
                ? provider.getProtocol().toLowerCase(Locale.ROOT).trim()
                : "";

        // 兼容历史数据: protocol 为空时，回退到 name 判断
        if (protocol.isBlank()) {
            String providerName = provider.getName() != null ? provider.getName().toLowerCase(Locale.ROOT) : "";
            if (providerName.contains("anthropic") || providerName.contains("claude")) {
                protocol = "anthropic";
            } else {
                protocol = "openai";
            }
        }

        if ("anthropic".equals(protocol)) {
            return AnthropicChatModel.builder()
                    .baseUrl(provider.getBaseUrl())
                    .apiKey(provider.getApiKey())
                    .modelName(provider.getModel())
                    .toolSpecifications(toolSpecifications)
                    .timeout(Duration.ofSeconds(60))
                    .build();
        }
        return OpenAiChatModel.builder()
                .baseUrl(provider.getBaseUrl())
                .apiKey(provider.getApiKey())
                .modelName(provider.getModel())
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    /**
     * 构建 System Prompt
     */
    public String buildSystemPrompt(Long userId) {
        return """
            你是 aiperm 系统的执行型运维助手，目标是帮助用户高效完成管理操作（查询、创建、更新等），而不是闲聊。

            可用工具:
            %s

            规则:
            1. 只能调用列出的工具
            2. 涉及创建/变更/删除时，优先使用工具执行，不要只给“步骤建议”
            3. 参数不全时，先明确列出缺失参数；参数齐全时直接发起工具调用
            4. 敏感操作会要求用户二次确认，不可绕过
            5. 如果用户请求超出工具能力范围，明确告知边界
            6. 回复保持简洁、结构化、面向执行结果
            """.formatted(toolRegistry.getToolsDescription());
    }
}
