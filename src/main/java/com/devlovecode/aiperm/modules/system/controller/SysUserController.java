package com.devlovecode.aiperm.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.modules.system.entity.SysUser;
import com.devlovecode.aiperm.modules.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 用户控制器
 *
 * @author devlovecode
 */
@Slf4j
@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户相关接口")
@Validated
public class SysUserController {

    private final ISysUserService sysUserService;

    @GetMapping("/page")
    @Operation(summary = "分页查询用户列表")
    @SaCheckPermission("system:user:list")
    public R<PageResult<SysUser>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long pageSize,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "手机号") @RequestParam(required = false) String phone,
            @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        PageResult<SysUser> result = sysUserService.page(pageNum, pageSize, username, phone, deptId, status);
        return R.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户")
    @SaCheckPermission("system:user:query")
    public R<SysUser> getById(@Parameter(description = "用户ID") @PathVariable("id") @NotNull Long id) {
        SysUser user = sysUserService.getById(id);
        return R.ok(user);
    }

    @PostMapping
    @Operation(summary = "创建用户")
    @SaCheckPermission("system:user:add")
    public R<Void> create(@Valid @RequestBody SysUser user) {
        boolean success = sysUserService.create(user);
        return success ? R.ok() : R.fail();
    }

    @PutMapping
    @Operation(summary = "更新用户")
    @SaCheckPermission("system:user:edit")
    public R<Void> update(@Valid @RequestBody SysUser user) {
        boolean success = sysUserService.update(user);
        return success ? R.ok() : R.fail();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @SaCheckPermission("system:user:remove")
    public R<Void> delete(@Parameter(description = "用户ID") @PathVariable("id") @NotNull Long id) {
        boolean success = sysUserService.delete(id);
        return success ? R.ok() : R.fail();
    }

    @PutMapping("/reset-password")
    @Operation(summary = "重置用户密码")
    @SaCheckPermission("system:user:resetPwd")
    public R<Void> resetPassword(
            @Parameter(description = "用户ID") @RequestParam @NotNull Long userId,
            @Parameter(description = "新密码") @RequestParam @NotNull String newPassword) {
        boolean success = sysUserService.resetPassword(userId, newPassword);
        return success ? R.ok() : R.fail();
    }

    @PutMapping("/change-status")
    @Operation(summary = "修改用户状态")
    @SaCheckPermission("system:user:edit")
    public R<Void> changeStatus(
            @Parameter(description = "用户ID") @RequestParam @NotNull Long userId,
            @Parameter(description = "状态") @RequestParam @NotNull Integer status) {
        boolean success = sysUserService.changeStatus(userId, status);
        return success ? R.ok() : R.fail();
    }
}
