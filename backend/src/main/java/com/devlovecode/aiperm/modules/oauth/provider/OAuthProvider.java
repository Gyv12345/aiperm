package com.devlovecode.aiperm.modules.oauth.provider;

/**
 * OAuth 提供者接口
 */
public interface OAuthProvider {

	/** 获取平台标识 */
	String getPlatform();

	/** 根据 code 获取用户信息 */
	OAuthUserInfo getUserInfo(String code);

	/** 获取 OAuth 授权跳转 URL */
	String getAuthorizationUrl(String state);

}
