package com.devlovecode.aiperm.modules.auth.social.config;

import com.devlovecode.aiperm.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.request.AuthGiteeRequest;
import me.zhyd.oauth.request.AuthGithubRequest;
import me.zhyd.oauth.request.AuthRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 第三方平台注册表：启动时为每个已配置(client-id/secret 非空)的平台构建 {@link AuthRequest} 并缓存。
 *
 * 新增平台：在 yml 配置 platforms 节点 + 在 {@link #createRequest} 补一个 case。
 */
@Component
@RequiredArgsConstructor
public class SocialPlatformRegistry {

	private static final String CALLBACK_PATH = "/auth/social/callback";

	private final JustAuthProperties properties;

	private final Map<String, AuthRequest> cache = new ConcurrentHashMap<>();

	@PostConstruct
	public void init() {
		Map<String, JustAuthProperties.Platform> platforms = properties.getPlatforms();
		if (platforms == null) {
			return;
		}
		String redirectUri = properties.getCallbackBaseUrl() + CALLBACK_PATH;
		platforms.forEach((platform, p) -> {
			if (p == null || !p.isConfigured()) {
				return;
			}
			AuthConfig config = AuthConfig.builder()
				.clientId(p.getClientId())
				.clientSecret(p.getClientSecret())
				.redirectUri(redirectUri)
				.build();
			cache.put(platform.toUpperCase(), createRequest(platform, config));
		});
	}

	/** 获取某平台的 AuthRequest，未配置则抛业务异常 */
	public AuthRequest get(String platform) {
		AuthRequest request = cache.get(platform.toUpperCase());
		if (request == null) {
			throw new BusinessException("不支持或未启用的登录平台：" + platform);
		}
		return request;
	}

	/** 已启用的平台列表（供 login-config 返回） */
	public List<String> enabledPlatforms() {
		return new ArrayList<>(cache.keySet());
	}

	private AuthRequest createRequest(String platform, AuthConfig config) {
		return switch (platform.toUpperCase()) {
			case "GITHUB" -> new AuthGithubRequest(config);
			case "GITEE" -> new AuthGiteeRequest(config);
			default -> throw new BusinessException("不支持的登录平台：" + platform);
		};
	}

}
