package com.devlovecode.aiperm.modules.auth.oauth.provider;

import lombok.Builder;
import lombok.Data;

/**
 * OAuth 用户信息 DTO
 */
@Data
@Builder
public class OAuthUserInfo {

	private String openId;

	private String unionId;

	private String nickname;

	private String avatar;

}
