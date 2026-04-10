package com.devlovecode.aiperm.modules.monitor.export;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class OnlineUserExportModel {

	@ExcelProperty("用户ID")
	private Long userId;

	@ExcelProperty("用户名")
	private String username;

	@ExcelProperty("昵称")
	private String nickname;

	@ExcelProperty("部门")
	private String deptName;

	@ExcelProperty("角色")
	private String roleNames;

	@ExcelProperty("登录IP")
	private String ip;

	@ExcelProperty("浏览器")
	private String browser;

	@ExcelProperty("操作系统")
	private String os;

	@ExcelProperty("登录时间")
	private String loginTime;

	@ExcelProperty("最后活跃时间")
	private String lastAccessTime;

	@ExcelProperty("Token剩余秒数")
	private Long tokenTimeout;

}
