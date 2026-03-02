package com.devlovecode.aiperm.modules.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "审批提交DTO")
public class ApprovalSubmitDTO {

    @Schema(description = "场景编码")
    @NotBlank(message = "场景编码不能为空")
    private String sceneCode;

    @Schema(description = "业务类型")
    @NotBlank(message = "业务类型不能为空")
    private String businessType;

    @Schema(description = "业务ID")
    @NotNull(message = "业务ID不能为空")
    private Long businessId;

    @Schema(description = "审批表单数据")
    private Map<String, Object> formData;
}
