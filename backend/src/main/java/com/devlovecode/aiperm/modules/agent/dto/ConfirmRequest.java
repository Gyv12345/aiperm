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
