package com.devlovecode.aiperm.modules.audit.api;

import java.time.LocalDateTime;

public record AuditLoginLogRecord(Long id, Long userId, String username, String ip, String location, String browser,
		String os, Integer status, String msg, LocalDateTime loginTime) {
}
