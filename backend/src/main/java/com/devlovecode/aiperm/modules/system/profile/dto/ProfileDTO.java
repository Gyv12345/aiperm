package com.devlovecode.aiperm.modules.system.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 个人信息 DTO
 */
@Data
@Schema(description = "个人信息数据")
public class ProfileDTO {

	@Schema(description = "昵称")
	@Size(max = 50, message = "昵称不能超过50个字符")
	private String nickname;

	@Schema(description = "真实姓名")
	@Size(max = 50, message = "真实姓名不能超过50个字符")
	private String realName;

	@Schema(description = "用户邮箱")
	@Email(message = "邮箱格式不正确")
	@Size(max = 100, message = "邮箱不能超过100个字符")
	private String email;

	@Schema(description = "手机号码")
	@Size(max = 20, message = "手机号码不能超过20个字符")
	private String phone;

	@Schema(description = "用户性别（0=未知，1=男，2=女）")
	private Integer gender;

	@Schema(description = "头像地址")
	@Size(max = 500, message = "头像地址不能超过500个字符")
	private String avatar;

}
