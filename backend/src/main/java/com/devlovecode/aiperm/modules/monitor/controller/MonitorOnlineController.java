package com.devlovecode.aiperm.modules.monitor.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.monitor.service.OnlineUserService;
import com.devlovecode.aiperm.modules.monitor.vo.OnlineUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "在线用户监控")
@RestController
@RequestMapping("/monitor/online")
@SaCheckLogin
@RequiredArgsConstructor
public class MonitorOnlineController {

	private final OnlineUserService onlineUserService;

	@Operation(summary = "分页查询在线用户")
	@SaCheckPermission("monitor:online:list")
	@GetMapping
	public R<PageResult<OnlineUserVO>> page(@RequestParam(name = "page", defaultValue = "1") Integer page,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
			@RequestParam(name = "username", required = false) String username,
			@RequestParam(name = "ip", required = false) String ip) {
		return R.ok(onlineUserService.queryPage(username, ip, page, pageSize));
	}

	@Operation(summary = "导出在线用户")
	@SaCheckPermission("monitor:online:export")
	@Log(title = "在线用户监控", operType = OperType.EXPORT)
	@GetMapping("/export")
	public void export(@RequestParam(name = "username", required = false) String username,
			@RequestParam(name = "ip", required = false) String ip, HttpServletResponse response) {
		onlineUserService.export(username, ip, response);
	}

	@Operation(summary = "强退在线用户")
	@SaCheckPermission("monitor:online:forceLogout")
	@Log(title = "在线用户监控", operType = OperType.DELETE)
	@DeleteMapping("/{id}")
	public R<Void> forceLogout(@PathVariable Long id) {
		onlineUserService.forceLogout(id);
		return R.ok();
	}

	@Operation(summary = "批量强退在线用户")
	@SaCheckPermission("monitor:online:forceLogout")
	@Log(title = "在线用户监控", operType = OperType.DELETE)
	@DeleteMapping("/batch")
	public R<Void> forceLogoutBatch(@RequestBody List<Long> ids) {
		onlineUserService.forceLogoutBatch(ids);
		return R.ok();
	}

}
