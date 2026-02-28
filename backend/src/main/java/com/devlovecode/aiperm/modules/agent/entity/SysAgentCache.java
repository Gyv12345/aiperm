package com.devlovecode.aiperm.modules.agent.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Agent 语义缓存实体
 */
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
