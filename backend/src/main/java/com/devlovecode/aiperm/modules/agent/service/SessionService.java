package com.devlovecode.aiperm.modules.agent.service;

import com.devlovecode.aiperm.modules.agent.dto.ChatMessage;
import com.devlovecode.aiperm.modules.agent.repository.AgentConfigRepository;
import com.devlovecode.aiperm.modules.agent.repository.AgentSessionRepository;
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

        SessionData data = new SessionData();
        data.setUserId(userId);
        data.setChannel(channel);
        data.setMessages(new ArrayList<>());

        String key = SESSION_KEY_PREFIX + sessionId;
        try {
            redis.opsForValue().set(key, objectMapper.writeValueAsString(data),
                timeout, TimeUnit.MINUTES);

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

            if (!data.getUserId().equals(userId)) {
                log.warn("Session access denied: sessionId={}, userId={}, ownerId={}",
                    sessionId, userId, data.getUserId());
                return null;
            }

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
