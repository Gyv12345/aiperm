package com.devlovecode.aiperm.modules.agent.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LlmProviderDTO {
    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    private Long id;

    @NotBlank(message = "提供商名称不能为空", groups = {Views.Create.class})
    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    private String name;

    @NotBlank(message = "显示名称不能为空", groups = {Views.Create.class, Views.Update.class})
    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    private String displayName;

    @NotBlank(message = "协议不能为空", groups = {Views.Create.class, Views.Update.class})
    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    private String protocol;

    @NotBlank(message = "API Key不能为空", groups = {Views.Create.class, Views.Update.class})
    @JsonView({Views.Create.class, Views.Update.class})
    private String apiKey;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    private String baseUrl;

    @NotBlank(message = "模型名称不能为空", groups = {Views.Create.class, Views.Update.class})
    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    private String model;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    private Boolean isDefault;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    private Integer status;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    private Integer sort;

    @JsonView({Views.Create.class, Views.Update.class})
    private String remark;
}
