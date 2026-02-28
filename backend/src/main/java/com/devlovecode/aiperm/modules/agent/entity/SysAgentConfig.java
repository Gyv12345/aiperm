package com.devlovecode.aiperm.modules.agent.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 配置实体
 */
@Data
public class SysAgentConfig {
    private Long id;
    private String configKey;
    private String configValue;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
