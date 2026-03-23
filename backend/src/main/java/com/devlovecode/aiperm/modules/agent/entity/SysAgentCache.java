package com.devlovecode.aiperm.modules.agent.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Agent 语义缓存实体
 */
@Entity
@Table(name = "sys_agent_cache")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysAgentCache extends BaseEntity {
    private Long userId;
    private String questionHash;
    private String question;
    private String answer;
    private byte[] embedding;
    private Integer hitCount;
}
