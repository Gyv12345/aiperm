package com.devlovecode.aiperm.modules.approval.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "审批场景数据")
public class ApprovalSceneDTO {

	@JsonView(Views.Query.class)
	@Schema(description = "页码", example = "1")
	private Integer page = 1;

	@JsonView(Views.Query.class)
	@Schema(description = "每页条数", example = "10")
	private Integer pageSize = 10;

	@JsonView({ Views.Query.class, Views.Create.class, Views.Update.class })
	@Schema(description = "场景编码")
	@NotBlank(message = "场景编码不能为空", groups = { Views.Create.class, Views.Update.class })
	@Size(max = 50, message = "场景编码不能超过50个字符")
	private String sceneCode;

	@JsonView({ Views.Query.class, Views.Create.class, Views.Update.class })
	@Schema(description = "场景名称")
	@NotBlank(message = "场景名称不能为空", groups = { Views.Create.class, Views.Update.class })
	@Size(max = 100, message = "场景名称不能超过100个字符")
	private String sceneName;

	@JsonView({ Views.Query.class, Views.Create.class, Views.Update.class })
	@Schema(description = "业务类型")
	@NotBlank(message = "业务类型不能为空", groups = { Views.Create.class, Views.Update.class })
	@Size(max = 50, message = "业务类型不能超过50个字符")
	private String businessType;

	@JsonView({ Views.Query.class, Views.Create.class, Views.Update.class })
	@Schema(description = "平台")
	@NotBlank(message = "平台不能为空", groups = { Views.Create.class, Views.Update.class })
	@Size(max = 20, message = "平台标识不能超过20个字符")
	private String platform;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "模板ID")
	@NotBlank(message = "模板ID不能为空", groups = { Views.Create.class, Views.Update.class })
	@Size(max = 100, message = "模板ID不能超过100个字符")
	private String templateId;

	@JsonView({ Views.Query.class, Views.Create.class, Views.Update.class })
	@Schema(description = "是否启用")
	private Integer enabled;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "处理器Bean名称")
	@NotBlank(message = "处理器Bean不能为空", groups = { Views.Create.class, Views.Update.class })
	@Size(max = 200, message = "处理器Bean不能超过200个字符")
	private String handlerBeanName;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "是否自动提交审批")
	private Integer autoSubmitEnabled;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "是否允许重复待审")
	private Integer allowDuplicatePending;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "超时时间（小时）")
	private Integer timeoutHours;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "超时动作")
	@Size(max = 20, message = "超时动作不能超过20个字符")
	private String timeoutAction;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "通知模板编码")
	@Size(max = 50, message = "通知模板编码不能超过50个字符")
	private String notifyTemplateCode;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "备注")
	@Size(max = 500, message = "备注不能超过500个字符")
	private String remark;

}
