package com.devlovecode.aiperm.modules.system.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "字典数据")
public class DictDataDTO {

	@JsonView({ Views.Create.class, Views.Query.class })
	@Schema(description = "字典类型")
	@NotBlank(message = "字典类型不能为空", groups = { Views.Create.class, Views.Query.class })
	private String dictType;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "字典标签（显示值）")
	@NotBlank(message = "字典标签不能为空", groups = { Views.Create.class, Views.Update.class })
	private String dictLabel;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "字典键值（存储值）")
	@NotBlank(message = "字典键值不能为空", groups = { Views.Create.class, Views.Update.class })
	private String dictValue;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "排序")
	private Integer sort;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "状态：0-禁用 1-启用")
	private Integer status;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "样式属性（tag类型或十六进制颜色，如 success、#ff5500）")
	private String listClass;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "备注")
	private String remark;

}
