package com.devlovecode.aiperm.modules.captcha.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 验证码配置更新请求 DTO
 */
@Data
@Schema(description = "验证码配置更新请求")
public class CaptchaConfigDTO {

	@JsonView(Views.Update.class)
	private Integer enabled;

	// SMS
	@JsonView(Views.Update.class)
	private String smsProvider;

	@JsonView(Views.Update.class)
	private String smsAccessKey;

	@JsonView(Views.Update.class)
	private String smsSecretKey;

	@JsonView(Views.Update.class)
	private String smsSignName;

	@JsonView(Views.Update.class)
	private String smsTemplateCode;

	// Email
	@JsonView(Views.Update.class)
	private String emailHost;

	@JsonView(Views.Update.class)
	private Integer emailPort;

	@JsonView(Views.Update.class)
	private String emailUsername;

	@JsonView(Views.Update.class)
	private String emailPassword;

	@JsonView(Views.Update.class)
	private String emailFrom;

	@JsonView(Views.Update.class)
	private String emailFromName;

	// 通用
	@JsonView(Views.Update.class)
	private Integer codeLength;

	@JsonView(Views.Update.class)
	private Integer expireMinutes;

	@JsonView(Views.Update.class)
	private Integer dailyLimit;

}
