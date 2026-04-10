package com.devlovecode.aiperm.modules.monitor.api;

import com.devlovecode.aiperm.modules.monitor.service.OnlineUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OnlineSessionApi {

	private final OnlineUserService onlineUserService;

	public void registerLoginSession(Long userId, String username, String ip, String userAgent) {
		onlineUserService.registerLoginSession(userId, username, ip, userAgent);
	}

	public void logoutCurrentToken() {
		onlineUserService.logoutCurrentToken();
	}

	public long countActiveSessions() {
		return onlineUserService.countActiveSessions();
	}

}
