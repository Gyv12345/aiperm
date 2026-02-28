# AI Agent Phase 3: 服务层

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 创建 LlmService、SessionService 和 AgentService 核心服务

**Architecture:** 服务层负责 LLM 调用、会话管理和核心对话逻辑

**Tech Stack:** Spring Boot 3.5 + Redis + RestTemplate/OpenAI Client

---

## Task 1: 创建 LLM 相关 DTO

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/dto/ChatMessage.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/dto/LlmResponse.java`

**Step 1: 创建 ChatMessage**

```java
package com.devlovecode.aiperm.modules.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String role;    // system, user, assistant, tool
    private String content;
    private String toolCallId;
    private String toolName;

    public static ChatMessage system(String content) {
        return new ChatMessage("system", content, null, null);
    }

    public static ChatMessage user(String content) {
        return new ChatMessage("user", content, null, null);
    }

    public static ChatMessage assistant(String content) {
        return new ChatMessage("assistant", content, null, null);
    }

    public static ChatMessage toolResult(String toolCallId, String toolName, String content) {
        return new ChatMessage("tool", content, toolCallId, toolName);
    }
}
```

**Step 2: 创建 LlmResponse**

```java
package com.devlovecode.aiperm.modules.agent.dto;

import lombok.Data;
import java.util.List;

/**
 * LLM 响应
 */
@Data
public class LlmResponse {
    private String content;
    private List<ToolCall> toolCalls;
    private boolean finished;

    @Data
    public static class ToolCall {
        private String id;
        private String name;
        private String arguments;
    }

    public static LlmResponse text(String content) {
        LlmResponse r = new LlmResponse();
        r.setContent(content);
        r.setFinished(true);
        return r;
    }

    public static LlmResponse toolCall(List<ToolCall> toolCalls) {
        LlmResponse r = new LlmResponse();
        r.setToolCalls(toolCalls);
        r.setFinished(false);
        return r;
    }
}
```

**Step 3: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/dto/
git commit -m "feat(agent): add ChatMessage and LlmResponse DTOs"
```

---

## Task 2: 创建 SessionService

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/service/SessionService.java`

**Step 1: 创建 SessionService**

```java
package com.devlovecode.aiperm.modules.agent.service;

import com.devlovecode.aiperm.modules.agent.dto.ChatMessage;
import com.devlovecode.aiperm.modules.agent.repository.AgentConfigRepository;
import com.devlovecode.aiperm.modules.agent.repository.AgentSessionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Agent 会话服务
 * 管理 Redis 中的会话上下文
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final AgentSessionRepository sessionRepo;
    private final AgentConfigRepository configRepo;

    private static final String SESSION_KEY_PREFIX = "agent:session:";

    /**
     * 创建新会话
     */
    public String createSession(Long userId, String channel) {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        int timeout = configRepo.getValueAsInt("session_timeout", 30);

        // 创建会话数据
        SessionData data = new SessionData();
        data.setUserId(userId);
        data.setChannel(channel);
        data.setMessages(new ArrayList<>());

        // 存入 Redis
        String key = SESSION_KEY_PREFIX + sessionId;
        try {
            redis.opsForValue().set(key, objectMapper.writeValueAsString(data),
                timeout, TimeUnit.MINUTES);

            // 持久化到 MySQL
            var entity = new com.devlovecode.aiperm.modules.agent.entity.SysAgentSession();
            entity.setId(sessionId);
            entity.setUserId(userId);
            entity.setChannel(channel);
            entity.setStatus(0);
            entity.setLastActive(java.time.LocalDateTime.now());
            sessionRepo.insert(entity);

            log.info("Created agent session: {} for user: {}", sessionId, userId);
            return sessionId;
        } catch (Exception e) {
            log.error("Failed to create session", e);
            throw new RuntimeException("创建会话失败", e);
        }
    }

    /**
     * 获取会话数据
     */
    public SessionData getSession(String sessionId, Long userId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        String json = redis.opsForValue().get(key);

        if (json == null) {
            return null;
        }

        try {
            SessionData data = objectMapper.readValue(json, SessionData.class);

            // 校验归属
            if (!data.getUserId().equals(userId)) {
                log.warn("Session access denied: sessionId={}, userId={}, ownerId={}",
                    sessionId, userId, data.getUserId());
                return null;
            }

            // 续期
            int timeout = configRepo.getValueAsInt("session_timeout", 30);
            redis.expire(key, timeout, TimeUnit.MINUTES);
            sessionRepo.updateLastActive(sessionId);

            return data;
        } catch (Exception e) {
            log.error("Failed to get session", e);
            return null;
        }
    }

    /**
     * 追加消息到会话
     */
    public void appendMessage(String sessionId, ChatMessage message) {
        String key = SESSION_KEY_PREFIX + sessionId;
        String json = redis.opsForValue().get(key);

        if (json == null) {
            return;
        }

        try {
            SessionData data = objectMapper.readValue(json, SessionData.class);
            data.getMessages().add(message);

            // 限制历史条数
            int maxHistory = configRepo.getValueAsInt("max_history", 20);
            if (data.getMessages().size() > maxHistory) {
                data.setMessages(data.getMessages().subList(
                    data.getMessages().size() - maxHistory,
                    data.getMessages().size()
                ));
            }

            int timeout = configRepo.getValueAsInt("session_timeout", 30);
            redis.opsForValue().set(key, objectMapper.writeValueAsString(data),
                timeout, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Failed to append message", e);
        }
    }

    /**
     * 保存待确认操作
     */
    public void savePendingAction(String sessionId, String actionId, String toolName, String toolArgs) {
        String key = SESSION_KEY_PREFIX + sessionId;
        String json = redis.opsForValue().get(key);

        if (json == null) {
            return;
        }

        try {
            SessionData data = objectMapper.readValue(json, SessionData.class);
            data.setPendingAction(new PendingAction(actionId, toolName, toolArgs));

            int timeout = configRepo.getValueAsInt("session_timeout", 30);
            redis.opsForValue().set(key, objectMapper.writeValueAsString(data),
                timeout, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Failed to save pending action", e);
        }
    }

    /**
     * 获取并清除待确认操作
     */
    public PendingAction consumePendingAction(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        String json = redis.opsForValue().get(key);

        if (json == null) {
            return null;
        }

        try {
            SessionData data = objectMapper.readValue(json, SessionData.class);
            PendingAction action = data.getPendingAction();
            data.setPendingAction(null);

            int timeout = configRepo.getValueAsInt("session_timeout", 30);
            redis.opsForValue().set(key, objectMapper.writeValueAsString(data),
                timeout, TimeUnit.MINUTES);

            return action;
        } catch (Exception e) {
            log.error("Failed to consume pending action", e);
            return null;
        }
    }

    /**
     * 删除会话
     */
    public void deleteSession(String sessionId) {
        redis.delete(SESSION_KEY_PREFIX + sessionId);
        sessionRepo.expireSession(sessionId);
    }

    // === 内部类 ===

    @lombok.Data
    public static class SessionData {
        private Long userId;
        private String channel;
        private List<ChatMessage> messages;
        private PendingAction pendingAction;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class PendingAction {
        private String actionId;
        private String toolName;
        private String toolArgs;
    }
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/service/SessionService.java
git commit -m "feat(agent): add SessionService for session management"
```

---

## Task 3: 创建 LlmService

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/service/LlmService.java`

**Step 1: 创建 LlmService**

```java
package com.devlovecode.aiperm.modules.agent.service;

import com.devlovecode.aiperm.modules.agent.dto.ChatMessage;
import com.devlovecode.aiperm.modules.agent.dto.LlmResponse;
import com.devlovecode.aiperm.modules.agent.entity.SysLlmProvider;
import com.devlovecode.aiperm.modules.agent.repository.AgentConfigRepository;
import com.devlovecode.aiperm.modules.agent.repository.LlmProviderRepository;
import com.devlovecode.aiperm.modules.agent.tool.ToolRegistry;
import com.fasterxml.jackson.databind.JsonNode;
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

            // 构建请求
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
        Long defaultId = configRepo.getValueAsLong("default_provider_id", 0);
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

            // 检查是否有工具调用
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

            // 普通文本响应
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
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/service/LlmService.java
git commit -m "feat(agent): add LlmService for LLM API calls"
```

---

## Task 4: 创建 StreamCallback 接口

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/service/StreamCallback.java`

**Step 1: 创建 StreamCallback**

```java
package com.devlovecode.aiperm.modules.agent.service;

/**
 * 流式响应回调接口
 */
public interface StreamCallback {

    /**
     * 文本片段
     */
    void onText(String delta);

    /**
     * 需要确认
     */
    void onConfirm(String actionId, String toolName, String message);

    /**
     * 工具执行结果
     */
    void onToolResult(String toolName, Object result);

    /**
     * 完成
     */
    void onDone();

    /**
     * 错误
     */
    void onError(Throwable e);
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/service/StreamCallback.java
git commit -m "feat(agent): add StreamCallback interface"
```

---

## Task 5: 创建 AgentService

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/service/AgentService.java`

**Step 1: 创建 AgentService**

```java
package com.devlovecode.aiperm.modules.agent.service;

import com.devlovecode.aiperm.modules.agent.dto.ChatMessage;
import com.devlovecode.aiperm.modules.agent.dto.LlmResponse;
import com.devlovecode.aiperm.modules.agent.dto.ToolResult;
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

    /**
     * 发送消息 (流式)
     */
    public void chatStream(String sessionId, Long userId, String message, StreamCallback callback) {
        try {
            // 1. 获取或创建会话
            SessionService.SessionData session = sessionService.getSession(sessionId, userId);
            if (session == null) {
                session = new SessionService.SessionData();
                session.setUserId(userId);
                session.setChannel("WEB");
                session.setMessages(new ArrayList<>());
            }

            // 2. 追加用户消息
            ChatMessage userMsg = ChatMessage.user(message);
            session.getMessages().add(userMsg);
            sessionService.appendMessage(sessionId, userMsg);

            // 3. 构建完整消息列表
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.system(llmService.buildSystemPrompt(userId)));
            messages.addAll(session.getMessages());

            // 4. 调用 LLM
            LlmResponse response = llmService.chat(messages);

            // 5. 处理响应
            processResponse(sessionId, userId, messages, response, callback);

        } catch (Exception e) {
            log.error("Agent chat failed", e);
            callback.onError(e);
        }
    }

    /**
     * 处理 LLM 响应
     */
    private void processResponse(String sessionId, Long userId, List<ChatMessage> messages,
                                 LlmResponse response, StreamCallback callback) {
        if (response.getToolCalls() != null && !response.getToolCalls().isEmpty()) {
            // 有工具调用
            handleToolCalls(sessionId, userId, messages, response.getToolCalls(), callback);
        } else {
            // 纯文本响应
            String content = response.getContent();
            if (content != null && !content.isEmpty()) {
                // 模拟流式输出（实际应该调用流式 API）
                callback.onText(content);
            }
            callback.onDone();
        }
    }

    /**
     * 处理工具调用
     */
    private void handleToolCalls(String sessionId, Long userId, List<ChatMessage> messages,
                                 List<LlmResponse.ToolCall> toolCalls, StreamCallback callback) {
        for (LlmResponse.ToolCall tc : toolCalls) {
            String toolName = tc.getName();
            String toolArgs = tc.getArguments();

            // 检查白名单
            if (!toolRegistry.isAllowed(toolName)) {
                callback.onText("工具 " + toolName + " 不在允许列表中");
                continue;
            }

            // 获取工具
            Optional<AgentTool> toolOpt = toolRegistry.getTool(toolName);
            if (toolOpt.isEmpty()) {
                callback.onText("工具 " + toolName + " 不存在");
                continue;
            }

            AgentTool tool = toolOpt.get();

            // 检查是否需要确认
            if (tool.isSensitive()) {
                String actionId = UUID.randomUUID().toString();
                sessionService.savePendingAction(sessionId, actionId, toolName, toolArgs);
                callback.onConfirm(actionId, toolName, "确认执行 " + toolName + " 操作？");
                return; // 等待确认
            }

            // 执行工具
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

            // 将工具结果追加到消息
            ChatMessage toolResultMsg = ChatMessage.toolResult(toolCallId, tool.getName(),
                result.isSuccess() ? result.getMessage() : "Error: " + result.getMessage());
            sessionService.appendMessage(sessionId, toolResultMsg);
            messages.add(toolResultMsg);

            // 如果工具执行成功，继续调用 LLM 生成回复
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

        // 执行工具
        Optional<AgentTool> toolOpt = toolRegistry.getTool(action.getToolName());
        if (toolOpt.isEmpty()) {
            callback.onText("工具不存在");
            callback.onDone();
            return;
        }

        // 获取会话消息
        SessionService.SessionData session = sessionService.getSession(sessionId, userId);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(llmService.buildSystemPrompt(userId)));
        if (session != null) {
            messages.addAll(session.getMessages());
        }

        executeTool(sessionId, userId, toolOpt.get(), action.getToolArgs(),
            UUID.randomUUID().toString(), messages, callback);

        callback.onDone();
    }
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/service/AgentService.java
git commit -m "feat(agent): add AgentService for core agent logic"
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

- [ ] ChatMessage DTO 已创建
- [ ] LlmResponse DTO 已创建
- [ ] SessionService 已创建
- [ ] LlmService 已创建
- [ ] StreamCallback 接口已创建
- [ ] AgentService 已创建
- [ ] 编译通过

---

## Next Phase

继续执行 Phase 4: 控制器层实现
