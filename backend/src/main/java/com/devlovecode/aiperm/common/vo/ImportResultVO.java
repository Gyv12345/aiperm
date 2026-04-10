package com.devlovecode.aiperm.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "导入结果")
public class ImportResultVO {

	@Schema(description = "成功条数", example = "10")
	private Integer successCount = 0;

	@Schema(description = "失败条数", example = "2")
	private Integer failureCount = 0;

	@Schema(description = "错误列表")
	private List<ImportErrorVO> errors = new ArrayList<>();

	public void addSuccess() {
		successCount++;
	}

	public void addError(int rowNumber, String message) {
		failureCount++;
		errors.add(new ImportErrorVO(rowNumber, message));
	}

}
