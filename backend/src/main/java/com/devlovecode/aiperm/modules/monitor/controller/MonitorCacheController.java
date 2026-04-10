package com.devlovecode.aiperm.modules.monitor.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.modules.monitor.service.MonitorCacheService;
import com.devlovecode.aiperm.modules.monitor.vo.CacheMonitorVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "缓存监控")
@RestController
@RequestMapping("/monitor/cache")
@SaCheckLogin
@RequiredArgsConstructor
public class MonitorCacheController {

	private final MonitorCacheService monitorCacheService;

	@Operation(summary = "获取缓存监控概览")
	@SaCheckPermission("monitor:cache:list")
	@GetMapping
	public R<CacheMonitorVO> overview() {
		return R.ok(monitorCacheService.getOverview());
	}

}
