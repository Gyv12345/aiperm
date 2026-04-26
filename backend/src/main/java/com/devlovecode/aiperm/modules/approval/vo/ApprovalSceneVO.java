package com.devlovecode.aiperm.modules.approval.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "审批场景响应")
public class ApprovalSceneVO {

	private Long id;

	private String sceneCode;

	private String sceneName;

	private String businessType;

	private String platform;

	private String templateId;

	private Integer enabled;

	private String handlerBeanName;

	private Integer autoSubmitEnabled;

	private Integer allowDuplicatePending;

	private Integer timeoutHours;

	private String timeoutAction;

	private String notifyTemplateCode;

	private String remark;

	private LocalDateTime createTime;

}
