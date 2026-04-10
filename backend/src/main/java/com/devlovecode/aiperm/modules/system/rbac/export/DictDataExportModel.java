package com.devlovecode.aiperm.modules.system.rbac.export;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class DictDataExportModel {

	@ExcelProperty("字典类型")
	private String dictType;

	@ExcelProperty("字典标签")
	private String dictLabel;

	@ExcelProperty("字典键值")
	private String dictValue;

	@ExcelProperty("排序")
	private Integer sort;

	@ExcelProperty("状态")
	private String statusText;

	@ExcelProperty("样式属性")
	private String listClass;

	@ExcelProperty("备注")
	private String remark;

}
