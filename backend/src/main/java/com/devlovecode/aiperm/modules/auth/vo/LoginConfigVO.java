package com.devlovecode.aiperm.modules.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "登录配置响应")
public class LoginConfigVO {

	@Schema(description = "密码登录是否启用")
	private boolean passwordEnabled;

	@Schema(description = "短信验证码登录是否启用")
	private boolean smsEnabled;

	@Schema(description = "邮箱验证码登录是否启用")
	private boolean emailEnabled;

	@Schema(description = "OAuth 登录配置列表")
	private List<OAuthConfig> oauthConfigs;

	@Data
	@Builder
	public static class OAuthConfig {

		private String platform; // WEWORK/DINGTALK/FEISHU

		private String displayName; // 企业微信/钉钉/飞书

		private String icon; // 图标 URL

		private boolean enabled;

	}

}
