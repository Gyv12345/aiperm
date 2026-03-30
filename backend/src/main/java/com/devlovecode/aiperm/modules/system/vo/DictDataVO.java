package com.devlovecode.aiperm.modules.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "字典数据响应VO")
public class DictDataVO {

	@Schema(description = "字典数据ID")
	private Long id;

	@Schema(description = "字典类型")
	private String dictType;

	@Schema(description = "字典标签（显示值）")
	private String dictLabel;

	@Schema(description = "字典键值（存储值）")
	private String dictValue;

	@Schema(description = "排序")
	private Integer sort;

	@Schema(description = "状态：0-禁用 1-启用")
	private Integer status;

	@Schema(description = "样式属性（tag类型或十六进制颜色）")
	private String listClass;

	@Schema(description = "备注")
	private String remark;

}
