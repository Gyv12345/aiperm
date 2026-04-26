package com.devlovecode.aiperm.modules.approval.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "审批实例响应")
public class ApprovalInstanceVO {

	private Long id;

	private String sceneCode;

	private String sceneName;

	private String businessType;

	private Long businessId;

	private Long initiatorId;

	private String initiatorName;

	private String platform;

	private String platformInstanceId;

	private String status;

	private String errorMessage;

	private LocalDateTime createTime;

	private LocalDateTime resultTime;

}
