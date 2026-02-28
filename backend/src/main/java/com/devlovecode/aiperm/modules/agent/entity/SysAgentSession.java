package com.devlovecode.aiperm.modules.agent.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 会话实体
 */
@Data
public class SysAgentSession {
    private String id;
    private Long userId;
    private String channel;
    private Integer status;
    private LocalDateTime lastActive;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
