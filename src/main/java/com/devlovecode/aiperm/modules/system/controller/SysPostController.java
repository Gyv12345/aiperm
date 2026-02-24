package com.devlovecode.aiperm.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.modules.system.entity.SysPost;
import com.devlovecode.aiperm.modules.system.service.ISysPostService;
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
 * 岗位控制器
 *
 * @author devlovecode
 */
@Slf4j
@RestController
@RequestMapping("/api/system/post")
@RequiredArgsConstructor
@Tag(name = "岗位管理", description = "岗位相关接口")
@Validated
public class SysPostController {

    private final ISysPostService sysPostService;

    @GetMapping("/page")
    @Operation(summary = "分页查询岗位列表")
    @SaCheckPermission("system:post:list")
    public R<PageResult<SysPost>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long pageSize,
            @Parameter(description = "岗位名称") @RequestParam(required = false) String postName,
            @Parameter(description = "岗位编码") @RequestParam(required = false) String postCode,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        PageResult<SysPost> result = sysPostService.page(pageNum, pageSize, postName, postCode, status);
        return R.ok(result);
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有岗位")
    @SaCheckPermission("system:post:list")
    public R<List<SysPost>> list() {
        List<SysPost> list = sysPostService.list();
        return R.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询岗位")
    @SaCheckPermission("system:post:query")
    public R<SysPost> getById(@Parameter(description = "岗位ID") @PathVariable("id") @NotNull Long id) {
        SysPost post = sysPostService.getById(id);
        return R.ok(post);
    }

    @PostMapping
    @Operation(summary = "创建岗位")
    @SaCheckPermission("system:post:add")
    public R<Void> create(@Valid @RequestBody SysPost post) {
        boolean success = sysPostService.create(post);
        return success ? R.ok() : R.fail();
    }

    @PutMapping
    @Operation(summary = "更新岗位")
    @SaCheckPermission("system:post:edit")
    public R<Void> update(@Valid @RequestBody SysPost post) {
        boolean success = sysPostService.update(post);
        return success ? R.ok() : R.fail();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除岗位")
    @SaCheckPermission("system:post:remove")
    public R<Void> delete(@Parameter(description = "岗位ID") @PathVariable("id") @NotNull Long id) {
        boolean success = sysPostService.delete(id);
        return success ? R.ok() : R.fail();
    }
}
