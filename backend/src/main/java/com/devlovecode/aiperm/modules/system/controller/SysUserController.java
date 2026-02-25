package com.devlovecode.aiperm.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.system.dto.UserDTO;
import com.devlovecode.aiperm.modules.system.entity.SysUser;
import com.devlovecode.aiperm.modules.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/user")
@SaCheckLogin
@RequiredArgsConstructor
public class SysUserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户列表")
    @SaCheckPermission("system:user:list")
    @Log(title = "用户管理", operType = OperType.QUERY)
    @GetMapping
    public R<PageResult<SysUser>> page(@Validated({Default.class, Views.Query.class}) UserDTO dto) {
        return R.ok(userService.queryPage(dto));
    }

    @Operation(summary = "根据ID查询用户")
    @SaCheckPermission("system:user:list")
    @GetMapping("/{id}")
    public R<SysUser> getById(@PathVariable Long id) {
        return R.ok(userService.findById(id));
    }

    @Operation(summary = "创建用户")
    @SaCheckPermission("system:user:create")
    @Log(title = "用户管理", operType = OperType.CREATE)
    @PostMapping
    public R<Void> create(@RequestBody @Validated({Default.class, Views.Create.class}) UserDTO dto) {
        userService.create(dto);
        return R.ok();
    }

    @Operation(summary = "更新用户")
    @SaCheckPermission("system:user:update")
    @Log(title = "用户管理", operType = OperType.UPDATE)
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody @Validated({Default.class, Views.Update.class}) UserDTO dto) {
        userService.update(id, dto);
        return R.ok();
    }

    @Operation(summary = "删除用户")
    @SaCheckPermission("system:user:delete")
    @Log(title = "用户管理", operType = OperType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return R.ok();
    }

    @Operation(summary = "重置用户密码")
    @SaCheckPermission("system:user:update")
    @Log(title = "用户管理", operType = OperType.UPDATE)
    @PutMapping("/{id}/reset-password")
    public R<Void> resetPassword(@PathVariable Long id, @RequestBody UserDTO dto) {
        userService.resetPassword(id, dto.getNewPassword());
        return R.ok();
    }

    @Operation(summary = "修改用户状态")
    @SaCheckPermission("system:user:update")
    @Log(title = "用户管理", operType = OperType.UPDATE)
    @PutMapping("/{id}/status")
    public R<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.changeStatus(id, status);
        return R.ok();
    }
}
