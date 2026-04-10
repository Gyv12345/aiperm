package com.devlovecode.aiperm.modules.audit.api;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.modules.audit.entity.SysLoginLog;
import com.devlovecode.aiperm.modules.audit.repository.LoginLogRepository;
import com.devlovecode.aiperm.modules.audit.service.LoginLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginAuditApi {

	private final LoginLogService loginLogService;

	private final LoginLogRepository loginLogRepository;

	public void recordSuccess(Long userId, String username, String ip, String userAgent, HttpServletRequest request) {
		loginLogService.recordSuccess(userId, username, ip, userAgent, request);
	}

	public void recordSuccess(Long userId, String username, String ip) {
		loginLogService.recordSuccess(userId, username, ip);
	}

	public void recordFailed(String username, String ip, String message, String userAgent, HttpServletRequest request) {
		loginLogService.recordFailed(username, ip, message, userAgent, request);
	}

	public PageResult<AuditLoginLogRecord> queryUserLoginLogs(Long userId, String username, int pageNum, int pageSize) {
		return PageResult.fromJpaPage(loginLogRepository.queryPageByUser(userId, username, pageNum, pageSize))
			.map(this::toRecord);
	}

	private AuditLoginLogRecord toRecord(SysLoginLog entity) {
		return new AuditLoginLogRecord(entity.getId(), entity.getUserId(), entity.getUsername(), entity.getIp(),
				entity.getLocation(), entity.getBrowser(), entity.getOs(), entity.getStatus(), entity.getMsg(),
				entity.getLoginTime());
	}

}
