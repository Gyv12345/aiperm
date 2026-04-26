package com.devlovecode.aiperm.modules.approval.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.modules.approval.service.ApprovalBridgeService;
import com.devlovecode.aiperm.modules.approval.vo.ApprovalTodoOverviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "待我审批总览")
@RestController
@RequestMapping("/approval/todo")
@SaCheckLogin
@RequiredArgsConstructor
public class ApprovalTodoController {

	private final ApprovalBridgeService approvalBridgeService;

	@Operation(summary = "获取待我审批总览")
	@SaCheckPermission("approval:todo:list")
	@GetMapping("/overview")
	public R<ApprovalTodoOverviewVO> overview(@RequestParam(required = false) String platform) {
		return R.ok(approvalBridgeService.getTodoOverview(platform));
	}

}
