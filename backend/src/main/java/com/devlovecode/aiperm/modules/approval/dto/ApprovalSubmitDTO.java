package com.devlovecode.aiperm.modules.approval.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "通用审批提交通知")
public class ApprovalSubmitDTO {

	@JsonView(Views.Create.class)
	@Schema(description = "场景编码")
	@NotBlank(message = "场景编码不能为空", groups = Views.Create.class)
	@Size(max = 50, message = "场景编码不能超过50个字符")
	private String sceneCode;

	@JsonView(Views.Create.class)
	@Schema(description = "业务类型")
	@NotBlank(message = "业务类型不能为空", groups = Views.Create.class)
	@Size(max = 50, message = "业务类型不能超过50个字符")
	private String businessType;

	@JsonView(Views.Create.class)
	@Schema(description = "业务ID")
	@NotNull(message = "业务ID不能为空", groups = Views.Create.class)
	private Long businessId;

	@JsonView(Views.Create.class)
	@Schema(description = "业务载荷")
	private Map<String, Object> payload;

	@JsonView(Views.Create.class)
	@Schema(description = "是否强制要求审批能力已就绪。true: 未启用/未配置IM时报错；false: 直接跳过审批")
	private Boolean required;

}
