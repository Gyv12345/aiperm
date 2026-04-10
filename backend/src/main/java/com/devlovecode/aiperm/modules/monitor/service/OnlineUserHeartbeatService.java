package com.devlovecode.aiperm.modules.monitor.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.modules.monitor.repository.OnlineUserRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnlineUserHeartbeatService {

	private static final String HEARTBEAT_KEY = "aiperm:online-user:heartbeat";

	private static final String DIRTY_HEARTBEAT_KEY = "aiperm:online-user:heartbeat:dirty";

	private static final Duration SYNC_INTERVAL = Duration.ofSeconds(15);

	private static final long HEARTBEAT_EXPIRE_SECONDS = 35 * 60L;

	private final StringRedisTemplate redisTemplate;

	private final OnlineUserRepository onlineUserRepo;

	@Qualifier("onlineUserTaskScheduler")
	private final ThreadPoolTaskScheduler onlineUserTaskScheduler;

	private final TransactionTemplate transactionTemplate;

	private volatile ScheduledFuture<?> syncFuture;

	@EventListener(ApplicationReadyEvent.class)
	public void initOnStartup() {
		if (syncFuture != null) {
			return;
		}
		syncFuture = onlineUserTaskScheduler.scheduleWithFixedDelay(this::syncDirtyHeartbeatsSafely, SYNC_INTERVAL);
		log.info("在线用户心跳同步任务已启动，interval={}s", SYNC_INTERVAL.getSeconds());
	}

	@PreDestroy
	public void destroy() {
		if (syncFuture != null) {
			syncFuture.cancel(false);
		}
	}

	public void recordHeartbeat(String token, LocalDateTime lastAccessTime) {
		if (!hasText(token)) {
			return;
		}
		double score = toScore(lastAccessTime == null ? LocalDateTime.now() : lastAccessTime);
		try {
			redisTemplate.opsForZSet().add(HEARTBEAT_KEY, token, score);
			redisTemplate.opsForZSet().add(DIRTY_HEARTBEAT_KEY, token, score);
		}
		catch (Exception e) {
			log.warn("在线用户心跳写入 Redis 失败，token={}", maskToken(token), e);
		}
	}

	public void removeHeartbeat(String token) {
		if (!hasText(token)) {
			return;
		}
		try {
			removeTokenInternal(token);
		}
		catch (Exception e) {
			log.warn("在线用户心跳从 Redis 移除失败，token={}", maskToken(token), e);
		}
	}

	public void flushToDatabase() {
		syncDirtyHeartbeatsSafely();
	}

	private void syncDirtyHeartbeatsSafely() {
		try {
			syncDirtyHeartbeats();
		}
		catch (Exception e) {
			log.warn("在线用户心跳同步失败", e);
		}
	}

	private void syncDirtyHeartbeats() {
		LocalDateTime now = LocalDateTime.now();
		pruneExpiredHeartbeats(now);

		Set<ZSetOperations.TypedTuple<String>> dirtyEntries = redisTemplate.opsForZSet()
			.rangeWithScores(DIRTY_HEARTBEAT_KEY, 0, -1);
		if (dirtyEntries == null || dirtyEntries.isEmpty()) {
			return;
		}

		for (ZSetOperations.TypedTuple<String> entry : dirtyEntries) {
			String token = entry.getValue();
			if (!hasText(token)) {
				continue;
			}

			try {
				if (isTokenInvalid(token)) {
					softDeleteByToken(token, now);
					removeTokenInternal(token);
					continue;
				}

				LocalDateTime lastAccessTime = toLocalDateTime(entry.getScore(), now);
				Integer updated = transactionTemplate.execute(
						status -> onlineUserRepo.touchByToken(token, lastAccessTime, lastAccessTime));
				if (updated == null || updated == 0) {
					removeTokenInternal(token);
					continue;
				}

				redisTemplate.opsForZSet().remove(DIRTY_HEARTBEAT_KEY, token);
			}
			catch (Exception e) {
				log.warn("同步在线用户心跳失败，token={}", maskToken(token), e);
			}
		}
	}

	private void pruneExpiredHeartbeats(LocalDateTime now) {
		double expireBefore = toScore(now.minusSeconds(HEARTBEAT_EXPIRE_SECONDS));
		Set<String> expiredTokens = redisTemplate.opsForZSet().rangeByScore(HEARTBEAT_KEY, 0, expireBefore);
		if (expiredTokens == null || expiredTokens.isEmpty()) {
			return;
		}

		for (String token : expiredTokens) {
			if (!hasText(token)) {
				continue;
			}
			try {
				softDeleteByToken(token, now);
				removeTokenInternal(token);
			}
			catch (Exception e) {
				log.warn("清理过期在线用户心跳失败，token={}", maskToken(token), e);
			}
		}
	}

	private void softDeleteByToken(String token, LocalDateTime updateTime) {
		transactionTemplate.executeWithoutResult(status -> onlineUserRepo.softDeleteByToken(token, updateTime));
	}

	private boolean isTokenInvalid(String token) {
		try {
			Object loginId = StpUtil.getLoginIdByToken(token);
			long timeout = StpUtil.getTokenTimeout(token);
			return loginId == null || timeout == -2;
		}
		catch (Exception e) {
			return true;
		}
	}

	private void removeTokenInternal(String token) {
		redisTemplate.opsForZSet().remove(HEARTBEAT_KEY, token);
		redisTemplate.opsForZSet().remove(DIRTY_HEARTBEAT_KEY, token);
	}

	private double toScore(LocalDateTime time) {
		return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	private LocalDateTime toLocalDateTime(Double score, LocalDateTime fallback) {
		if (score == null) {
			return fallback;
		}
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(score.longValue()), ZoneId.systemDefault());
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String maskToken(String token) {
		if (!hasText(token)) {
			return "unknown";
		}
		if (token.length() <= 8) {
			return token;
		}
		return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
	}

}
