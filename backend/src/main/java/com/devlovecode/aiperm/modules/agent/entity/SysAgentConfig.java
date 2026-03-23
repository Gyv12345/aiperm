package com.devlovecode.aiperm.modules.agent.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 配置实体
 */
@Entity
@Table(name = "sys_agent_config")
@Data
public class SysAgentConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String configKey;
    private String configValue;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
