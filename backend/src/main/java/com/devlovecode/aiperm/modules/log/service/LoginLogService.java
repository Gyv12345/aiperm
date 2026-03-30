package com.devlovecode.aiperm.modules.log.service;

import com.devlovecode.aiperm.common.util.ClientIpUtils;
import com.devlovecode.aiperm.modules.log.entity.SysLoginLog;
import com.devlovecode.aiperm.modules.log.repository.LoginLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogService {

	private static final String UNKNOWN_VALUE = "未知";

	private static final String INTERNAL_IP = "内网IP";

	private static final long LOCATION_CACHE_TTL_MILLIS = 24 * 60 * 60 * 1000L;

	private static final String GEO_API_TEMPLATE = "http://ip-api.com/json/%s?lang=zh-CN";

	private static final List<UserAgentRule> BROWSER_RULES = List.of(
			new UserAgentRule(Pattern.compile("edg(?:e|ios|a)?/", Pattern.CASE_INSENSITIVE), "Edge"),
			new UserAgentRule(Pattern.compile("(opr/|opera)", Pattern.CASE_INSENSITIVE), "Opera"),
			new UserAgentRule(Pattern.compile("chrome/", Pattern.CASE_INSENSITIVE), "Chrome"),
			new UserAgentRule(Pattern.compile("firefox/", Pattern.CASE_INSENSITIVE), "Firefox"),
			new UserAgentRule(Pattern.compile("version/.*safari/", Pattern.CASE_INSENSITIVE), "Safari"),
			new UserAgentRule(Pattern.compile("(msie |trident/)", Pattern.CASE_INSENSITIVE), "Internet Explorer"));

	private static final List<UserAgentRule> OS_RULES = List.of(
			new UserAgentRule(Pattern.compile("(iphone|ipad|ipod|cpu iphone os)", Pattern.CASE_INSENSITIVE), "iOS"),
			new UserAgentRule(Pattern.compile("android", Pattern.CASE_INSENSITIVE), "Android"),
			new UserAgentRule(Pattern.compile("windows", Pattern.CASE_INSENSITIVE), "Windows"),
			new UserAgentRule(Pattern.compile("(mac os x|macintosh)", Pattern.CASE_INSENSITIVE), "macOS"),
			new UserAgentRule(Pattern.compile("linux", Pattern.CASE_INSENSITIVE), "Linux"));

	private final LoginLogRepository loginLogRepository;

	private final RestTemplate restTemplate;

	private final ConcurrentHashMap<String, LocationCacheEntry> locationCache = new ConcurrentHashMap<>();

	/**
	 * 记录登录成功日志
	 */
	public void recordSuccess(Long userId, String username, String ip) {
		HttpServletRequest currentRequest = getCurrentRequest();
		recordSuccess(userId, username, ip, resolveUserAgent(null, currentRequest), currentRequest);
	}

	/**
	 * 记录登录成功日志
	 */
	public void recordSuccess(Long userId, String username, String ip, String userAgent, HttpServletRequest request) {
		record(userId, username, ip, 0, "登录成功", userAgent, request);
	}

	/**
	 * 记录登录失败日志
	 */
	public void recordFailed(String username, String ip, String message) {
		HttpServletRequest currentRequest = getCurrentRequest();
		recordFailed(username, ip, message, resolveUserAgent(null, currentRequest), currentRequest);
	}

	/**
	 * 记录登录失败日志
	 */
	public void recordFailed(String username, String ip, String message, String userAgent, HttpServletRequest request) {
		record(null, username, ip, 1, message, userAgent, request);
	}

	private void record(Long userId, String username, String ip, Integer status, String message, String userAgent,
			HttpServletRequest request) {
		String resolvedIp = resolveClientIp(ip, request);
		String resolvedUserAgent = resolveUserAgent(userAgent, request);

		CompletableFuture.runAsync(() -> persistLog(userId, username, resolvedIp, status, message, resolvedUserAgent));
	}

	private void persistLog(Long userId, String username, String ip, Integer status, String message, String userAgent) {
		try {
			SysLoginLog logRecord = new SysLoginLog();
			logRecord.setUserId(userId);
			logRecord.setUsername((username == null || username.isBlank()) ? "-" : username);
			logRecord.setIp((ip == null || ip.isBlank()) ? "unknown" : ip);
			logRecord.setLocation(resolveLocation(ip));
			logRecord.setBrowser(resolveBrowser(userAgent));
			logRecord.setOs(resolveOs(userAgent));
			logRecord.setStatus(status);
			logRecord.setMsg(message);
			logRecord.setLoginTime(LocalDateTime.now());
			logRecord.setDeleted(0);
			logRecord.setCreateTime(LocalDateTime.now());
			loginLogRepository.save(logRecord);
		}
		catch (Exception e) {
			log.warn("记录登录日志失败: username={}, ip={}", username, ip, e);
		}
	}

	private String resolveClientIp(String ip, HttpServletRequest request) {
		if (ip != null && !ip.isBlank()) {
			return ip;
		}
		return ClientIpUtils.getClientIp(request);
	}

	private String resolveUserAgent(String userAgent, HttpServletRequest request) {
		if (userAgent != null && !userAgent.isBlank()) {
			return userAgent;
		}
		if (request == null) {
			return "";
		}
		String header = request.getHeader(HttpHeaders.USER_AGENT);
		return header == null ? "" : header.trim();
	}

	private String resolveLocation(String ip) {
		if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
			return UNKNOWN_VALUE;
		}

		if (isPrivateOrLocalIp(ip)) {
			return INTERNAL_IP;
		}

		LocationCacheEntry cached = locationCache.get(ip);
		long now = System.currentTimeMillis();
		if (cached != null && cached.expiresAt() > now) {
			return cached.location();
		}
		if (cached != null) {
			locationCache.remove(ip);
		}

		String location = fetchLocation(ip);
		locationCache.put(ip, new LocationCacheEntry(location, now + LOCATION_CACHE_TTL_MILLIS));
		return location;
	}

	private String fetchLocation(String ip) {
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> response = restTemplate.getForObject(GEO_API_TEMPLATE.formatted(ip), Map.class);
			if (response == null || !Objects.equals("success", response.get("status"))) {
				return UNKNOWN_VALUE;
			}

			String country = asText(response.get("country"));
			String region = asText(response.get("regionName"));
			String city = asText(response.get("city"));

			return List.of(country, region, city)
				.stream()
				.filter(value -> value != null && !value.isBlank())
				.distinct()
				.reduce((left, right) -> left + " " + right)
				.orElse(UNKNOWN_VALUE);
		}
		catch (Exception e) {
			log.debug("IP 定位失败: ip={}", ip, e);
			return UNKNOWN_VALUE;
		}
	}

	private String resolveBrowser(String userAgent) {
		return matchUserAgent(userAgent, BROWSER_RULES);
	}

	private String resolveOs(String userAgent) {
		return matchUserAgent(userAgent, OS_RULES);
	}

	private String matchUserAgent(String userAgent, List<UserAgentRule> rules) {
		if (userAgent == null || userAgent.isBlank()) {
			return UNKNOWN_VALUE;
		}

		return rules.stream()
			.filter(rule -> rule.pattern().matcher(userAgent).find())
			.map(UserAgentRule::name)
			.findFirst()
			.orElse(UNKNOWN_VALUE);
	}

	private boolean isPrivateOrLocalIp(String ip) {
		try {
			InetAddress address = InetAddress.getByName(ip);
			return address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isSiteLocalAddress()
					|| address.isLinkLocalAddress();
		}
		catch (Exception e) {
			return false;
		}
	}

	private HttpServletRequest getCurrentRequest() {
		RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
			return servletRequestAttributes.getRequest();
		}
		return null;
	}

	private String asText(Object value) {
		return value == null ? "" : value.toString().trim();
	}

	private record UserAgentRule(Pattern pattern, String name) {
	}

	private record LocationCacheEntry(String location, long expiresAt) {
	}

}
