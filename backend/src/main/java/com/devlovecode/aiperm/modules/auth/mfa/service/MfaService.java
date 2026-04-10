package com.devlovecode.aiperm.modules.auth.mfa.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.auth.mfa.entity.SysUserMfa;
import com.devlovecode.aiperm.modules.auth.mfa.repository.UserMfaRepository;
import com.devlovecode.aiperm.modules.auth.mfa.vo.MfaQrcodeVO;
import com.devlovecode.aiperm.modules.auth.mfa.vo.MfaStatusVO;
import com.devlovecode.aiperm.modules.system.api.SystemAccess;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 2FA 服务
 *
 * 使用 Hutool 内置的 GoogleAuthenticator 实现 TOTP 验证
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MfaService {

	private final UserMfaRepository userMfaRepo;

	private final SystemAccess systemAccess;

	private final StringRedisTemplate redisTemplate;

	private static final Long SUPER_ADMIN_ID = 1L;

	private static final String MFA_VERIFIED_PREFIX = "mfa:verified:";

	private static final String MFA_TEMP_PREFIX = "mfa:temp:";

	private static final String APP_NAME = "AIPerm";

	public MfaStatusVO getStatus() {
		Long userId = StpUtil.getLoginIdAsLong();
		boolean bound = userMfaRepo.findByUserId(userId)
			.map(m -> m.getStatus() != null && m.getStatus() == 1)
			.orElse(false);
		boolean required = SUPER_ADMIN_ID.equals(userId);
		boolean verified = isVerified(userId);

		return MfaStatusVO.builder().bound(bound).required(required).verified(verified).build();
	}

	public MfaQrcodeVO generateQrCode() {
		Long userId = StpUtil.getLoginIdAsLong();

		boolean alreadyBound = userMfaRepo.findByUserId(userId)
			.map(m -> m.getStatus() != null && m.getStatus() == 1)
			.orElse(false);
		if (alreadyBound) {
			throw new BusinessException("您已绑定2FA，如需重新绑定请先解绑");
		}

		String secretKey = generateSecretKey();
		String tempKey = MFA_TEMP_PREFIX + userId;
		redisTemplate.opsForValue().set(tempKey, secretKey, 10, TimeUnit.MINUTES);

		String username = StpUtil.getLoginIdAsString();
		String totpUri = "otpauth://totp/" + APP_NAME + ":" + username + "?secret=" + secretKey + "&issuer=" + APP_NAME;

		return MfaQrcodeVO.builder().totpUri(totpUri).secretKey(secretKey).build();
	}

	@Transactional
	public void confirmBind(String code) {
		Long userId = StpUtil.getLoginIdAsLong();

		String tempKey = MFA_TEMP_PREFIX + userId;
		String secretKey = redisTemplate.opsForValue().get(tempKey);
		if (secretKey == null) {
			throw new BusinessException("绑定已超时，请重新获取二维码");
		}

		if (!verifyTotp(secretKey, code)) {
			throw new BusinessException("验证码错误");
		}

		redisTemplate.delete(tempKey);
		userMfaRepo.findByUserId(userId).ifPresent(old -> userMfaRepo.softDelete(old.getId(), LocalDateTime.now()));

		SysUserMfa mfa = new SysUserMfa();
		mfa.setUserId(userId);
		mfa.setMfaType("TOTP");
		mfa.setSecretKey(secretKey);
		mfa.setBindTime(LocalDateTime.now());
		mfa.setStatus(1);
		mfa.setCreateTime(LocalDateTime.now());
		mfa.setCreateBy(StpUtil.getLoginIdAsString());
		userMfaRepo.save(mfa);
	}

	@Transactional
	public void unbind(String code) {
		Long userId = StpUtil.getLoginIdAsLong();

		if (SUPER_ADMIN_ID.equals(userId)) {
			throw new BusinessException("超级管理员不允许解绑2FA");
		}

		SysUserMfa mfa = userMfaRepo.findByUserId(userId).orElseThrow(() -> new BusinessException("您未绑定2FA"));
		if (!verifyTotp(mfa.getSecretKey(), code)) {
			throw new BusinessException("验证码错误");
		}

		userMfaRepo.softDelete(mfa.getId(), LocalDateTime.now());
		redisTemplate.delete(MFA_VERIFIED_PREFIX + userId);
	}

	public void verify(String code) {
		Long userId = StpUtil.getLoginIdAsLong();

		SysUserMfa mfa = userMfaRepo.findByUserId(userId).orElseThrow(() -> new BusinessException("您未绑定2FA，请先绑定"));
		if (!verifyTotp(mfa.getSecretKey(), code)) {
			throw new BusinessException("验证码错误");
		}

		int expireMinutes = systemAccess.getIntConfig("mfa.verify_expire_minutes", 30);
		redisTemplate.opsForValue().set(MFA_VERIFIED_PREFIX + userId, "1", expireMinutes, TimeUnit.MINUTES);
	}

	public boolean isVerified(Long userId) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(MFA_VERIFIED_PREFIX + userId));
	}

	private String generateSecretKey() {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
		StringBuilder sb = new StringBuilder();
		java.util.Random random = new java.util.Random();
		for (int i = 0; i < 16; i++) {
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}
		return sb.toString();
	}

	private boolean verifyTotp(String secretKey, String code) {
		try {
			if (code == null || code.length() != 6) {
				return false;
			}

			byte[] key = decodeBase32(secretKey);
			long currentTimeWindow = System.currentTimeMillis() / 1000 / 30;

			for (int i = -1; i <= 1; i++) {
				long timeWindow = currentTimeWindow + i;
				String expectedCode = generateTotp(key, timeWindow);
				if (expectedCode.equals(code)) {
					return true;
				}
			}
			return false;
		}
		catch (Exception e) {
			log.error("TOTP 验证异常", e);
			return false;
		}
	}

	private String generateTotp(byte[] key, long timeWindow) {
		byte[] data = new byte[8];
		long value = timeWindow;
		for (int i = 7; i >= 0; i--) {
			data[i] = (byte) (value & 0xFF);
			value >>= 8;
		}

		javax.crypto.Mac mac;
		try {
			mac = javax.crypto.Mac.getInstance("HmacSHA1");
			mac.init(new javax.crypto.spec.SecretKeySpec(key, "HmacSHA1"));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		byte[] hash = mac.doFinal(data);

		int offset = hash[hash.length - 1] & 0x0F;
		int binary = ((hash[offset] & 0x7F) << 24) | ((hash[offset + 1] & 0xFF) << 16)
				| ((hash[offset + 2] & 0xFF) << 8) | (hash[offset + 3] & 0xFF);

		int otp = binary % 1000000;
		return String.format("%06d", otp);
	}

	private byte[] decodeBase32(String base32) {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
		base32 = base32.toUpperCase().replaceAll("[^A-Z2-7]", "");

		int outputLength = base32.length() * 5 / 8;
		byte[] output = new byte[outputLength];

		int buffer = 0;
		int bitsLeft = 0;
		int index = 0;

		for (char c : base32.toCharArray()) {
			int value = chars.indexOf(c);
			if (value < 0) {
				continue;
			}

			buffer = (buffer << 5) | value;
			bitsLeft += 5;

			if (bitsLeft >= 8) {
				output[index++] = (byte) (buffer >> (bitsLeft - 8));
				bitsLeft -= 8;
			}
		}

		return output;
	}

}
