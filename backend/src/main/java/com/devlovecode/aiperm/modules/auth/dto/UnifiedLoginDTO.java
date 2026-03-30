package com.devlovecode.aiperm.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "统一登录请求")
public class UnifiedLoginDTO {

	@Schema(description = "登录类型：PASSWORD/SMS/EMAIL/OAUTH")
	@NotBlank(message = "登录类型不能为空")
	private String loginType;

	@Schema(description = "登录标识（用户名/手机号/邮箱）")
	@NotBlank(message = "登录标识不能为空")
	private String identifier;

	@Schema(description = "凭证（密码/验证码/OAuth code）")
	@NotBlank(message = "凭证不能为空")
	private String credential;

	@Schema(description = "图形验证码（密码登录时需要）")
	private String imageCaptcha;

	@Schema(description = "图形验证码Key（密码登录时需要）")
	private String imageCaptchaKey;

}
