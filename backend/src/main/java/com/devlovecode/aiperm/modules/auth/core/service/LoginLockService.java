package com.devlovecode.aiperm.modules.auth.core.service;

import com.devlovecode.aiperm.common.enums.ErrorCode;
import com.devlovecode.aiperm.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 登录失败锁定：同一用户名在统计窗口内连续登录失败达到阈值后，临时锁定一段时间。
 *
 * <p>计数与锁定状态存 Redis，多实例天然共享；仅在密码登录路径使用
 * （验证码登录本身有时效与尝试次数约束，不走此逻辑）。
 *
 * <p>配置见 application*.yaml 的 auth.lockout 节点。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LoginLockService {

	private static final String FAIL_KEY_PREFIX = "auth:lock:fail:";

	private static final String LOCK_KEY_PREFIX = "auth:lock:user:";

	private final StringRedisTemplate redis;

	@Value("${auth.lockout.enabled:true}")
	private boolean enabled;

	/** 触发锁定所需的窗口内连续失败次数 */
	@Value("${auth.lockout.max-attempts:5}")
	private int maxAttempts;

	/** 失败计数窗口（分钟），窗口内未再次失败则计数自动过期 */
	@Value("${auth.lockout.window-minutes:15}")
	private long windowMinutes;

	/** 锁定时长（分钟） */
	@Value("${auth.lockout.lock-minutes:30}")
	private long lockMinutes;

	/** 登录前检查：处于锁定期则拒绝，并提示剩余约几分钟 */
	public void checkLocked(String username) {
		if (!enabled) {
			return;
		}
		String lockKey = LOCK_KEY_PREFIX + username.toLowerCase();
		if (Boolean.TRUE.equals(redis.hasKey(lockKey))) {
			Long ttl = redis.getExpire(lockKey);
			long minutes = ttl == null || ttl < 0 ? lockMinutes : (long) Math.ceil(ttl / 60.0);
			throw new BusinessException(ErrorCode.USER_ACCOUNT_LOCKED,
					String.format("登录失败次数过多，账号已锁定，请约 %d 分钟后重试", Math.max(minutes, 1)));
		}
	}

	/** 密码校验失败时记一次；窗口内累计达到阈值则锁定并清空计数 */
	public void recordFailure(String username) {
		if (!enabled) {
			return;
		}
		String failKey = FAIL_KEY_PREFIX + username.toLowerCase();
		Long count = redis.opsForValue().increment(failKey);
		if (count == null) {
			return;
		}
		if (count == 1) {
			redis.expire(failKey, Duration.ofMinutes(windowMinutes));
		}
		if (count >= maxAttempts) {
			redis.opsForValue().set(LOCK_KEY_PREFIX + username.toLowerCase(), String.valueOf(count),
					Duration.ofMinutes(lockMinutes));
			redis.delete(failKey);
			log.warn("用户名 {} 在 {} 分钟窗口内连续登录失败 {} 次，已临时锁定 {} 分钟", username, windowMinutes, count, lockMinutes);
		}
	}

	/** 登录成功后清除失败计数 */
	public void resetFailures(String username) {
		if (!enabled) {
			return;
		}
		redis.delete(FAIL_KEY_PREFIX + username.toLowerCase());
	}

}
