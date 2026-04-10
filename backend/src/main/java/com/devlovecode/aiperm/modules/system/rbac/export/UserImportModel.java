package com.devlovecode.aiperm.modules.system.rbac.export;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class UserImportModel {

	@ExcelProperty("用户名")
	private String username;

	@ExcelProperty("密码")
	private String password;

	@ExcelProperty("昵称")
	private String nickname;

	@ExcelProperty("真实姓名")
	private String realName;

	@ExcelProperty("邮箱")
	private String email;

	@ExcelProperty("手机号")
	private String phone;

	@ExcelProperty("性别(男/女/未知)")
	private String genderText;

	@ExcelProperty("部门名称")
	private String deptName;

	@ExcelProperty("岗位编码")
	private String postCode;

	@ExcelProperty("角色编码(逗号分隔)")
	private String roleCodes;

	@ExcelProperty("状态(启用/停用)")
	private String statusText;

	@ExcelProperty("备注")
	private String remark;

}
