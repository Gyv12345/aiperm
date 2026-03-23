package com.devlovecode.aiperm.modules.agent.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 消息实体
 */
@Entity
@Table(name = "sys_agent_message")
@Data
public class SysAgentMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
