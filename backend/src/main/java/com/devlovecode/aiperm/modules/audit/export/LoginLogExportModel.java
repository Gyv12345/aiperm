package com.devlovecode.aiperm.modules.audit.export;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class LoginLogExportModel {

	@ExcelProperty("用户名")
	private String username;

	@ExcelProperty("登录IP")
	private String ip;

	@ExcelProperty("登录地点")
	private String location;

	@ExcelProperty("浏览器")
	private String browser;

	@ExcelProperty("操作系统")
	private String os;

	@ExcelProperty("登录状态")
	private String statusText;

	@ExcelProperty("提示消息")
	private String msg;

	@ExcelProperty("登录时间")
	private String loginTime;

}
