package com.devlovecode.aiperm.modules.auth.social.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * JustAuth 第三方社交登录配置。
 *
 * 对应 application*.yaml 中的 justauth 节点。client-id/secret 留空的平台视为未启用。
 */
@Data
@Component
@ConfigurationProperties(prefix = "justauth")
public class JustAuthProperties {

	/** 后端处理完后重定向回前端的基址 */
	private String frontendBaseUrl = "http://localhost:8000";

	/** 第三方平台回调到后端的基址，redirect-uri 拼接为 {callbackBaseUrl}/auth/social/callback */
	private String callbackBaseUrl = "http://localhost:8080";

	/** 各平台配置，key 为平台标识(如 GITHUB/GITEE) */
	private Map<String, Platform> platforms;

	@Data
	public static class Platform {

		private String clientId;

		private String clientSecret;

		/** client-id/secret 均非空才算已配置启用 */
		public boolean isConfigured() {
			return clientId != null && !clientId.isBlank() && clientSecret != null && !clientSecret.isBlank();
		}

	}

}
