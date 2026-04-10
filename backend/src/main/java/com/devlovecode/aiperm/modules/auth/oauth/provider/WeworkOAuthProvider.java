package com.devlovecode.aiperm.modules.auth.oauth.provider;

import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.auth.oauth.entity.SysOauthConfig;
import com.devlovecode.aiperm.modules.auth.oauth.repository.OauthConfigRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * 企业微信 OAuth 提供者
 *
 * 注意：企业微信 OAuth 流程比较复杂，此实现为基础框架。 实际项目需要根据企业微信最新 API 文档进行调整。
 *
 * @see <a href="https://developer.work.weixin.qq.com/document/path/91022">企业微信 OAuth
 * 文档</a>
 */
@Component
public class WeworkOAuthProvider extends AbstractOAuthProvider {

	public WeworkOAuthProvider(OauthConfigRepository oauthConfigRepo, RestTemplate restTemplate) {
		super(oauthConfigRepo, restTemplate);
	}

	@Override
	public String getPlatform() {
		return "WEWORK";
	}

	@Override
	public String getAuthorizationUrl(String state) {
		SysOauthConfig config = getConfig();
		return UriComponentsBuilder.fromUriString("https://open.weixin.qq.com/connect/oauth2/authorize")
			.queryParam("appid", config.getCorpId())
			.queryParam("redirect_uri", config.getCallbackUrl())
			.queryParam("response_type", "code")
			.queryParam("scope", "snsapi_base")
			.queryParam("state", state)
			.toUriString() + "#wechat_redirect";
	}

	@Override
	protected String getAccessToken(String code, SysOauthConfig config) {
		// 企业微信：获取 access_token
		String tokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/gettoken" + "?corpid=" + config.getCorpId()
				+ "&corpsecret=" + config.getAppSecret();

		@SuppressWarnings("unchecked")
		Map<String, Object> tokenResp = restTemplate.getForObject(tokenUrl, Map.class);
		if (tokenResp == null || !"0".equals(String.valueOf(tokenResp.get("errcode")))) {
			throw new BusinessException("获取企业微信 access_token 失败");
		}
		return (String) tokenResp.get("access_token");
	}

	@Override
	protected OAuthUserInfo fetchUserInfo(String accessToken, SysOauthConfig config) {
		// 注意：企业微信需要先通过 code 获取 userId，再通过 userId 获取用户详情
		// 这里简化处理，实际项目需要根据企业微信 API 文档完整实现
		throw new UnsupportedOperationException("WeworkOAuthProvider.fetchUserInfo 需要根据企业微信实际 API 实现。"
				+ "请参考：https://developer.work.weixin.qq.com/document/path/91022");
	}

}
