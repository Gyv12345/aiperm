package com.devlovecode.aiperm.modules.dashboard.service;

import com.devlovecode.aiperm.modules.dashboard.vo.DashboardStatsVO;
import com.devlovecode.aiperm.modules.monitor.api.OnlineSessionApi;
import com.devlovecode.aiperm.modules.system.api.SystemAccess;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

	private final SystemAccess systemAccess;

	private final OnlineSessionApi onlineSessionApi;

	/**
	 * 获取首页统计数据
	 */
	public DashboardStatsVO getStats() {
		DashboardStatsVO vo = new DashboardStatsVO();
		vo.setUserCount(systemAccess.countUsers());
		vo.setRoleCount(systemAccess.countRoles());
		vo.setMenuCount(systemAccess.countMenus());
		vo.setOnlineCount(getOnlineCount());
		return vo;
	}

	private Long getOnlineCount() {
		try {
			return onlineSessionApi.countActiveSessions();
		}
		catch (Exception e) {
			log.warn("获取在线用户数失败: {}", e.getMessage());
			return 0L;
		}
	}

}
