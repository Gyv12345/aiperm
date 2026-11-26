package com.devlovecode.aiperm.common.security;

import com.devlovecode.aiperm.common.enums.ErrorCode;
import com.devlovecode.aiperm.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 密码策略：复杂度校验（新增/修改/重置密码时）+ 有效期校验（密码登录时）。
 *
 * <p>配置见 application*.yaml 的 auth.password 节点；expire-days=0 表示不启用有效期。
 * pwd_update_time 为空（存量账号未记录）视为不过期，改密后开始生效。
 */
@Component
public class PasswordPolicy {

	@Value("${auth.password.min-length:8}")
	private int minLength;

	/** 是否要求同时包含字母和数字 */
	@Value("${auth.password.require-letter-digit:true}")
	private boolean requireLetterDigit;

	/** 是否要求包含特殊字符 */
	@Value("${auth.password.require-special:false}")
	private boolean requireSpecial;

	/** 密码有效期（天），0 = 不启用 */
	@Value("${auth.password.expire-days:0}")
	private int expireDays;

	/** 校验明文密码复杂度，不满足抛业务异常 */
	public void validate(String rawPassword) {
		if (rawPassword == null || rawPassword.length() < minLength) {
			throw new BusinessException("密码长度不能少于 " + minLength + " 位");
		}
		if (rawPassword.length() > 64) {
			throw new BusinessException("密码长度不能超过 64 位");
		}
		if (requireLetterDigit && !(rawPassword.matches(".*[A-Za-z].*") && rawPassword.matches(".*[0-9].*"))) {
			throw new BusinessException("密码必须同时包含字母和数字");
		}
		if (requireSpecial && rawPassword.matches("[A-Za-z0-9]*")) {
			throw new BusinessException("密码必须包含特殊字符");
		}
	}

	/** 校验密码是否过期；未记录更新时间的账号跳过 */
	public void checkNotExpired(LocalDateTime pwdUpdateTime) {
		if (expireDays <= 0 || pwdUpdateTime == null) {
			return;
		}
		long days = ChronoUnit.DAYS.between(pwdUpdateTime, LocalDateTime.now());
		if (days >= expireDays) {
			throw new BusinessException(ErrorCode.USER_PASSWORD_EXPIRED,
					String.format("密码已使用超过 %d 天，请联系管理员重置密码", expireDays));
		}
	}

}
