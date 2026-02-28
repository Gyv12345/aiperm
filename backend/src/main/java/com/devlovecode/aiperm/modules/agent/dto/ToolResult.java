package com.devlovecode.aiperm.modules.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具执行结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolResult {
    private boolean success;
    private String message;
    private Object data;

    public static ToolResult success(String message) {
        return new ToolResult(true, message, null);
    }

    public static ToolResult success(String message, Object data) {
        return new ToolResult(true, message, data);
    }

    public static ToolResult error(String message) {
        return new ToolResult(false, message, null);
    }
}
