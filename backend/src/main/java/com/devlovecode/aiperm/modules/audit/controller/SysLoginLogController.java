package com.devlovecode.aiperm.modules.audit.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.audit.service.LoginLogService;
import com.devlovecode.aiperm.modules.audit.vo.LoginLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "登录日志管理")
@RestController
@RequestMapping("/monitor/login-log")
@SaCheckLogin
@RequiredArgsConstructor
public class SysLoginLogController {

	private final LoginLogService loginLogService;

	@Operation(summary = "分页查询登录日志")
	@SaCheckPermission("monitor:login-log:list")
	@GetMapping
	public R<PageResult<LoginLogVO>> page(@RequestParam(name = "page", defaultValue = "1") Integer page,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
			@RequestParam(name = "username", required = false) String username,
			@RequestParam(name = "status", required = false) Integer status,
			@RequestParam(name = "ip", required = false) String ip,
			@RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
			@RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
		return R.ok(loginLogService.queryPage(username, status, ip, startDate, endDate, page, pageSize));
	}

	@Operation(summary = "导出登录日志")
	@SaCheckPermission("monitor:login-log:export")
	@Log(title = "登录日志管理", operType = OperType.EXPORT)
	@GetMapping("/export")
	public void export(@RequestParam(name = "username", required = false) String username,
			@RequestParam(name = "status", required = false) Integer status,
			@RequestParam(name = "ip", required = false) String ip,
			@RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
			@RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
			HttpServletResponse response) {
		loginLogService.export(username, status, ip, startDate, endDate, response);
	}

	@Operation(summary = "删除登录日志")
	@SaCheckPermission("monitor:login-log:delete")
	@Log(title = "登录日志管理", operType = OperType.DELETE)
	@DeleteMapping("/{id}")
	public R<Void> delete(@PathVariable Long id) {
		loginLogService.delete(id);
		return R.ok();
	}

	@Operation(summary = "清空登录日志")
	@SaCheckPermission("monitor:login-log:delete")
	@Log(title = "登录日志管理", operType = OperType.DELETE)
	@DeleteMapping("/clean")
	public R<Void> clean() {
		loginLogService.clean();
		return R.ok();
	}

}
