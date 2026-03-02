package com.devlovecode.aiperm.modules.im.adapter;

import com.devlovecode.aiperm.modules.approval.entity.SysApprovalScene;

import java.util.Map;

public interface ImPlatformAdapter {

    String getPlatform();

    void sendMessage(String toUserId, String title, String content);

    String createApproval(SysApprovalScene scene, Map<String, Object> formData, String initiatorId);

    CallbackData parseCallback(String body, Map<String, String> headers);

    boolean verifySignature(String body, Map<String, String> headers);
}
