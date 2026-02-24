package com.devlovecode.aiperm.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.modules.system.entity.SysMenu;
import com.devlovecode.aiperm.modules.system.service.ISysMenuService;
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
 * 菜单控制器
 *
 * @author devlovecode
 */
@Slf4j
@RestController
@RequestMapping("/api/system/menu")
@RequiredArgsConstructor
@Tag(name = "菜单管理", description = "菜单相关接口")
@Validated
public class SysMenuController {

    private final ISysMenuService sysMenuService;

    @GetMapping("/tree")
    @Operation(summary = "查询菜单树")
    @SaCheckPermission("system:menu:list")
    public R<List<SysMenu>> tree() {
        List<SysMenu> tree = sysMenuService.getMenuTree();
        return R.ok(tree);
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有菜单")
    @SaCheckPermission("system:menu:list")
    public R<List<SysMenu>> list() {
        List<SysMenu> list = sysMenuService.list();
        return R.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询菜单")
    @SaCheckPermission("system:menu:query")
    public R<SysMenu> getById(@Parameter(description = "菜单ID") @PathVariable("id") @NotNull Long id) {
        SysMenu menu = sysMenuService.getById(id);
        return R.ok(menu);
    }

    @GetMapping("/children/{parentId}")
    @Operation(summary = "查询子菜单列表")
    @SaCheckPermission("system:menu:list")
    public R<List<SysMenu>> getChildren(@Parameter(description = "父菜单ID") @PathVariable("parentId") @NotNull Long parentId) {
        List<SysMenu> children = sysMenuService.listByParentId(parentId);
        return R.ok(children);
    }

    @PostMapping
    @Operation(summary = "创建菜单")
    @SaCheckPermission("system:menu:add")
    public R<Void> create(@Valid @RequestBody SysMenu menu) {
        boolean success = sysMenuService.create(menu);
        return success ? R.ok() : R.fail();
    }

    @PutMapping
    @Operation(summary = "更新菜单")
    @SaCheckPermission("system:menu:edit")
    public R<Void> update(@Valid @RequestBody SysMenu menu) {
        boolean success = sysMenuService.update(menu);
        return success ? R.ok() : R.fail();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜单")
    @SaCheckPermission("system:menu:remove")
    public R<Void> delete(@Parameter(description = "菜单ID") @PathVariable("id") @NotNull Long id) {
        boolean success = sysMenuService.delete(id);
        return success ? R.ok() : R.fail();
    }
}
