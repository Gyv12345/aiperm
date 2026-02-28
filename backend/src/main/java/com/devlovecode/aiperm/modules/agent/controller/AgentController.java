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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
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
public class AgentController {

    private final AgentService agentService;
    private final SessionService sessionService;
    private final ObjectMapper objectMapper;
    private final Executor agentTaskExecutor;

    public AgentController(AgentService agentService,
                           SessionService sessionService,
                           ObjectMapper objectMapper,
                           @Qualifier("agentTaskExecutor") Executor agentTaskExecutor) {
        this.agentService = agentService;
        this.sessionService = sessionService;
        this.objectMapper = objectMapper;
        this.agentTaskExecutor = agentTaskExecutor;
    }

    private static final long SSE_TIMEOUT = 5 * 60 * 1000L; // 5 分钟

    /**
     * SSE 流式对话
     */
    @Operation(summary = "流式对话")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@Valid @RequestBody ChatRequest request) {
        Long userId = Long.parseLong(StpUtil.getLoginIdAsString());

        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = sessionService.createSession(userId, "WEB");
        }

        final String finalSessionId = sessionId;
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

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

        emitter.onTimeout(() -> {
            log.warn("SSE timeout: sessionId={}", finalSessionId);
            emitter.complete();
        });

        emitter.onError(e -> log.error("SSE error: sessionId={}", finalSessionId, e));

        return emitter;
    }

    /**
     * 同步对话 (企微等场景)
     */
    @Operation(summary = "同步对话")
    @PostMapping("/chat")
    public R<Map<String, Object>> chat(@Valid @RequestBody ChatRequest request) {
        Long userId = Long.parseLong(StpUtil.getLoginIdAsString());

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
        var session = sessionService.getSession(sessionId, userId);
        if (session != null) {
            sessionService.deleteSession(sessionId);
        }
        return R.ok();
    }
}
