package com.devlovecode.aiperm.modules.audit.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.audit.entity.SysOperLog;
import com.devlovecode.aiperm.modules.audit.service.OperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "操作日志管理")
@RestController
@RequestMapping("/log/oper")
@SaCheckLogin
@RequiredArgsConstructor
public class SysOperLogController {

	private final OperLogService operLogService;

	@Operation(summary = "分页查询操作日志")
	@SaCheckPermission("log:oper:list")
	@GetMapping
	public R<PageResult<SysOperLog>> page(@RequestParam(name = "page", defaultValue = "1") Integer page,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
			@RequestParam(name = "title", required = false) String title,
			@RequestParam(name = "status", required = false) Integer status,
			@RequestParam(name = "operUser", required = false) String operUser,
			@RequestParam(name = "operIp", required = false) String operIp,
			@RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
			@RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
		return R.ok(operLogService.queryPage(title, status, operUser, operIp, startDate, endDate, page, pageSize));
	}

	@Operation(summary = "查询操作日志详情")
	@SaCheckPermission("log:oper:list")
	@GetMapping("/{id}")
	public R<SysOperLog> getById(@PathVariable Long id) {
		return R.ok(operLogService.findById(id));
	}

	@Operation(summary = "导出操作日志")
	@SaCheckPermission("log:oper:export")
	@Log(title = "操作日志管理", operType = OperType.EXPORT)
	@GetMapping("/export")
	public void export(@RequestParam(name = "title", required = false) String title,
			@RequestParam(name = "status", required = false) Integer status,
			@RequestParam(name = "operUser", required = false) String operUser,
			@RequestParam(name = "operIp", required = false) String operIp,
			@RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
			@RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
			HttpServletResponse response) {
		operLogService.export(title, status, operUser, operIp, startDate, endDate, response);
	}

	@Operation(summary = "删除操作日志")
	@SaCheckPermission("log:oper:delete")
	@Log(title = "操作日志管理", operType = OperType.DELETE)
	@DeleteMapping("/{id}")
	public R<Void> delete(@PathVariable Long id) {
		operLogService.delete(id);
		return R.ok();
	}

	@Operation(summary = "清空操作日志")
	@SaCheckPermission("log:oper:delete")
	@Log(title = "操作日志管理", operType = OperType.DELETE)
	@DeleteMapping("/clean")
	public R<Void> clean() {
		operLogService.clean();
		return R.ok();
	}

}
