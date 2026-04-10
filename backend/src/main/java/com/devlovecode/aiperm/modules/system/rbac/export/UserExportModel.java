package com.devlovecode.aiperm.modules.system.rbac.export;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class UserExportModel {

	@ExcelProperty("用户名")
	private String username;

	@ExcelProperty("昵称")
	private String nickname;

	@ExcelProperty("真实姓名")
	private String realName;

	@ExcelProperty("邮箱")
	private String email;

	@ExcelProperty("手机号")
	private String phone;

	@ExcelProperty("性别")
	private String genderText;

	@ExcelProperty("部门")
	private String deptName;

	@ExcelProperty("岗位")
	private String postNames;

	@ExcelProperty("角色")
	private String roleNames;

	@ExcelProperty("状态")
	private String statusText;

	@ExcelProperty("备注")
	private String remark;

	@ExcelProperty("创建时间")
	private String createTime;

}
