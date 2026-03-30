package com.devlovecode.aiperm.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OperType {

	OTHER(0, "其他"), CREATE(1, "新增"), UPDATE(2, "修改"), DELETE(3, "删除"), QUERY(4, "查询"), EXPORT(5, "导出"), IMPORT(6, "导入"),
	UPLOAD(7, "上传");

	private final int code;

	private final String desc;

}
