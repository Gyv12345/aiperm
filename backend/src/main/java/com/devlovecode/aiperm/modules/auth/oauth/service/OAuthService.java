package com.devlovecode.aiperm.modules.auth.oauth.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.util.ClientIpUtils;
import com.devlovecode.aiperm.modules.audit.api.LoginAuditApi;
import com.devlovecode.aiperm.modules.auth.oauth.entity.SysUserOauth;
import com.devlovecode.aiperm.modules.auth.oauth.provider.OAuthProvider;
import com.devlovecode.aiperm.modules.auth.oauth.provider.OAuthUserInfo;
import com.devlovecode.aiperm.modules.auth.oauth.repository.UserOauthRepository;
import com.devlovecode.aiperm.modules.auth.oauth.vo.OauthBindingVO;
import com.devlovecode.aiperm.modules.auth.core.vo.LoginVO;
import com.devlovecode.aiperm.modules.monitor.api.OnlineSessionApi;
import com.devlovecode.aiperm.modules.system.api.SystemAccess;
import com.devlovecode.aiperm.modules.system.api.SystemUserAccount;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * OAuth 服务
 */
@Service
@RequiredArgsConstructor
public class OAuthService {

	private final UserOauthRepository userOauthRepo;

	private final List<OAuthProvider> providers;

	private final SystemAccess systemAccess;

	private final LoginAuditApi loginAuditApi;

	private final OnlineSessionApi onlineSessionApi;

	private Map<String, OAuthProvider> providerMap;

	@PostConstruct
	public void init() {
		providerMap = providers.stream().collect(Collectors.toMap(OAuthProvider::getPlatform, Function.identity()));
	}

	/** 获取授权跳转 URL */
	public String getAuthorizationUrl(String platform, String state) {
		return getProvider(platform).getAuthorizationUrl(state);
	}

	/**
	 * OAuth 登录（通过 code 查找绑定用户，直接登录）
	 */
	@Transactional
	public LoginVO oauthLogin(String platform, String code) {
		OAuthUserInfo userInfo = getProvider(platform).getUserInfo(code);

		SysUserOauth binding = userOauthRepo.findByPlatformAndOpenId(platform, userInfo.getOpenId())
			.orElseThrow(() -> new BusinessException("该" + platform + "账号未绑定，请先登录后在个人中心绑定"));

		SystemUserAccount user = systemAccess.findUserById(binding.getUserId())
			.orElseThrow(() -> new BusinessException("绑定的用户不存在"));

		if (user.status() != null && user.status() == 0) {
			throw new BusinessException("账号已被禁用");
		}

		StpUtil.login(user.id());
		String ip = ClientIpUtils.getCurrentRequestIp();
		systemAccess.updateLoginInfo(user.id(), ip, LocalDateTime.now());
		loginAuditApi.recordSuccess(user.id(), user.username(), ip);
		onlineSessionApi.registerLoginSession(user.id(), user.username(), ip, null);
		userOauthRepo.updateLastLoginTime(binding.getId(), LocalDateTime.now());

		LoginVO.UserInfo loginUserInfo = LoginVO.UserInfo.builder()
			.id(user.id())
			.username(user.username())
			.nickname(user.nickname())
			.avatar(user.avatar())
			.email(user.email())
			.phone(user.phone())
			.build();
		return LoginVO.builder().token(StpUtil.getTokenValue()).userInfo(loginUserInfo).build();
	}

	/**
	 * 主动绑定（用户已登录，绑定第三方账号）
	 */
	@Transactional
	public void bind(String platform, String code) {
		Long userId = StpUtil.getLoginIdAsLong();
		OAuthUserInfo userInfo = getProvider(platform).getUserInfo(code);

		userOauthRepo.findByPlatformAndOpenId(platform, userInfo.getOpenId()).ifPresent(existing -> {
			if (!existing.getUserId().equals(userId)) {
				throw new BusinessException("该" + platform + "账号已绑定其他用户");
			}
		});

		userOauthRepo.findByUserIdAndPlatform(userId, platform).ifPresent(existing -> {
			throw new BusinessException("您已绑定" + platform + "账号，如需重新绑定请先解绑");
		});

		SysUserOauth oauth = new SysUserOauth();
		oauth.setUserId(userId);
		oauth.setPlatform(platform);
		oauth.setOpenId(userInfo.getOpenId());
		oauth.setUnionId(userInfo.getUnionId());
		oauth.setNickname(userInfo.getNickname());
		oauth.setAvatar(userInfo.getAvatar());
		oauth.setLastLoginTime(LocalDateTime.now());
		oauth.setStatus(1);
		oauth.setCreateTime(LocalDateTime.now());
		oauth.setCreateBy(StpUtil.getLoginIdAsString());
		userOauthRepo.save(oauth);
	}

	/** 解绑第三方账号 */
	@Transactional
	public void unbind(String platform) {
		Long userId = StpUtil.getLoginIdAsLong();
		userOauthRepo.findByUserIdAndPlatform(userId, platform)
			.ifPresentOrElse(oauth -> userOauthRepo.softDelete(oauth.getId(), LocalDateTime.now()), () -> {
				throw new BusinessException("您未绑定该平台账号");
			});
	}

	/** 获取用户已绑定的第三方账号列表 */
	public List<OauthBindingVO> getBindings() {
		Long userId = StpUtil.getLoginIdAsLong();
		return userOauthRepo.findByUserIdAndStatus(userId, 1).stream().map(this::toVO).collect(Collectors.toList());
	}

	private OauthBindingVO toVO(SysUserOauth entity) {
		OauthBindingVO vo = new OauthBindingVO();
		vo.setPlatform(entity.getPlatform());
		vo.setNickname(entity.getNickname());
		vo.setAvatar(entity.getAvatar());
		vo.setCreateTime(entity.getCreateTime());
		vo.setLastLoginTime(entity.getLastLoginTime());
		return vo;
	}

	private OAuthProvider getProvider(String platform) {
		OAuthProvider provider = providerMap.get(platform.toUpperCase());
		if (provider == null) {
			throw new BusinessException("不支持的 OAuth 平台：" + platform);
		}
		return provider;
	}

}
