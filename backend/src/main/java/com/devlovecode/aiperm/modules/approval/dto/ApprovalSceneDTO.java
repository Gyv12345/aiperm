package com.devlovecode.aiperm.modules.approval.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "审批场景DTO")
public class ApprovalSceneDTO {

    @JsonView(Views.Query.class)
    private Integer page = 1;

    @JsonView(Views.Query.class)
    private Integer pageSize = 10;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "场景编码")
    @NotBlank(message = "场景编码不能为空", groups = Views.Create.class)
    @Size(max = 50, message = "场景编码不能超过50字符")
    private String sceneCode;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "场景名称")
    @NotBlank(message = "场景名称不能为空", groups = Views.Create.class)
    @Size(max = 100, message = "场景名称不能超过100字符")
    private String sceneName;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "平台")
    @NotBlank(message = "平台不能为空", groups = Views.Create.class)
    private String platform;

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "平台审批模板ID")
    private String templateId;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "是否启用")
    @NotNull(message = "启用状态不能为空", groups = Views.Create.class)
    private Integer enabled;

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "回调处理器Bean名")
    private String handlerClass;

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "超时小时数")
    private Integer timeoutHours;

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "超时动作")
    private String timeoutAction;
}
