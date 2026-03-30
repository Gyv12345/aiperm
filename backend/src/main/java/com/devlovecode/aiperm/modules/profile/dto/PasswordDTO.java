package com.devlovecode.aiperm.modules.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码 DTO
 */
@Data
@Schema(description = "修改密码数据")
public class PasswordDTO {

	@Schema(description = "旧密码")
	@NotBlank(message = "旧密码不能为空")
	private String oldPassword;

	@Schema(description = "新密码")
	@NotBlank(message = "新密码不能为空")
	@Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
	private String newPassword;

}
