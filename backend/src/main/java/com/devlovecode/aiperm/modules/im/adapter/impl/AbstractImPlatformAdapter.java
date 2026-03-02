package com.devlovecode.aiperm.modules.im.adapter.impl;

import com.devlovecode.aiperm.modules.approval.entity.SysApprovalScene;
import com.devlovecode.aiperm.modules.im.adapter.CallbackData;
import com.devlovecode.aiperm.modules.im.adapter.ImPlatformAdapter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

@Slf4j
public abstract class AbstractImPlatformAdapter implements ImPlatformAdapter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void sendMessage(String toUserId, String title, String content) {
        log.info("[{}] send message toUserId={}, title={}", getPlatform(), toUserId, title);
    }

    @Override
    public String createApproval(SysApprovalScene scene, Map<String, Object> formData, String initiatorId) {
        String id = getPlatform() + "-" + UUID.randomUUID();
        log.info("[{}] create approval, sceneCode={}, initiator={}, instanceId={}",
                getPlatform(), scene.getSceneCode(), initiatorId, id);
        return id;
    }

    @Override
    public CallbackData parseCallback(String body, Map<String, String> headers) {
        try {
            Map<String, Object> map = objectMapper.readValue(body, new TypeReference<>() {});
            CallbackData data = new CallbackData();
            Object instanceId = map.getOrDefault("instanceId", map.get("instance_id"));
            Object status = map.getOrDefault("status", map.get("result"));
            data.setInstanceId(instanceId == null ? null : String.valueOf(instanceId));
            data.setStatus(status == null ? "PENDING" : String.valueOf(status).toUpperCase());
            return data;
        } catch (Exception e) {
            log.warn("[{}] parse callback failed: {}", getPlatform(), e.getMessage());
            CallbackData fallback = new CallbackData();
            fallback.setStatus("PENDING");
            return fallback;
        }
    }

    @Override
    public boolean verifySignature(String body, Map<String, String> headers) {
        return true;
    }
}
