package com.devlovecode.aiperm.modules.audit.service;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.common.util.ClientIpUtils;
import com.devlovecode.aiperm.common.util.ExcelExportHelper;
import com.devlovecode.aiperm.common.util.UserAgentUtils;
import com.devlovecode.aiperm.modules.audit.entity.SysLoginLog;
import com.devlovecode.aiperm.modules.audit.export.LoginLogExportModel;
import com.devlovecode.aiperm.modules.audit.repository.LoginLogRepository;
import com.devlovecode.aiperm.modules.audit.vo.LoginLogVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogService {

	private static final String UNKNOWN_VALUE = "未知";

	private static final String INTERNAL_IP = "内网IP";

	private static final long LOCATION_CACHE_TTL_MILLIS = 24 * 60 * 60 * 1000L;

	private static final String GEO_API_TEMPLATE = "http://ip-api.com/json/%s?lang=zh-CN";

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final LoginLogRepository loginLogRepository;

	private final RestTemplate restTemplate;

	private final ExcelExportHelper excelExportHelper;

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

	public PageResult<LoginLogVO> queryPage(String username, Integer status, String ip, LocalDate startDate,
			LocalDate endDate, int page, int pageSize) {
		Specification<SysLoginLog> specification = SpecificationUtils.and(SpecificationUtils.like("username", username),
				SpecificationUtils.eq("status", status), SpecificationUtils.like("ip", ip),
				SpecificationUtils.ge("loginTime", startDate == null ? null : startDate.atStartOfDay()),
				SpecificationUtils.le("loginTime", endDate == null ? null : endDate.atTime(23, 59, 59)));
		Page<SysLoginLog> jpaPage = loginLogRepository.findAll(specification,
				PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "loginTime")));
		return PageResult.fromJpaPage(jpaPage).map(this::toVO);
	}

	public void export(String username, Integer status, String ip, LocalDate startDate, LocalDate endDate,
			HttpServletResponse response) {
		List<LoginLogExportModel> rows = queryPage(username, status, ip, startDate, endDate, 1, Integer.MAX_VALUE)
			.getList()
			.stream()
			.map(this::toExportModel)
			.toList();
		excelExportHelper.export(response, "login-logs", LoginLogExportModel.class, rows);
	}

	@Transactional
	public void delete(Long id) {
		if (!loginLogRepository.existsById(id)) {
			throw new BusinessException("登录日志不存在");
		}
		loginLogRepository.softDelete(id);
	}

	@Transactional
	public void clean() {
		loginLogRepository.softDeleteAll();
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
			logRecord.setBrowser(UserAgentUtils.resolveBrowser(userAgent));
			logRecord.setOs(UserAgentUtils.resolveOs(userAgent));
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

	private LoginLogVO toVO(SysLoginLog entity) {
		LoginLogVO vo = new LoginLogVO();
		vo.setId(entity.getId());
		vo.setUserId(entity.getUserId());
		vo.setUsername(entity.getUsername());
		vo.setIp(entity.getIp());
		vo.setLocation(entity.getLocation());
		vo.setBrowser(entity.getBrowser());
		vo.setOs(entity.getOs());
		vo.setStatus(entity.getStatus());
		vo.setMsg(entity.getMsg());
		vo.setLoginTime(entity.getLoginTime());
		return vo;
	}

	private LoginLogExportModel toExportModel(LoginLogVO vo) {
		LoginLogExportModel model = new LoginLogExportModel();
		model.setUsername(vo.getUsername());
		model.setIp(vo.getIp());
		model.setLocation(vo.getLocation());
		model.setBrowser(vo.getBrowser());
		model.setOs(vo.getOs());
		model.setStatusText(vo.getStatus() != null && vo.getStatus() == 0 ? "成功" : "失败");
		model.setMsg(vo.getMsg());
		model.setLoginTime(vo.getLoginTime() == null ? "" : vo.getLoginTime().format(DATE_TIME_FORMATTER));
		return model;
	}

	private record LocationCacheEntry(String location, long expiresAt) {
	}

}
