package com.devlovecode.aiperm.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.system.dto.request.DictTypeCreateRequest;
import com.devlovecode.aiperm.modules.system.dto.request.DictTypeUpdateRequest;
import com.devlovecode.aiperm.modules.system.entity.SysDictType;
import com.devlovecode.aiperm.modules.system.service.ISysDictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "字典类型管理")
@RestController
@RequestMapping("/system/dict/type")
@SaCheckLogin
@RequiredArgsConstructor
public class SysDictTypeController {

    private final ISysDictTypeService dictTypeService;

    @Operation(summary = "分页查询字典类型")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/page")
    public R<PageResult<SysDictType>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String dictName,
            @RequestParam(required = false) String dictType) {
        return R.ok(dictTypeService.page(page, pageSize, dictName, dictType));
    }

    @Operation(summary = "根据ID查询字典类型")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/{id}")
    public R<SysDictType> getById(@PathVariable Long id) {
        return R.ok(dictTypeService.getById(id));
    }

    @Operation(summary = "创建字典类型")
    @SaCheckPermission("system:dict:create")
    @Log(title = "字典类型管理", operType = OperType.CREATE)
    @PostMapping
    public R<Void> create(@Valid @RequestBody DictTypeCreateRequest req) {
        SysDictType dictType = new SysDictType();
        dictType.setDictName(req.getDictName());
        dictType.setDictType(req.getDictType());
        dictType.setRemark(req.getRemark());
        dictType.setStatus(1);
        dictTypeService.create(dictType);
        return R.ok();
    }

    @Operation(summary = "更新字典类型")
    @SaCheckPermission("system:dict:update")
    @Log(title = "字典类型管理", operType = OperType.UPDATE)
    @PutMapping
    public R<Void> update(@Valid @RequestBody DictTypeUpdateRequest req) {
        SysDictType dictType = new SysDictType();
        dictType.setId(req.getId());
        dictType.setDictName(req.getDictName());
        dictType.setStatus(req.getStatus());
        dictType.setRemark(req.getRemark());
        dictTypeService.update(dictType);
        return R.ok();
    }

    @Operation(summary = "删除字典类型")
    @SaCheckPermission("system:dict:delete")
    @Log(title = "字典类型管理", operType = OperType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        dictTypeService.delete(id);
        return R.ok();
    }
}
