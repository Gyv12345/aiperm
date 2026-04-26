package com.devlovecode.aiperm.modules.approval.controller;

import com.devlovecode.aiperm.modules.approval.service.ApprovalBridgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "审批回调")
@RestController
@RequestMapping("/approval/callback")
@RequiredArgsConstructor
public class ApprovalCallbackController {

	private final ApprovalBridgeService approvalBridgeService;

	@Operation(summary = "处理外部审批回调")
	@PostMapping("/{platform}")
	public String callback(@PathVariable String platform, @RequestBody(required = false) String body,
			@RequestHeader Map<String, String> headers) {
		return approvalBridgeService.handleCallback(platform, body, headers);
	}

}
