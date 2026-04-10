package com.devlovecode.aiperm.modules.auth.core.strategy;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.audit.api.LoginAuditApi;
import com.devlovecode.aiperm.modules.auth.core.enums.LoginType;
import com.devlovecode.aiperm.modules.auth.core.vo.LoginVO;
import com.devlovecode.aiperm.modules.monitor.api.OnlineSessionApi;
import com.devlovecode.aiperm.modules.system.api.SystemAccess;
import com.devlovecode.aiperm.modules.system.api.SystemUserAccount;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PasswordLoginStrategy implements LoginStrategy {

	private final SystemAccess systemAccess;

	private final LoginAuditApi loginAuditApi;

	private final OnlineSessionApi onlineSessionApi;

	@Override
	public String getLoginType() {
		return LoginType.PASSWORD.getCode();
	}

	@Override
	public LoginVO login(String identifier, String credential, String ip, String userAgent,
			HttpServletRequest request) {
		SystemUserAccount user = systemAccess.findUserByUsername(identifier)
			.orElseThrow(() -> new BusinessException("用户名或密码错误"));

		if (user.status() != null && user.status() == 0) {
			throw new BusinessException("账号已被禁用");
		}
		if (!BCrypt.checkpw(credential, user.password())) {
			throw new BusinessException("用户名或密码错误");
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
