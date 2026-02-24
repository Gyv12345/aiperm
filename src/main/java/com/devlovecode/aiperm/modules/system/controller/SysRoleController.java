package com.devlovecode.aiperm.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.modules.system.entity.SysRole;
import com.devlovecode.aiperm.modules.system.service.ISysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 角色控制器
 *
 * @author devlovecode
 */
@Slf4j
@RestController
@RequestMapping("/api/system/role")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色相关接口")
@Validated
public class SysRoleController {

    private final ISysRoleService sysRoleService;

    @GetMapping("/page")
    @Operation(summary = "分页查询角色列表")
    @SaCheckPermission("system:role:list")
    public R<PageResult<SysRole>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long pageSize,
            @Parameter(description = "角色名称") @RequestParam(required = false) String roleName,
            @Parameter(description = "角色编码") @RequestParam(required = false) String roleCode,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        PageResult<SysRole> result = sysRoleService.page(pageNum, pageSize, roleName, roleCode, status);
        return R.ok(result);
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有角色")
    @SaCheckPermission("system:role:list")
    public R<List<SysRole>> list() {
        List<SysRole> list = sysRoleService.list();
        return R.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询角色")
    @SaCheckPermission("system:role:query")
    public R<SysRole> getById(@Parameter(description = "角色ID") @PathVariable("id") @NotNull Long id) {
        SysRole role = sysRoleService.getById(id);
        return R.ok(role);
    }

    @PostMapping
    @Operation(summary = "创建角色")
    @SaCheckPermission("system:role:add")
    public R<Void> create(@Valid @RequestBody SysRole role) {
        boolean success = sysRoleService.create(role);
        return success ? R.ok() : R.fail();
    }

    @PutMapping
    @Operation(summary = "更新角色")
    @SaCheckPermission("system:role:edit")
    public R<Void> update(@Valid @RequestBody SysRole role) {
        boolean success = sysRoleService.update(role);
        return success ? R.ok() : R.fail();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    @SaCheckPermission("system:role:remove")
    public R<Void> delete(@Parameter(description = "角色ID") @PathVariable("id") @NotNull Long id) {
        boolean success = sysRoleService.delete(id);
        return success ? R.ok() : R.fail();
    }

    @PostMapping("/assign-menus")
    @Operation(summary = "分配角色菜单")
    @SaCheckPermission("system:role:edit")
    public R<Void> assignMenus(
            @Parameter(description = "角色ID") @RequestParam @NotNull Long roleId,
            @Parameter(description = "菜单ID列表") @RequestBody @NotNull List<Long> menuIds) {
        boolean success = sysRoleService.assignMenus(roleId, menuIds);
        return success ? R.ok() : R.fail();
    }

    @PostMapping("/assign-permissions")
    @Operation(summary = "分配角色权限")
    @SaCheckPermission("system:role:edit")
    public R<Void> assignPermissions(
            @Parameter(description = "角色ID") @RequestParam @NotNull Long roleId,
            @Parameter(description = "权限ID列表") @RequestBody @NotNull List<Long> permissionIds) {
        boolean success = sysRoleService.assignPermissions(roleId, permissionIds);
        return success ? R.ok() : R.fail();
    }
}
