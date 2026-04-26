package com.devlovecode.aiperm.modules.approval.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.approval.dto.ApprovalInstanceDTO;
import com.devlovecode.aiperm.modules.approval.dto.ApprovalSubmitDTO;
import com.devlovecode.aiperm.modules.approval.service.ApprovalBridgeService;
import com.devlovecode.aiperm.modules.approval.vo.ApprovalInstanceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "审批实例")
@RestController
@RequestMapping("/approval/instance")
@SaCheckLogin
@RequiredArgsConstructor
public class ApprovalInstanceController {

	private final ApprovalBridgeService approvalBridgeService;

	@Operation(summary = "分页查询我发起的审批")
	@SaCheckPermission("approval:instance:list")
	@GetMapping
	public R<PageResult<ApprovalInstanceVO>> page(
			@Validated({ Default.class, Views.Query.class }) ApprovalInstanceDTO dto) {
		return R.ok(approvalBridgeService.queryMyPage(dto));
	}

	@Operation(summary = "通用提交审批")
	@SaCheckPermission("approval:instance:submit")
	@Log(title = "审批实例", operType = OperType.CREATE)
	@PostMapping
	public R<Long> submit(@RequestBody @Validated({ Default.class, Views.Create.class }) ApprovalSubmitDTO dto) {
		return R.ok(approvalBridgeService.submit(dto));
	}

}
