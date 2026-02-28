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
