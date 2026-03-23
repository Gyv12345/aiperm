package com.devlovecode.aiperm.modules.agent.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * LLM 提供商实体
 */
@Entity
@Table(name = "sys_llm_provider")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysLlmProvider extends BaseEntity {
    private String name;
    private String displayName;
    private String protocol;
    private String apiKey;
    private String baseUrl;
    private String model;
    private Boolean isDefault;
    private Integer status;
    private Integer sort;
    private String remark;
}
