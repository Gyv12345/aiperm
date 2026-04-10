package com.devlovecode.aiperm.modules.monitor.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.modules.monitor.service.MonitorServerService;
import com.devlovecode.aiperm.modules.monitor.vo.ServerMonitorVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "服务监控")
@RestController
@RequestMapping("/monitor/server")
@SaCheckLogin
@RequiredArgsConstructor
public class MonitorServerController {

	private final MonitorServerService monitorServerService;

	@Operation(summary = "获取服务监控概览")
	@SaCheckPermission("monitor:server:list")
	@GetMapping
	public R<ServerMonitorVO> overview() {
		return R.ok(monitorServerService.getOverview());
	}

}
