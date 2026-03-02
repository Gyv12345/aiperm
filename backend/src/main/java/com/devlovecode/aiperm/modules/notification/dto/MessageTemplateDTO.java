package com.devlovecode.aiperm.modules.notification.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "消息模板DTO")
public class MessageTemplateDTO {
    @JsonView(Views.Query.class)
    private Integer page = 1;

    @JsonView(Views.Query.class)
    private Integer pageSize = 10;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @NotBlank(message = "模板编码不能为空", groups = Views.Create.class)
    @Size(max = 50, message = "模板编码不能超过50个字符")
    private String templateCode;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @NotBlank(message = "模板名称不能为空", groups = Views.Create.class)
    @Size(max = 100, message = "模板名称不能超过100个字符")
    private String templateName;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    private String category;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    private String platform;

    @JsonView({Views.Create.class, Views.Update.class})
    @Size(max = 200, message = "标题不能超过200个字符")
    private String title;

    @JsonView({Views.Create.class, Views.Update.class})
    private String content;
}
