package com.devlovecode.aiperm.modules.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private List<Map<String, Object>> toolCalls;

    public static ChatMessage system(String content) {
        return new ChatMessage("system", content, null, null, null);
    }

    public static ChatMessage user(String content) {
        return new ChatMessage("user", content, null, null, null);
    }

    public static ChatMessage assistant(String content) {
        return new ChatMessage("assistant", content, null, null, null);
    }

    public static ChatMessage assistantToolCalls(List<LlmResponse.ToolCall> toolCalls) {
        List<Map<String, Object>> calls = new ArrayList<>();
        for (LlmResponse.ToolCall tc : toolCalls) {
            Map<String, Object> call = new HashMap<>();
            call.put("id", tc.getId());
            call.put("type", "function");
            Map<String, Object> func = new HashMap<>();
            func.put("name", tc.getName());
            func.put("arguments", tc.getArguments());
            call.put("function", func);
            calls.add(call);
        }
        return new ChatMessage("assistant", null, null, null, calls);
    }

    public static ChatMessage toolResult(String toolCallId, String toolName, String content) {
        return new ChatMessage("tool", content, toolCallId, toolName, null);
    }
}
