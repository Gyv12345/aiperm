package com.devlovecode.aiperm.modules.dashboard.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.modules.dashboard.service.DashboardService;
import com.devlovecode.aiperm.modules.dashboard.vo.DashboardStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "首页仪表盘")
@RestController
@RequestMapping("/dashboard")
@SaCheckLogin
@RequiredArgsConstructor
public class DashboardController {

	private final DashboardService dashboardService;

	@Operation(summary = "获取统计数据")
	@GetMapping("/stats")
	public R<DashboardStatsVO> getStats() {
		return R.ok(dashboardService.getStats());
	}

}
