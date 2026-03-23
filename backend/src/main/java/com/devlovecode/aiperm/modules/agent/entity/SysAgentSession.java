package com.devlovecode.aiperm.modules.agent.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 会话实体
 */
@Entity
@Table(name = "sys_agent_session")
@Data
public class SysAgentSession {
    @Id
    private String id;
    private Long userId;
    private String channel;
    private Integer status;
    private LocalDateTime lastActive;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
