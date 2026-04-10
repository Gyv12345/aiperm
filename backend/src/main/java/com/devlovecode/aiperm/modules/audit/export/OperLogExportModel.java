package com.devlovecode.aiperm.modules.audit.export;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class OperLogExportModel {

	@ExcelProperty("操作模块")
	private String title;

	@ExcelProperty("操作类型")
	private Integer operType;

	@ExcelProperty("操作用户")
	private String operUser;

	@ExcelProperty("请求方式")
	private String requestMethod;

	@ExcelProperty("访问地址")
	private String operUrl;

	@ExcelProperty("操作IP")
	private String operIp;

	@ExcelProperty("执行状态")
	private String statusText;

	@ExcelProperty("错误信息")
	private String errorMsg;

	@ExcelProperty("耗时(ms)")
	private Long costTime;

	@ExcelProperty("操作时间")
	private String createTime;

}
