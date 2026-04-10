package com.devlovecode.aiperm.modules.monitor.service;

import com.devlovecode.aiperm.modules.monitor.vo.CacheEntryVO;
import com.devlovecode.aiperm.modules.monitor.vo.CacheMonitorVO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
public class MonitorCacheService {

	private final CacheManager cacheManager;

	private final RedisConnectionFactory redisConnectionFactory;

	public CacheMonitorVO getOverview() {
		CacheMonitorVO vo = new CacheMonitorVO();
		try (RedisConnection connection = redisConnectionFactory.getConnection()) {
			Properties memoryInfo = connection.serverCommands().info("memory");
			Properties statsInfo = connection.serverCommands().info("stats");
			Properties clientsInfo = connection.serverCommands().info("clients");
			Properties keyspaceInfo = connection.serverCommands().info("keyspace");

			vo.setUsedMemoryHuman(readString(memoryInfo, "used_memory_human"));
			vo.setConnectedClients(readLong(clientsInfo, "connected_clients"));
			vo.setHits(readLong(statsInfo, "keyspace_hits"));
			vo.setMisses(readLong(statsInfo, "keyspace_misses"));
			vo.setTotalKeys(parseTotalKeys(keyspaceInfo));
			vo.setHitRate(calculateHitRate(vo.getHits(), vo.getMisses()));
			vo.setEntries(buildCacheEntries(connection));
		}
		return vo;
	}

	private List<CacheEntryVO> buildCacheEntries(RedisConnection connection) {
		Set<String> cacheNames = new TreeSet<>(cacheManager.getCacheNames());
		List<CacheEntryVO> entries = new ArrayList<>();
		for (String cacheName : cacheNames) {
			String prefix = cacheName + "::";
			Set<byte[]> keys = connection.keyCommands().keys((prefix + "*").getBytes(StandardCharsets.UTF_8));
			CacheEntryVO entry = new CacheEntryVO();
			entry.setCacheName(cacheName);
			entry.setKeyPrefix(prefix);
			entry.setEstimatedSize(keys == null ? 0L : (long) keys.size());
			entry.setSampleTtl(resolveSampleTtl(connection, keys));
			entries.add(entry);
		}
		return entries;
	}

	private Long resolveSampleTtl(RedisConnection connection, Set<byte[]> keys) {
		if (keys == null || keys.isEmpty()) {
			return 0L;
		}
		byte[] firstKey = keys.iterator().next();
		Long ttl = connection.keyCommands().ttl(firstKey);
		return ttl == null || ttl < 0 ? 0L : ttl;
	}

	private long parseTotalKeys(Properties keyspaceInfo) {
		if (keyspaceInfo == null || keyspaceInfo.isEmpty()) {
			return 0L;
		}
		long total = 0L;
		for (String name : keyspaceInfo.stringPropertyNames()) {
			String value = keyspaceInfo.getProperty(name);
			if (value == null || !value.contains("keys=")) {
				continue;
			}
			for (String segment : value.split(",")) {
				if (segment.startsWith("keys=")) {
					total += Long.parseLong(segment.substring(5));
				}
			}
		}
		return total;
	}

	private double calculateHitRate(Long hits, Long misses) {
		long total = (hits == null ? 0L : hits) + (misses == null ? 0L : misses);
		if (total <= 0) {
			return 0D;
		}
		return ((double) (hits == null ? 0L : hits)) / total;
	}

	private String readString(Properties properties, String key) {
		return properties == null ? "" : properties.getProperty(key, "");
	}

	private Long readLong(Properties properties, String key) {
		if (properties == null) {
			return 0L;
		}
		String value = properties.getProperty(key);
		if (value == null || value.isBlank()) {
			return 0L;
		}
		try {
			return Long.parseLong(value);
		}
		catch (NumberFormatException e) {
			return 0L;
		}
	}

}
