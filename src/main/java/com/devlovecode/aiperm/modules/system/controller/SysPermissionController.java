package com.devlovecode.aiperm.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.modules.system.entity.SysPermission;
import com.devlovecode.aiperm.modules.system.service.ISysPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 权限控制器
 *
 * @author devlovecode
 */
@Slf4j
@RestController
@RequestMapping("/api/system/permission")
@RequiredArgsConstructor
@Tag(name = "权限管理", description = "权限相关接口")
@Validated
public class SysPermissionController {

    private final ISysPermissionService sysPermissionService;

    @GetMapping("/page")
    @Operation(summary = "分页查询权限列表")
    @SaCheckPermission("system:permission:list")
    public R<PageResult<SysPermission>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long pageSize,
            @Parameter(description = "权限名称") @RequestParam(required = false) String permissionName,
            @Parameter(description = "权限编码") @RequestParam(required = false) String permissionCode,
            @Parameter(description = "权限类型") @RequestParam(required = false) Integer permissionType,
            @Parameter(description = "菜单ID") @RequestParam(required = false) Long menuId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        PageResult<SysPermission> result = sysPermissionService.page(pageNum, pageSize, permissionName,
                permissionCode, permissionType, menuId, status);
        return R.ok(result);
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有权限")
    @SaCheckPermission("system:permission:list")
    public R<List<SysPermission>> list() {
        List<SysPermission> list = sysPermissionService.list();
        return R.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询权限")
    @SaCheckPermission("system:permission:query")
    public R<SysPermission> getById(@Parameter(description = "权限ID") @PathVariable("id") @NotNull Long id) {
        SysPermission permission = sysPermissionService.getById(id);
        return R.ok(permission);
    }

    @PostMapping
    @Operation(summary = "创建权限")
    @SaCheckPermission("system:permission:add")
    public R<Void> create(@Valid @RequestBody SysPermission permission) {
        boolean success = sysPermissionService.create(permission);
        return success ? R.ok() : R.fail();
    }

    @PutMapping
    @Operation(summary = "更新权限")
    @SaCheckPermission("system:permission:edit")
    public R<Void> update(@Valid @RequestBody SysPermission permission) {
        boolean success = sysPermissionService.update(permission);
        return success ? R.ok() : R.fail();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除权限")
    @SaCheckPermission("system:permission:remove")
    public R<Void> delete(@Parameter(description = "权限ID") @PathVariable("id") @NotNull Long id) {
        boolean success = sysPermissionService.delete(id);
        return success ? R.ok() : R.fail();
    }
}
