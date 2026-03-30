package com.devlovecode.aiperm.modules.enterprise.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "系统配置响应VO")
public class ConfigVO {

	@Schema(description = "配置ID")
	private Long id;

	@Schema(description = "配置键")
	private String configKey;

	@Schema(description = "配置值")
	private String configValue;

	@Schema(description = "配置类型")
	private String configType;

	@Schema(description = "备注")
	private String remark;

	@Schema(description = "创建时间")
	private LocalDateTime createTime;

}
