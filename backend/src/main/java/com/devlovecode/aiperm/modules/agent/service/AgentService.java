package com.devlovecode.aiperm.modules.agent.service;

import com.devlovecode.aiperm.modules.agent.dto.ChatMessage;
import com.devlovecode.aiperm.modules.agent.dto.LlmResponse;
import com.devlovecode.aiperm.modules.agent.dto.ToolResult;
import com.devlovecode.aiperm.modules.agent.repository.AgentConfigRepository;
import com.devlovecode.aiperm.modules.agent.tool.AgentTool;
import com.devlovecode.aiperm.modules.agent.tool.ToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Agent 核心服务
 * 协调会话、LLM 和工具调用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final SessionService sessionService;
    private final LlmService llmService;
    private final ToolRegistry toolRegistry;
    private final SemanticCacheService semanticCacheService;
    private final AgentConfigRepository configRepo;

    /**
     * 发送消息 (流式)
     */
    public void chatStream(String sessionId, Long userId, String message, StreamCallback callback) {
        try {
            // 1. 尝试语义缓存
            if (semanticCacheEnabled()) {
                Optional<SemanticCacheService.CacheResult> cached =
                    semanticCacheService.findSimilar(userId, message);
                if (cached.isPresent()) {
                    callback.onText(cached.get().getAnswer());
                    callback.onDone();
                    return;
                }
            }

            SessionService.SessionData session = sessionService.getSession(sessionId, userId);
            if (session == null) {
                session = new SessionService.SessionData();
                session.setUserId(userId);
                session.setChannel("WEB");
                session.setMessages(new ArrayList<>());
            }

            ChatMessage userMsg = ChatMessage.user(message);
            session.getMessages().add(userMsg);
            sessionService.appendMessage(sessionId, userMsg);

            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.system(llmService.buildSystemPrompt(userId)));
            messages.addAll(session.getMessages());

            LlmResponse response = llmService.chat(messages);

            processResponse(sessionId, userId, message, messages, response, callback);

        } catch (Exception e) {
            log.error("Agent chat failed", e);
            callback.onError(e);
        }
    }

    /**
     * 处理 LLM 响应
     */
    private void processResponse(String sessionId, Long userId, String originalMessage,
                                 List<ChatMessage> messages, LlmResponse response, StreamCallback callback) {
        if (response.getToolCalls() != null && !response.getToolCalls().isEmpty()) {
            handleToolCalls(sessionId, userId, messages, response.getToolCalls(), callback);
        } else {
            String content = response.getContent();
            if (content != null && !content.isEmpty()) {
                callback.onText(content);

                // 存入语义缓存（仅纯文本回复）
                if (semanticCacheEnabled()) {
                    semanticCacheService.store(userId, originalMessage, content);
                }
            }
            callback.onDone();
        }
    }

    private List<ChatMessage> messages;

    /**
     * 处理工具调用
     */
    private void handleToolCalls(String sessionId, Long userId, List<ChatMessage> messages,
                                 List<LlmResponse.ToolCall> toolCalls, StreamCallback callback) {
        // 必须保留 assistant.tool_calls，后续 tool 消息才是合法上下文
        ChatMessage assistantToolCallsMsg = ChatMessage.assistantToolCalls(toolCalls);
        messages.add(assistantToolCallsMsg);
        sessionService.appendMessage(sessionId, assistantToolCallsMsg);

        for (LlmResponse.ToolCall tc : toolCalls) {
            String toolName = tc.getName();
            String toolArgs = tc.getArguments();

            if (!toolRegistry.isAllowed(toolName)) {
                callback.onText("工具 " + toolName + " 不在允许列表中");
                continue;
            }

            Optional<AgentTool> toolOpt = toolRegistry.getTool(toolName);
            if (toolOpt.isEmpty()) {
                callback.onText("工具 " + toolName + " 不存在");
                continue;
            }

            AgentTool tool = toolOpt.get();

            if (tool.isSensitive()) {
                String actionId = UUID.randomUUID().toString();
                sessionService.savePendingAction(sessionId, actionId, toolName, toolArgs, tc.getId());
                callback.onConfirm(actionId, toolName, "确认执行 " + toolName + " 操作？");
                return;
            }

            executeTool(sessionId, userId, tool, toolArgs, tc.getId(), messages, callback);
        }

        callback.onDone();
    }

    /**
     * 执行工具
     */
    private void executeTool(String sessionId, Long userId, AgentTool tool, String toolArgs,
                             String toolCallId, List<ChatMessage> messages, StreamCallback callback) {
        try {
            log.info("Executing tool: {} with args: {}", tool.getName(), toolArgs);

            ToolResult result = tool.execute(toolArgs, userId);

            callback.onToolResult(tool.getName(), result);

            ChatMessage toolResultMsg = ChatMessage.toolResult(toolCallId, tool.getName(),
                result.isSuccess() ? result.getMessage() : "Error: " + result.getMessage());
            sessionService.appendMessage(sessionId, toolResultMsg);
            messages.add(toolResultMsg);

            if (result.isSuccess()) {
                LlmResponse nextResponse = llmService.chat(messages);
                if (nextResponse.getContent() != null && !nextResponse.getContent().isEmpty()) {
                    callback.onText(nextResponse.getContent());
                }
            } else {
                callback.onText("工具执行失败: " + result.getMessage());
            }

        } catch (Exception e) {
            log.error("Tool execution failed: {}", tool.getName(), e);
            callback.onText("工具执行出错: " + e.getMessage());
        }
    }

    /**
     * 确认敏感操作
     */
    public void confirmAction(String sessionId, Long userId, String actionId, StreamCallback callback) {
        SessionService.PendingAction action = sessionService.consumePendingAction(sessionId);

        if (action == null || !action.getActionId().equals(actionId)) {
            callback.onText("待确认的操作不存在或已过期");
            callback.onDone();
            return;
        }

        Optional<AgentTool> toolOpt = toolRegistry.getTool(action.getToolName());
        if (toolOpt.isEmpty()) {
            callback.onText("工具不存在");
            callback.onDone();
            return;
        }
        if (action.getToolCallId() == null || action.getToolCallId().isBlank()) {
            callback.onText("待确认操作上下文已过期，请重新发起操作");
            callback.onDone();
            return;
        }

        SessionService.SessionData session = sessionService.getSession(sessionId, userId);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(llmService.buildSystemPrompt(userId)));
        if (session != null) {
            messages.addAll(session.getMessages());
        }

        executeTool(sessionId, userId, toolOpt.get(), action.getToolArgs(),
            action.getToolCallId(), messages, callback);

        callback.onDone();
    }

    private boolean semanticCacheEnabled() {
        return configRepo.getValueAsBoolean("semantic_cache_enabled", false);
    }
}
