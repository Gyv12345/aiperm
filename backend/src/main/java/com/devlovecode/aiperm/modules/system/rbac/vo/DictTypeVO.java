package com.devlovecode.aiperm.modules.system.rbac.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "字典类型响应VO")
public class DictTypeVO {

	@Schema(description = "字典类型ID")
	private Long id;

	@Schema(description = "字典名称")
	private String dictName;

	@Schema(description = "字典类型标识")
	private String dictType;

	@Schema(description = "状态：0-禁用 1-启用")
	private Integer status;

	@Schema(description = "备注")
	private String remark;

	@Schema(description = "创建时间")
	private LocalDateTime createTime;

}
