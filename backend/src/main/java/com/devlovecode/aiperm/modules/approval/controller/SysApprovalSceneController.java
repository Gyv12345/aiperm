package com.devlovecode.aiperm.modules.approval.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.approval.dto.ApprovalSceneDTO;
import com.devlovecode.aiperm.modules.approval.entity.SysApprovalScene;
import com.devlovecode.aiperm.modules.approval.service.ApprovalSceneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "审批场景管理")
@RestController
@RequestMapping("/system/approval-scene")
@SaCheckLogin
@RequiredArgsConstructor
public class SysApprovalSceneController {

    private final ApprovalSceneService approvalSceneService;

    @Operation(summary = "分页查询审批场景")
    @SaCheckPermission("system:approval-scene:list")
    @GetMapping
    public R<PageResult<SysApprovalScene>> list(@Validated({Default.class, Views.Query.class}) ApprovalSceneDTO dto) {
        return R.ok(approvalSceneService.queryPage(dto));
    }

    @Operation(summary = "查询审批场景详情")
    @SaCheckPermission("system:approval-scene:list")
    @GetMapping("/{id}")
    public R<SysApprovalScene> detail(@PathVariable Long id) {
        return R.ok(approvalSceneService.findById(id));
    }

    @Operation(summary = "创建审批场景")
    @SaCheckPermission("system:approval-scene:create")
    @Log(title = "审批场景管理", operType = OperType.CREATE)
    @PostMapping
    public R<Void> create(@RequestBody @Validated({Default.class, Views.Create.class}) ApprovalSceneDTO dto) {
        approvalSceneService.create(dto);
        return R.ok();
    }

    @Operation(summary = "更新审批场景")
    @SaCheckPermission("system:approval-scene:update")
    @Log(title = "审批场景管理", operType = OperType.UPDATE)
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody @Validated({Default.class, Views.Update.class}) ApprovalSceneDTO dto) {
        approvalSceneService.update(id, dto);
        return R.ok();
    }

    @Operation(summary = "删除审批场景")
    @SaCheckPermission("system:approval-scene:delete")
    @Log(title = "审批场景管理", operType = OperType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        approvalSceneService.delete(id);
        return R.ok();
    }
}
