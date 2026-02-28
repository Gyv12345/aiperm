package com.devlovecode.aiperm.modules.agent.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 消息实体
 */
@Data
public class SysAgentMessage {
    private Long id;
    private String sessionId;
    private String role;
    private String content;
    private String toolName;
    private String toolArgs;
    private Boolean needConfirm;
    private Boolean confirmed;
    private LocalDateTime createTime;
}
