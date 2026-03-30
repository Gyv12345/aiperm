package com.devlovecode.aiperm.modules.oauth.vo;

import lombok.Data;

/**
 * OAuth 平台配置 VO（密钥脱敏）
 */
@Data
public class OauthConfigVO {

	private Long id;

	private String platform;

	private Integer enabled;

	private String corpId;

	private String agentId;

	private String appKey; // 脱敏：前4位+****

	private String callbackUrl;

	private String remark;

}
