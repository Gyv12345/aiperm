package com.devlovecode.aiperm.modules.agent.vo;

import lombok.Data;

@Data
public class LlmProviderVO {
    private Long id;
    private String name;
    private String displayName;
    private String baseUrl;
    private String model;
    private Boolean isDefault;
    private Integer status;
    private Integer sort;
    private String remark;
    private String createTime;
    private String updateTime;
}
