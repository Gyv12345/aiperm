package com.devlovecode.aiperm.modules.oauth.provider;

import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.oauth.entity.SysOauthConfig;
import com.devlovecode.aiperm.modules.oauth.repository.OauthConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;

/**
 * OAuth 提供者抽象基类（模板方法模式）
 */
@RequiredArgsConstructor
public abstract class AbstractOAuthProvider implements OAuthProvider {

	protected final OauthConfigRepository oauthConfigRepo;

	protected final RestTemplate restTemplate;

	/**
	 * 模板方法：获取用户信息 1. 用 code 换取 access_token 2. 用 access_token 获取用户信息
	 */
	@Override
	public final OAuthUserInfo getUserInfo(String code) {
		SysOauthConfig config = getConfig();
		String accessToken = getAccessToken(code, config);
		return fetchUserInfo(accessToken, config);
	}

	protected SysOauthConfig getConfig() {
		return oauthConfigRepo.findByPlatform(getPlatform())
			.filter(c -> c.getEnabled() != null && c.getEnabled() == 1)
			.orElseThrow(() -> new BusinessException(getPlatform() + " 登录未启用或未配置"));
	}

	/** 子类实现：用 code 换取 access_token */
	protected abstract String getAccessToken(String code, SysOauthConfig config);

	/** 子类实现：用 access_token 获取用户信息 */
	protected abstract OAuthUserInfo fetchUserInfo(String accessToken, SysOauthConfig config);

}
