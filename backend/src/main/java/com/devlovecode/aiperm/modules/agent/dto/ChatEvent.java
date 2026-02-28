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
