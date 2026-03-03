package com.devlovecode.aiperm.modules.approval.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.approval.dto.ApprovalSubmitDTO;
import com.devlovecode.aiperm.modules.approval.service.ApprovalCallbackService;
import com.devlovecode.aiperm.modules.approval.service.ApprovalService;
import com.devlovecode.aiperm.modules.approval.service.ApprovalTodoOverviewService;
import com.devlovecode.aiperm.modules.approval.vo.ApprovalInstanceVO;
import com.devlovecode.aiperm.modules.approval.vo.ApprovalTodoOverviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "审批中心")
@RestController
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;
    private final ApprovalCallbackService approvalCallbackService;
    private final ApprovalTodoOverviewService approvalTodoOverviewService;

    @Operation(summary = "发起审批")
    @SaCheckLogin
    @SaCheckPermission("approval:instance:submit")
    @Log(title = "审批中心", operType = OperType.CREATE)
    @PostMapping("/submit")
    public R<Void> submit(@RequestBody @Valid ApprovalSubmitDTO dto) {
        approvalService.submit(dto);
        return R.ok();
    }

    @Operation(summary = "查询我的审批")
    @SaCheckLogin
    @SaCheckPermission("approval:instance:list")
    @GetMapping("/my")
    public R<PageResult<ApprovalInstanceVO>> my(
            @RequestParam(required = false) String sceneCode,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(approvalService.queryMyInstances(sceneCode, status, page, pageSize));
    }

    @Operation(summary = "待我审批引导与诊断概览")
    @SaCheckLogin
    @SaCheckPermission("approval:todo:list")
    @GetMapping("/todo/overview")
    public R<ApprovalTodoOverviewVO> todoOverview(@RequestParam(required = false) String platform) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean isAdmin = StpUtil.hasPermission("system:im-config:list")
                || StpUtil.hasPermission("system:approval-scene:list")
                || StpUtil.hasPermission("enterprise:message-log:list");
        return R.ok(approvalTodoOverviewService.buildOverview(userId, isAdmin, platform));
    }

    @Operation(summary = "审批回调入口")
    @PostMapping("/callback/{platform}")
    public String callback(@PathVariable String platform,
                           @RequestBody String body,
                           @RequestHeader Map<String, String> headers) {
        return approvalCallbackService.handle(platform, body, headers);
    }
}
