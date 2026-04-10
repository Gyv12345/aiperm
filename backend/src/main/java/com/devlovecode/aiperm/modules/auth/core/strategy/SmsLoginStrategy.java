package com.devlovecode.aiperm.modules.auth.core.strategy;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.audit.api.LoginAuditApi;
import com.devlovecode.aiperm.modules.auth.captcha.enums.CaptchaScene;
import com.devlovecode.aiperm.modules.auth.captcha.service.CaptchaService;
import com.devlovecode.aiperm.modules.auth.core.enums.LoginType;
import com.devlovecode.aiperm.modules.auth.core.vo.LoginVO;
import com.devlovecode.aiperm.modules.monitor.api.OnlineSessionApi;
import com.devlovecode.aiperm.modules.system.api.SystemAccess;
import com.devlovecode.aiperm.modules.system.api.SystemUserAccount;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SmsLoginStrategy implements LoginStrategy {

	private final SystemAccess systemAccess;

	private final LoginAuditApi loginAuditApi;

	private final OnlineSessionApi onlineSessionApi;

	private final CaptchaService smsCaptchaService;

	public SmsLoginStrategy(SystemAccess systemAccess, LoginAuditApi loginAuditApi, OnlineSessionApi onlineSessionApi,
			@Qualifier("smsCaptchaService") CaptchaService smsCaptchaService) {
		this.systemAccess = systemAccess;
		this.loginAuditApi = loginAuditApi;
		this.onlineSessionApi = onlineSessionApi;
		this.smsCaptchaService = smsCaptchaService;
	}

	@Override
	public String getLoginType() {
		return LoginType.SMS.getCode();
	}

	@Override
	public LoginVO login(String identifier, String credential, String ip, String userAgent,
			HttpServletRequest request) {
		if (!smsCaptchaService.verify(identifier, credential, CaptchaScene.LOGIN)) {
			throw new BusinessException("验证码错误或已过期");
		}

		SystemUserAccount user = systemAccess.findUserByPhone(identifier)
			.orElseThrow(() -> new BusinessException("该手机号未注册"));

		if (user.status() != null && user.status() == 0) {
			throw new BusinessException("账号已被禁用");
		}

		StpUtil.login(user.id());
		systemAccess.updateLoginInfo(user.id(), ip, LocalDateTime.now());
		loginAuditApi.recordSuccess(user.id(), user.username(), ip, userAgent, request);
		onlineSessionApi.registerLoginSession(user.id(), user.username(), ip, userAgent);

		return LoginVO.builder().token(StpUtil.getTokenValue()).userInfo(buildUserInfo(user)).build();
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

}
