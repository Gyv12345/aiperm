package com.devlovecode.aiperm.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "导入错误明细")
public class ImportErrorVO {

	@Schema(description = "Excel 行号", example = "2")
	private Integer rowNumber;

	@Schema(description = "错误信息")
	private String message;

}
