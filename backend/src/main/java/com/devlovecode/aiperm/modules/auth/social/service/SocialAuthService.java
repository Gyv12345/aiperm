package com.devlovecode.aiperm.modules.auth.social.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.audit.api.LoginAuditApi;
import com.devlovecode.aiperm.modules.auth.social.config.JustAuthProperties;
import com.devlovecode.aiperm.modules.auth.social.config.SocialPlatformRegistry;
import com.devlovecode.aiperm.modules.auth.social.entity.SysSocialBinding;
import com.devlovecode.aiperm.modules.auth.social.repository.SocialBindingRepository;
import com.devlovecode.aiperm.modules.auth.social.vo.SocialBindingVO;
import com.devlovecode.aiperm.modules.monitor.api.OnlineSessionApi;
import com.devlovecode.aiperm.modules.system.api.SystemAccess;
import com.devlovecode.aiperm.modules.system.api.SystemUserAccount;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 第三方社交登录服务。
 *
 * - authorize：生成授权链接，state 存 Redis（编码 login/bind 意图 + userId）。
 * - callback：JustAuth 换取用户信息 → 按 state 意图走登录或绑定 → 返回「重定向到前端的目标 URL」。
 * - 登录复用 StpUtil.login + systemAccess/onlineSessionApi/loginAuditApi 的后登录处理（与 PasswordLoginStrategy 一致）。
 *
 * state 同时承担 CSRF 防护与意图编码，用后即删，TTL 5 分钟。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SocialAuthService {

	private static final String STATE_PREFIX = "oauth:state:";

	private static final long STATE_EXPIRE_MINUTES = 5;

	private static final String FRONTEND_CALLBACK_PATH = "/user/oauth/callback";

	private final SocialPlatformRegistry registry;

	private final SocialBindingRepository bindingRepo;

	private final SystemAccess systemAccess;

	private final OnlineSessionApi onlineSessionApi;

	private final LoginAuditApi loginAuditApi;

	private final StringRedisTemplate redis;

	private final JustAuthProperties properties;

	/**
	 * 生成第三方授权链接。
	 * @param platform 平台标识(GITHUB/GITEE)
	 * @param action login=登录页第三方登录；bind=个人中心绑定(需已登录)
	 */
	public String buildAuthorizeUrl(String platform, String action) {
		registry.get(platform); // 校验平台已启用
		Long userId = null;
		if ("bind".equalsIgnoreCase(action)) {
			userId = StpUtil.getLoginIdAsLong(); // 未登录抛 NotLoginException，由全局处理
		}
		else if (!"login".equalsIgnoreCase(action)) {
			throw new BusinessException("不支持的授权动作：" + action);
		}

		String state = IdUtil.fastSimpleUUID();
		JSONObject intent = new JSONObject().set("action", action.toLowerCase()).set("platform", platform.toUpperCase());
		if (userId != null) {
			intent.set("userId", userId);
		}
		redis.opsForValue()
			.set(STATE_PREFIX + state, JSONUtil.toJsonStr(intent), STATE_EXPIRE_MINUTES, TimeUnit.MINUTES);

		return registry.get(platform).authorize(state);
	}

	/**
	 * 第三方回调处理，返回重定向到前端 OAuthCallback 页的目标 URL。
	 */
	public String handleCallback(String code, String state, String ip, String userAgent, HttpServletRequest request) {
		// 1. 校验并消费 state
		String intentJson = redis.opsForValue().get(STATE_PREFIX + state);
		if (intentJson == null) {
			return redirectUrl("result=error&msg=" + encode("授权状态已过期，请重试"));
		}
		redis.delete(STATE_PREFIX + state);
		JSONObject intent = JSONUtil.parseObj(intentJson);
		String action = intent.getStr("action");
		String platform = intent.getStr("platform").toUpperCase();
		Long bindUserId = intent.getLong("userId");

		// 2. JustAuth 换取用户信息
		AuthUser authUser;
		try {
			AuthRequest authRequest = registry.get(platform);
			AuthCallback callback = AuthCallback.builder().code(code).state(state).build();
			AuthResponse<AuthUser> response = authRequest.login(callback);
			if (!response.ok() || response.getData() == null) {
				String msg = response.getMsg() == null ? "第三方授权失败" : response.getMsg();
				return redirectUrl("result=error&msg=" + encode(msg));
			}
			authUser = response.getData();
		}
		catch (Exception e) {
			log.error("第三方授权换取用户信息失败: platform={}", platform, e);
			return redirectUrl("result=error&msg=" + encode("第三方授权失败：" + e.getMessage()));
		}

		// 3. 按意图分流
		if ("bind".equals(action)) {
			return doBind(platform, authUser, bindUserId);
		}
		return doLogin(platform, authUser, ip, userAgent, request);
	}

	/** 登录：命中绑定则签发 token，未命中则拒绝并提示先绑定 */
	private String doLogin(String source, AuthUser authUser, String ip, String userAgent, HttpServletRequest request) {
		Optional<SysSocialBinding> binding = bindingRepo.findBySourceAndOpenid(source, authUser.getUuid());
		if (binding.isEmpty()) {
			// 未绑定：拒绝，前端引导先登录后在个人中心绑定
			return redirectUrl("result=unbound&platform=" + source);
		}

		Long userId = binding.get().getUserId();
		SystemUserAccount user = systemAccess.findUserById(userId)
			.orElseThrow(() -> new BusinessException("绑定的系统用户不存在"));
		if (user.status() != null && user.status() == 0) {
			return redirectUrl("result=error&msg=" + encode("账号已被禁用"));
		}

		StpUtil.login(userId);
		systemAccess.updateLoginInfo(userId, ip, LocalDateTime.now());
		onlineSessionApi.registerLoginSession(userId, user.username(), ip, userAgent);
		loginAuditApi.recordSuccess(userId, user.username(), ip, userAgent, request);

		return redirectUrl("result=login_success&token=" + StpUtil.getTokenValue());
	}

	/** 绑定：写入绑定关系，校验唯一性 */
	private String doBind(String source, AuthUser authUser, Long userId) {
		if (userId == null) {
			return redirectUrl("result=error&msg=" + encode("缺少用户上下文，无法绑定"));
		}
		// 该第三方账号是否已被其他系统用户绑定
		if (bindingRepo.existsBySourceAndOpenid(source, authUser.getUuid())) {
			return redirectUrl("result=error&msg=" + encode("该第三方账号已被其他账号绑定"));
		}
		// 当前用户是否已绑定过该平台
		if (bindingRepo.findByUserIdAndSource(userId, source).isPresent()) {
			return redirectUrl("result=error&msg=" + encode("您已绑定过该平台账号"));
		}

		SysSocialBinding entity = new SysSocialBinding();
		entity.setUserId(userId);
		entity.setSource(source);
		entity.setOpenid(authUser.getUuid());
		entity.setNickname(authUser.getNickname());
		entity.setAvatar(authUser.getAvatar());
		entity.setEmail(authUser.getEmail());
		entity.setCreateBy(String.valueOf(userId));
		bindingRepo.save(entity);

		return redirectUrl("result=bind_success&platform=" + source);
	}

	/** 当前用户已绑定的第三方账号列表 */
	public List<SocialBindingVO> listBindingsOfCurrentUser() {
		Long userId = StpUtil.getLoginIdAsLong();
		return bindingRepo.findByUserId(userId).stream().map(this::toVO).toList();
	}

	/** 解绑当前用户在某平台的账号 */
	@Transactional
	public void unbind(String platform) {
		registry.get(platform); // 校验平台
		Long userId = StpUtil.getLoginIdAsLong();
		String source = platform.toUpperCase();
		int affected = bindingRepo.deleteByUserIdAndSource(userId, source);
		if (affected == 0) {
			throw new BusinessException("未找到该平台的绑定");
		}
	}

	private String redirectUrl(String query) {
		return properties.getFrontendBaseUrl() + FRONTEND_CALLBACK_PATH + "?" + query;
	}

	private String encode(String text) {
		return URLEncoder.encode(text, StandardCharsets.UTF_8);
	}

	private SocialBindingVO toVO(SysSocialBinding entity) {
		return SocialBindingVO.builder()
			.source(entity.getSource())
			.nickname(entity.getNickname())
			.avatar(entity.getAvatar())
			.email(entity.getEmail())
			.boundAt(entity.getCreateTime())
			.build();
	}

}
