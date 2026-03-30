package com.devlovecode.aiperm.modules.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 个人信息 VO
 */
@Data
@Schema(description = "个人信息")
public class ProfileVO {

	@Schema(description = "用户ID")
	private Long id;

	@Schema(description = "用户名")
	private String username;

	@Schema(description = "昵称")
	private String nickname;

	@Schema(description = "真实姓名")
	private String realName;

	@Schema(description = "邮箱")
	private String email;

	@Schema(description = "手机号")
	private String phone;

	@Schema(description = "性别（0=未知，1=男，2=女）")
	private Integer gender;

	@Schema(description = "头像")
	private String avatar;

	@Schema(description = "部门ID")
	private Long deptId;

	@Schema(description = "部门名称")
	private String deptName;

	@Schema(description = "岗位名称")
	private String postName;

	@Schema(description = "角色名称列表")
	private List<String> roleNames;

	@Schema(description = "状态（0=正常，1=停用）")
	private Integer status;

	@Schema(description = "最后登录IP")
	private String lastLoginIp;

	@Schema(description = "最后登录时间")
	private LocalDateTime lastLoginTime;

	@Schema(description = "创建时间")
	private LocalDateTime createTime;

}
