package com.devlovecode.aiperm.modules.system.rbac.export;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class DictTypeExportModel {

	@ExcelProperty("字典名称")
	private String dictName;

	@ExcelProperty("字典类型")
	private String dictType;

	@ExcelProperty("状态")
	private String statusText;

	@ExcelProperty("备注")
	private String remark;

	@ExcelProperty("创建时间")
	private String createTime;

}
