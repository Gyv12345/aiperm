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
