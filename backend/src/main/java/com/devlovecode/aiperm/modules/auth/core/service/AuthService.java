package com.devlovecode.aiperm.modules.auth.core.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.lang.UUID;
import cn.hutool.crypto.digest.BCrypt;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.util.ClientIpUtils;
import com.devlovecode.aiperm.modules.audit.api.LoginAuditApi;
import com.devlovecode.aiperm.modules.auth.core.dto.UnifiedLoginDTO;
import com.devlovecode.aiperm.modules.auth.core.dto.request.LoginRequest;
import com.devlovecode.aiperm.modules.auth.core.enums.LoginType;
import com.devlovecode.aiperm.modules.auth.core.strategy.LoginStrategy;
import com.devlovecode.aiperm.modules.auth.core.strategy.LoginStrategyFactory;
import com.devlovecode.aiperm.modules.auth.core.vo.CaptchaVO;
import com.devlovecode.aiperm.modules.auth.core.vo.LoginConfigVO;
import com.devlovecode.aiperm.modules.auth.core.vo.LoginVO;
import com.devlovecode.aiperm.modules.auth.core.vo.MenuVO;
import com.devlovecode.aiperm.modules.auth.core.vo.UserInfoVO;
import com.devlovecode.aiperm.modules.monitor.api.OnlineSessionApi;
import com.devlovecode.aiperm.modules.system.api.SystemAccess;
import com.devlovecode.aiperm.modules.system.api.SystemMenuDescriptor;
import com.devlovecode.aiperm.modules.system.api.SystemUserAccount;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务
 *
 * @author DevLoveCode
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

	private final StringRedisTemplate redisTemplate;

	private final LoginStrategyFactory loginStrategyFactory;

	private final SystemAccess systemAccess;

	private final LoginAuditApi loginAuditApi;

	private final OnlineSessionApi onlineSessionApi;

	@Value("${auth.captcha.enabled:true}")
	private boolean captchaEnabled;

	private static final String CAPTCHA_PREFIX = "captcha:";

	private static final long CAPTCHA_EXPIRE = 5;

	/**
	 * 生成验证码
	 */
	public CaptchaVO generateCaptcha() {
		LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 20);
		String code = captcha.getCode();
		String imageBase64 = captcha.getImageBase64Data();

		String captchaKey = UUID.fastUUID().toString(true);
		redisTemplate.opsForValue().set(CAPTCHA_PREFIX + captchaKey, code.toLowerCase(), CAPTCHA_EXPIRE, TimeUnit.MINUTES);

		return CaptchaVO.builder().captchaKey(captchaKey).captchaImage(imageBase64).build();
	}

	/**
	 * 登录
	 */
	public LoginVO login(LoginRequest request) {
		HttpServletRequest currentRequest = getCurrentRequest();
		String ip = ClientIpUtils.getCurrentRequestIp();
		String userAgent = resolveUserAgent(currentRequest);
		try {
			validateCaptcha(request.getCaptchaKey(), request.getCaptcha());

			SystemUserAccount user = systemAccess.findUserByUsername(request.getUsername())
				.orElseThrow(() -> new BusinessException("用户名或密码错误"));

			if (user.status() != null && user.status() == 0) {
				throw new BusinessException("账号已被禁用");
			}

			if (!BCrypt.checkpw(request.getPassword(), user.password())) {
				throw new BusinessException("用户名或密码错误");
			}

			StpUtil.login(user.id());
			systemAccess.updateLoginInfo(user.id(), ip, LocalDateTime.now());
			loginAuditApi.recordSuccess(user.id(), user.username(), ip, userAgent, currentRequest);
			onlineSessionApi.registerLoginSession(user.id(), user.username(), ip, userAgent);

			return LoginVO.builder().token(StpUtil.getTokenValue()).userInfo(buildUserInfo(user)).build();
		}
		catch (BusinessException e) {
			loginAuditApi.recordFailed(request.getUsername(), ip, e.getMessage(), userAgent, currentRequest);
			throw e;
		}
		catch (Exception e) {
			log.error("传统登录异常: username={}, ip={}", request.getUsername(), ip, e);
			loginAuditApi.recordFailed(request.getUsername(), ip, "系统异常", userAgent, currentRequest);
			throw e;
		}
	}

	/**
	 * 统一登录（支持多种登录方式）
	 */
	public LoginVO unifiedLogin(UnifiedLoginDTO dto, HttpServletRequest request) {
		String ip = ClientIpUtils.getClientIp(request);
		String userAgent = resolveUserAgent(request);

		if (LoginType.PASSWORD.getCode().equalsIgnoreCase(dto.getLoginType())) {
			validateCaptcha(dto.getImageCaptchaKey(), dto.getImageCaptcha());
		}

		try {
			LoginStrategy strategy = loginStrategyFactory.getStrategy(dto.getLoginType());
			return strategy.login(dto.getIdentifier(), dto.getCredential(), ip, userAgent, request);
		}
		catch (BusinessException e) {
			loginAuditApi.recordFailed(dto.getIdentifier(), ip, e.getMessage(), userAgent, request);
			throw e;
		}
		catch (Exception e) {
			log.error("统一登录异常: loginType={}, identifier={}, ip={}", dto.getLoginType(), dto.getIdentifier(), ip, e);
			loginAuditApi.recordFailed(dto.getIdentifier(), ip, "系统异常", userAgent, request);
			throw e;
		}
	}

	/**
	 * 获取登录配置
	 */
	public LoginConfigVO getLoginConfig() {
		boolean smsEnabled = systemAccess.getBooleanConfig("login.sms.enabled", false);
		boolean emailEnabled = systemAccess.getBooleanConfig("login.email.enabled", false);

		List<LoginConfigVO.OAuthConfig> oauthConfigs = new ArrayList<>();
		if (systemAccess.getBooleanConfig("oauth.wework.enabled", false)) {
			oauthConfigs.add(LoginConfigVO.OAuthConfig.builder()
				.platform("WEWORK")
				.displayName("企业微信")
				.icon("/icons/wework.svg")
				.enabled(true)
				.build());
		}
		if (systemAccess.getBooleanConfig("oauth.dingtalk.enabled", false)) {
			oauthConfigs.add(LoginConfigVO.OAuthConfig.builder()
				.platform("DINGTALK")
				.displayName("钉钉")
				.icon("/icons/dingtalk.svg")
				.enabled(true)
				.build());
		}
		if (systemAccess.getBooleanConfig("oauth.feishu.enabled", false)) {
			oauthConfigs.add(LoginConfigVO.OAuthConfig.builder()
				.platform("FEISHU")
				.displayName("飞书")
				.icon("/icons/feishu.svg")
				.enabled(true)
				.build());
		}

		return LoginConfigVO.builder()
			.passwordEnabled(true)
			.smsEnabled(smsEnabled)
			.emailEnabled(emailEnabled)
			.oauthConfigs(oauthConfigs)
			.build();
	}

	/**
	 * 登出
	 */
	public void logout() {
		onlineSessionApi.logoutCurrentToken();
		StpUtil.logout();
	}

	/**
	 * 获取当前用户信息
	 */
	public LoginVO.UserInfo getCurrentUserInfo() {
		Long userId = StpUtil.getLoginIdAsLong();
		SystemUserAccount user = systemAccess.findUserById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
		return buildUserInfo(user);
	}

	/**
	 * 获取当前用户完整信息（包含角色和权限）
	 */
	public UserInfoVO getUserInfo() {
		Long userId = StpUtil.getLoginIdAsLong();
		SystemUserAccount user = systemAccess.findUserById(userId).orElseThrow(() -> new BusinessException("用户不存在"));

		List<String> roles;
		List<String> permissions;

		if (isSuperAdmin(userId)) {
			roles = List.of("super_admin");
			permissions = getAllPermissions();
		}
		else {
			roles = getUserRoles(userId);
			permissions = getUserPermissions(userId);
		}

		return UserInfoVO.builder()
			.id(user.id())
			.username(user.username())
			.nickname(user.nickname())
			.avatar(user.avatar())
			.roles(roles)
			.permissions(permissions)
			.build();
	}

	/**
	 * 获取当前用户可访问的菜单
	 */
	public List<MenuVO> getUserMenus() {
		Long userId = StpUtil.getLoginIdAsLong();
		return buildMenuTree(systemAccess.getAccessibleMenus(userId), 0L);
	}

	private void validateCaptcha(String captchaKey, String captcha) {
		if (!captchaEnabled) {
			return;
		}

		if (captchaKey == null || captchaKey.isBlank()) {
			throw new BusinessException("验证码Key不能为空");
		}
		if (captcha == null || captcha.isBlank()) {
			throw new BusinessException("验证码不能为空");
		}

		String key = CAPTCHA_PREFIX + captchaKey;
		String storedCode = redisTemplate.opsForValue().get(key);

		if (storedCode == null) {
			throw new BusinessException("验证码已过期");
		}
		if (!storedCode.equals(captcha.toLowerCase())) {
			throw new BusinessException("验证码错误");
		}

		redisTemplate.delete(key);
	}

	private LoginVO.UserInfo buildUserInfo(SystemUserAccount user) {
		return LoginVO.UserInfo.builder()
			.id(user.id())
			.username(user.username())
			.nickname(user.nickname())
			.avatar(user.avatar())
			.email(user.email())
			.phone(user.phone())
			.build();
	}

	private HttpServletRequest getCurrentRequest() {
		RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
			return servletRequestAttributes.getRequest();
		}
		return null;
	}

	private String resolveUserAgent(HttpServletRequest request) {
		if (request == null) {
			return "";
		}
		String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
		return userAgent == null ? "" : userAgent.trim();
	}

	public List<String> getUserRoles(Long userId) {
		return systemAccess.getUserRoleKeys(userId);
	}

	public List<String> getUserPermissions(Long userId) {
		return systemAccess.getUserPermissions(userId);
	}

	public List<String> getAllPermissions() {
		return systemAccess.getAllEnabledPermissions();
	}

	public boolean isSuperAdmin(Long userId) {
		return systemAccess.isAdmin(userId);
	}

	private List<MenuVO> buildMenuTree(List<SystemMenuDescriptor> allMenus, Long parentId) {
		Map<Long, List<SystemMenuDescriptor>> groupedByParent = allMenus.stream()
			.collect(Collectors.groupingBy(SystemMenuDescriptor::parentId));

		List<SystemMenuDescriptor> roots = groupedByParent.getOrDefault(parentId, new ArrayList<>());
		return roots.stream().map(menu -> toMenuVO(menu, groupedByParent)).collect(Collectors.toList());
	}

	private MenuVO toMenuVO(SystemMenuDescriptor menu, Map<Long, List<SystemMenuDescriptor>> groupedByParent) {
		MenuVO vo = MenuVO.builder()
			.id(menu.id())
			.menuName(menu.menuName())
			.parentId(menu.parentId())
			.menuType(menu.menuType())
			.path(menu.path())
			.component(menu.component())
			.perms(menu.perms())
			.icon(menu.icon())
			.sort(menu.sort())
			.visible(menu.visible())
			.status(menu.status())
			.build();

		List<SystemMenuDescriptor> children = groupedByParent.getOrDefault(menu.id(), new ArrayList<>());
		if (!children.isEmpty()) {
			vo.setChildren(children.stream().map(child -> toMenuVO(child, groupedByParent)).collect(Collectors.toList()));
		}

		return vo;
	}

}
