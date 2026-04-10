package com.devlovecode.aiperm.modules.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "健康检查组件")
public class HealthComponentVO {

	@Schema(description = "组件名称")
	private String name;

	@Schema(description = "状态")
	private String status;

	@Schema(description = "详情")
	private String details;

}
