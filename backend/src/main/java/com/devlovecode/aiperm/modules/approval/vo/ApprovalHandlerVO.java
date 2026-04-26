package com.devlovecode.aiperm.modules.approval.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "审批处理器选项")
public class ApprovalHandlerVO {

	private String beanName;

	private String displayName;

}
