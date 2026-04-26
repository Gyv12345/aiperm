package com.devlovecode.aiperm.modules.approval.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "审批实例查询参数")
public class ApprovalInstanceDTO {

	@JsonView(Views.Query.class)
	@Schema(description = "页码", example = "1")
	private Integer page = 1;

	@JsonView(Views.Query.class)
	@Schema(description = "每页条数", example = "10")
	private Integer pageSize = 10;

	@JsonView(Views.Query.class)
	@Schema(description = "场景编码")
	@Size(max = 50, message = "场景编码不能超过50个字符")
	private String sceneCode;

	@JsonView(Views.Query.class)
	@Schema(description = "业务类型")
	@Size(max = 50, message = "业务类型不能超过50个字符")
	private String businessType;

	@JsonView(Views.Query.class)
	@Schema(description = "平台")
	@Size(max = 20, message = "平台不能超过20个字符")
	private String platform;

	@JsonView(Views.Query.class)
	@Schema(description = "审批状态")
	@Size(max = 20, message = "审批状态不能超过20个字符")
	private String status;

}
