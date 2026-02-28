# AI Agent Phase 4: 控制器层

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 创建 AgentController，实现 SSE 流式响应和 API 接口

**Architecture:** 使用 SseEmitter 实现流式响应，异步执行不阻塞 Servlet 线程

**Tech Stack:** Spring Boot 3.5 + SseEmitter + CompletableFuture

---

## Task 1: 创建请求/响应 DTO

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/dto/ChatRequest.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/dto/ChatEvent.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/dto/ConfirmRequest.java`

**Step 1: 创建 ChatRequest**

```java
package com.devlovecode.aiperm.modules.agent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 对话请求
 */
@Data
public class ChatRequest {
    @NotBlank(message = "消息不能为空")
    private String message;

    /**
     * 会话 ID (可选，不传则创建新会话)
     */
    private String sessionId;
}
```

**Step 2: 创建 ChatEvent (SSE 事件)**

```java
package com.devlovecode.aiperm.modules.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSE 事件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatEvent {
    /**
     * 事件类型: text, confirm, tool_result, done, error
     */
    private String type;

    /**
     * 文本内容 (type=text)
     */
    private String delta;

    /**
     * 确认信息 (type=confirm)
     */
    private String actionId;
    private String toolName;
    private String confirmMessage;

    /**
     * 工具结果 (type=tool_result)
     */
    private Object result;

    /**
     * 错误信息 (type=error)
     */
    private String error;

    public static ChatEvent text(String delta) {
        ChatEvent e = new ChatEvent();
        e.setType("text");
        e.setDelta(delta);
        return e;
    }

    public static ChatEvent confirm(String actionId, String toolName, String message) {
        ChatEvent e = new ChatEvent();
        e.setType("confirm");
        e.setActionId(actionId);
        e.setToolName(toolName);
        e.setConfirmMessage(message);
        return e;
    }

    public static ChatEvent toolResult(Object result) {
        ChatEvent e = new ChatEvent();
        e.setType("tool_result");
        e.setResult(result);
        return e;
    }

    public static ChatEvent done() {
        ChatEvent e = new ChatEvent();
        e.setType("done");
        return e;
    }

    public static ChatEvent error(String message) {
        ChatEvent e = new ChatEvent();
        e.setType("error");
        e.setError(message);
        return e;
    }
}
```

**Step 3: 创建 ConfirmRequest**

```java
package com.devlovecode.aiperm.modules.agent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 确认请求
 */
@Data
public class ConfirmRequest {
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;

    @NotBlank(message = "操作ID不能为空")
    private String actionId;
}
```

**Step 4: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/dto/ChatRequest.java
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/dto/ChatEvent.java
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/dto/ConfirmRequest.java
git commit -m "feat(agent): add ChatRequest, ChatEvent and ConfirmRequest DTOs"
```

---

## Task 2: 创建异步线程池配置

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/config/AgentExecutorConfig.java`

**Step 1: 创建线程池配置**

```java
package com.devlovecode.aiperm.modules.agent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Agent 异步执行器配置
 */
@Configuration
public class AgentExecutorConfig {

    @Bean("agentTaskExecutor")
    public Executor agentTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("agent-");
        executor.setRejectedExecutionHandler((r, e) -> {
            throw new RuntimeException("Agent 任务队列已满，请稍后重试");
        });
        executor.initialize();
        return executor;
    }
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/config/AgentExecutorConfig.java
git commit -m "feat(agent): add agent task executor configuration"
```

---

## Task 3: 创建 AgentController

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/controller/AgentController.java`

**Step 1: 创建 AgentController**

```java
package com.devlovecode.aiperm.modules.agent.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.modules.agent.dto.*;
import com.devlovecode.aiperm.modules.agent.service.AgentService;
import com.devlovecode.aiperm.modules.agent.service.SessionService;
import com.devlovecode.aiperm.modules.agent.service.StreamCallback;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Agent 控制器
 */
@Slf4j
@Tag(name = "Agent智能助手")
@RestController
@RequestMapping("/agent")
@SaCheckLogin
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;
    private final SessionService sessionService;
    private final ObjectMapper objectMapper;
    private final Executor agentTaskExecutor;

    private static final long SSE_TIMEOUT = 5 * 60 * 1000L; // 5 分钟

    /**
     * SSE 流式对话
     */
    @Operation(summary = "流式对话")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@Valid @RequestBody ChatRequest request) {
        Long userId = Long.parseLong(StpUtil.getLoginIdAsString());

        // 获取或创建会话 ID
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = sessionService.createSession(userId, "WEB");
        }

        final String finalSessionId = sessionId;

        // 创建 SSE Emitter
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // 异步执行
        CompletableFuture.runAsync(() -> {
            try {
                agentService.chatStream(finalSessionId, userId, request.getMessage(),
                    new StreamCallback() {
                        @Override
                        public void onText(String delta) {
                            try {
                                emitter.send(SseEmitter.event()
                                    .data(objectMapper.writeValueAsString(ChatEvent.text(delta))));
                            } catch (IOException e) {
                                log.error("Failed to send text event", e);
                            }
                        }

                        @Override
                        public void onConfirm(String actionId, String toolName, String message) {
                            try {
                                emitter.send(SseEmitter.event()
                                    .data(objectMapper.writeValueAsString(
                                        ChatEvent.confirm(actionId, toolName, message))));
                                emitter.complete();
                            } catch (IOException e) {
                                log.error("Failed to send confirm event", e);
                            }
                        }

                        @Override
                        public void onToolResult(String toolName, Object result) {
                            try {
                                emitter.send(SseEmitter.event()
                                    .data(objectMapper.writeValueAsString(
                                        ChatEvent.toolResult(result))));
                            } catch (IOException e) {
                                log.error("Failed to send tool result event", e);
                            }
                        }

                        @Override
                        public void onDone() {
                            try {
                                emitter.send(SseEmitter.event()
                                    .data(objectMapper.writeValueAsString(ChatEvent.done())));
                                emitter.complete();
                            } catch (IOException e) {
                                log.error("Failed to send done event", e);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            try {
                                emitter.send(SseEmitter.event()
                                    .data(objectMapper.writeValueAsString(
                                        ChatEvent.error(e.getMessage()))));
                                emitter.completeWithError(e);
                            } catch (IOException ex) {
                                log.error("Failed to send error event", ex);
                            }
                        }
                    });
            } catch (Exception e) {
                log.error("Agent chat stream failed", e);
                try {
                    emitter.send(SseEmitter.event()
                        .data(objectMapper.writeValueAsString(ChatEvent.error(e.getMessage()))));
                    emitter.completeWithError(e);
                } catch (IOException ex) {
                    log.error("Failed to send error", ex);
                }
            }
        }, agentTaskExecutor);

        // 超时处理
        emitter.onTimeout(() -> {
            log.warn("SSE timeout: sessionId={}", finalSessionId);
            emitter.complete();
        });

        // 错误处理
        emitter.onError(e -> {
            log.error("SSE error: sessionId={}", finalSessionId, e);
        });

        return emitter;
    }

    /**
     * 同步对话 (企微等场景)
     */
    @Operation(summary = "同步对话")
    @PostMapping("/chat")
    public R<Map<String, Object>> chat(@Valid @RequestBody ChatRequest request) {
        Long userId = Long.parseLong(StpUtil.getLoginIdAsString());

        // 获取或创建会话 ID
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = sessionService.createSession(userId, "API");
        }

        StringBuilder responseBuilder = new StringBuilder();
        String[] actionInfo = new String[3]; // actionId, toolName, confirmMessage
        boolean[] needConfirm = {false};

        agentService.chatStream(sessionId, userId, request.getMessage(),
            new StreamCallback() {
                @Override
                public void onText(String delta) {
                    responseBuilder.append(delta);
                }

                @Override
                public void onConfirm(String actionId, String toolName, String message) {
                    actionInfo[0] = actionId;
                    actionInfo[1] = toolName;
                    actionInfo[2] = message;
                    needConfirm[0] = true;
                }

                @Override
                public void onToolResult(String toolName, Object result) {
                }

                @Override
                public void onDone() {
                }

                @Override
                public void onError(Throwable e) {
                    responseBuilder.append("错误: ").append(e.getMessage());
                }
            });

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("content", responseBuilder.toString());

        if (needConfirm[0]) {
            result.put("needConfirm", true);
            result.put("actionId", actionInfo[0]);
            result.put("toolName", actionInfo[1]);
            result.put("confirmMessage", actionInfo[2]);
        }

        return R.ok(result);
    }

    /**
     * 确认敏感操作
     */
    @Operation(summary = "确认操作")
    @PostMapping("/confirm")
    public SseEmitter confirmAction(@Valid @RequestBody ConfirmRequest request) {
        Long userId = Long.parseLong(StpUtil.getLoginIdAsString());

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        CompletableFuture.runAsync(() -> {
            try {
                agentService.confirmAction(request.getSessionId(), userId, request.getActionId(),
                    new StreamCallback() {
                        @Override
                        public void onText(String delta) {
                            try {
                                emitter.send(SseEmitter.event()
                                    .data(objectMapper.writeValueAsString(ChatEvent.text(delta))));
                            } catch (IOException e) {
                                log.error("Failed to send text event", e);
                            }
                        }

                        @Override
                        public void onConfirm(String actionId, String toolName, String message) {
                        }

                        @Override
                        public void onToolResult(String toolName, Object result) {
                            try {
                                emitter.send(SseEmitter.event()
                                    .data(objectMapper.writeValueAsString(
                                        ChatEvent.toolResult(result))));
                            } catch (IOException e) {
                                log.error("Failed to send tool result event", e);
                            }
                        }

                        @Override
                        public void onDone() {
                            try {
                                emitter.send(SseEmitter.event()
                                    .data(objectMapper.writeValueAsString(ChatEvent.done())));
                                emitter.complete();
                            } catch (IOException e) {
                                log.error("Failed to send done event", e);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            try {
                                emitter.send(SseEmitter.event()
                                    .data(objectMapper.writeValueAsString(
                                        ChatEvent.error(e.getMessage()))));
                                emitter.completeWithError(e);
                            } catch (IOException ex) {
                                log.error("Failed to send error event", ex);
                            }
                        }
                    });
            } catch (Exception e) {
                log.error("Confirm action failed", e);
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.error("Failed to complete with error", ex);
                }
            }
        }, agentTaskExecutor);

        emitter.onTimeout(emitter::complete);

        return emitter;
    }

    /**
     * 创建新会话
     */
    @Operation(summary = "创建会话")
    @PostMapping("/session")
    public R<Map<String, String>> createSession() {
        Long userId = Long.parseLong(StpUtil.getLoginIdAsString());
        String sessionId = sessionService.createSession(userId, "WEB");

        return R.ok(Map.of("sessionId", sessionId));
    }

    /**
     * 删除会话
     */
    @Operation(summary = "删除会话")
    @DeleteMapping("/session/{sessionId}")
    public R<Void> deleteSession(@PathVariable String sessionId) {
        Long userId = Long.parseLong(StpUtil.getLoginIdAsString());

        // 验证会话归属
        var session = sessionService.getSession(sessionId, userId);
        if (session != null) {
            sessionService.deleteSession(sessionId);
        }

        return R.ok();
    }
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/controller/AgentController.java
git commit -m "feat(agent): add AgentController with SSE streaming support"
```

---

## Task 4: 编译验证

**Step 1: 编译后端**

```bash
cd backend && ./gradlew build -x test
```

Expected: BUILD SUCCESSFUL

**Step 2: 修复编译错误（如有）**

---

## Task 5: 测试 API

**Step 1: 启动后端**

```bash
cd backend && ./gradlew bootRun
```

**Step 2: 测试创建会话**

```bash
curl -X POST http://localhost:8080/api/agent/session \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json"
```

Expected: `{"code":200,"data":{"sessionId":"xxx"}}`

**Step 3: 测试流式对话**

```bash
curl -X POST http://localhost:8080/api/agent/chat/stream \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"message":"查询所有角色"}'
```

Expected: SSE 事件流

---

## Completion Checklist

- [ ] ChatRequest DTO 已创建
- [ ] ChatEvent DTO 已创建
- [ ] ConfirmRequest DTO 已创建
- [ ] AgentExecutorConfig 已创建
- [ ] AgentController 已创建
- [ ] 编译通过
- [ ] API 测试通过

---

## Next Phase

继续执行 Phase 5: 前端实现
