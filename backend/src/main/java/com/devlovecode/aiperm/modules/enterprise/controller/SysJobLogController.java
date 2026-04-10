package com.devlovecode.aiperm.modules.enterprise.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.enterprise.service.JobLogService;
import com.devlovecode.aiperm.modules.enterprise.vo.JobLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Tag(name = "任务日志管理")
@RestController
@RequestMapping("/monitor/job-log")
@SaCheckLogin
@RequiredArgsConstructor
public class SysJobLogController {

	private final JobLogService jobLogService;

	@Operation(summary = "分页查询任务日志")
	@SaCheckPermission("monitor:job-log:list")
	@GetMapping
	public R<PageResult<JobLogVO>> page(@RequestParam(name = "page", defaultValue = "1") Integer page,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
			@RequestParam(name = "jobName", required = false) String jobName,
			@RequestParam(name = "status", required = false) Integer status,
			@RequestParam(name = "triggerSource", required = false) String triggerSource,
			@RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
			@RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
		return R.ok(jobLogService.queryPage(jobName, status, triggerSource, startDate == null ? null : startDate.atStartOfDay(),
				endDate == null ? null : endDate.atTime(23, 59, 59), page, pageSize));
	}

	@Operation(summary = "导出任务日志")
	@SaCheckPermission("monitor:job-log:export")
	@Log(title = "任务日志管理", operType = OperType.EXPORT)
	@GetMapping("/export")
	public void export(@RequestParam(name = "jobName", required = false) String jobName,
			@RequestParam(name = "status", required = false) Integer status,
			@RequestParam(name = "triggerSource", required = false) String triggerSource,
			@RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
			@RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
			HttpServletResponse response) {
		jobLogService.export(jobName, status, triggerSource, startDate == null ? null : startDate.atStartOfDay(),
				endDate == null ? null : endDate.atTime(23, 59, 59), response);
	}

	@Operation(summary = "删除任务日志")
	@SaCheckPermission("monitor:job-log:delete")
	@Log(title = "任务日志管理", operType = OperType.DELETE)
	@DeleteMapping("/{id}")
	public R<Void> delete(@PathVariable Long id) {
		jobLogService.delete(id);
		return R.ok();
	}

	@Operation(summary = "清空任务日志")
	@SaCheckPermission("monitor:job-log:delete")
	@Log(title = "任务日志管理", operType = OperType.DELETE)
	@DeleteMapping("/clean")
	public R<Void> clean() {
		jobLogService.clean();
		return R.ok();
	}

}
