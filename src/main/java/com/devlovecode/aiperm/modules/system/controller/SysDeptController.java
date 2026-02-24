package com.devlovecode.aiperm.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.modules.system.entity.SysDept;
import com.devlovecode.aiperm.modules.system.service.ISysDeptService;
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
 * 部门控制器
 *
 * @author devlovecode
 */
@Slf4j
@RestController
@RequestMapping("/api/system/dept")
@RequiredArgsConstructor
@Tag(name = "部门管理", description = "部门相关接口")
@Validated
public class SysDeptController {

    private final ISysDeptService sysDeptService;

    @GetMapping("/tree")
    @Operation(summary = "查询部门树")
    @SaCheckPermission("system:dept:list")
    public R<List<SysDept>> tree() {
        List<SysDept> tree = sysDeptService.getDeptTree();
        return R.ok(tree);
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有部门")
    @SaCheckPermission("system:dept:list")
    public R<List<SysDept>> list() {
        List<SysDept> list = sysDeptService.list();
        return R.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询部门")
    @SaCheckPermission("system:dept:query")
    public R<SysDept> getById(@Parameter(description = "部门ID") @PathVariable("id") @NotNull Long id) {
        SysDept dept = sysDeptService.getById(id);
        return R.ok(dept);
    }

    @GetMapping("/children/{parentId}")
    @Operation(summary = "查询子部门列表")
    @SaCheckPermission("system:dept:list")
    public R<List<SysDept>> getChildren(@Parameter(description = "父部门ID") @PathVariable("parentId") @NotNull Long parentId) {
        List<SysDept> children = sysDeptService.listByParentId(parentId);
        return R.ok(children);
    }

    @PostMapping
    @Operation(summary = "创建部门")
    @SaCheckPermission("system:dept:add")
    public R<Void> create(@Valid @RequestBody SysDept dept) {
        boolean success = sysDeptService.create(dept);
        return success ? R.ok() : R.fail();
    }

    @PutMapping
    @Operation(summary = "更新部门")
    @SaCheckPermission("system:dept:edit")
    public R<Void> update(@Valid @RequestBody SysDept dept) {
        boolean success = sysDeptService.update(dept);
        return success ? R.ok() : R.fail();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门")
    @SaCheckPermission("system:dept:remove")
    public R<Void> delete(@Parameter(description = "部门ID") @PathVariable("id") @NotNull Long id) {
        boolean success = sysDeptService.delete(id);
        return success ? R.ok() : R.fail();
    }
}
