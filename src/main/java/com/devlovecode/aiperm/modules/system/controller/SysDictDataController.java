package com.devlovecode.aiperm.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.system.dto.request.DictDataCreateRequest;
import com.devlovecode.aiperm.modules.system.entity.SysDictData;
import com.devlovecode.aiperm.modules.system.service.ISysDictDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "字典数据管理")
@RestController
@RequestMapping("/system/dict/data")
@SaCheckLogin
@RequiredArgsConstructor
public class SysDictDataController {

    private final ISysDictDataService dictDataService;

    @Operation(summary = "根据字典类型查询字典数据")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/list")
    public R<List<SysDictData>> listByDictType(@RequestParam String dictType) {
        return R.ok(dictDataService.listByDictType(dictType));
    }

    @Operation(summary = "创建字典数据")
    @SaCheckPermission("system:dict:create")
    @Log(title = "字典数据管理", operType = OperType.CREATE)
    @PostMapping
    public R<Void> create(@Valid @RequestBody DictDataCreateRequest req) {
        SysDictData dictData = new SysDictData();
        dictData.setDictType(req.getDictType());
        dictData.setDictLabel(req.getDictLabel());
        dictData.setDictValue(req.getDictValue());
        dictData.setSort(req.getSort() != null ? req.getSort() : 0);
        dictData.setRemark(req.getRemark());
        dictData.setStatus(1);
        dictDataService.create(dictData);
        return R.ok();
    }

    @Operation(summary = "更新字典数据")
    @SaCheckPermission("system:dict:update")
    @Log(title = "字典数据管理", operType = OperType.UPDATE)
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody DictDataCreateRequest req) {
        SysDictData dictData = new SysDictData();
        dictData.setId(id);
        dictData.setDictLabel(req.getDictLabel());
        dictData.setDictValue(req.getDictValue());
        dictData.setSort(req.getSort());
        dictData.setRemark(req.getRemark());
        dictDataService.update(dictData);
        return R.ok();
    }

    @Operation(summary = "删除字典数据")
    @SaCheckPermission("system:dict:delete")
    @Log(title = "字典数据管理", operType = OperType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        dictDataService.delete(id);
        return R.ok();
    }
}
