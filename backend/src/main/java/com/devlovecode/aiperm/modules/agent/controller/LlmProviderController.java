package com.devlovecode.aiperm.modules.agent.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.agent.dto.LlmProviderDTO;
import com.devlovecode.aiperm.modules.agent.service.LlmProviderService;
import com.devlovecode.aiperm.modules.agent.vo.LlmProviderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "LLM提供商管理")
@RestController
@RequestMapping("/agent/provider")
@SaCheckLogin
@RequiredArgsConstructor
public class LlmProviderController {

    private final LlmProviderService providerService;

    @Operation(summary = "查询所有提供商")
    @SaCheckPermission("agent:provider:list")
    @GetMapping
    public R<List<LlmProviderVO>> list() {
        return R.ok(providerService.listAll());
    }

    @Operation(summary = "查询提供商详情")
    @SaCheckPermission("agent:provider:query")
    @GetMapping("/{id}")
    public R<LlmProviderVO> getById(@PathVariable Long id) {
        return R.ok(providerService.findById(id));
    }

    @Operation(summary = "新增提供商")
    @SaCheckPermission("agent:provider:create")
    @Log(title = "LLM提供商管理", operType = OperType.CREATE)
    @PostMapping
    public R<Long> create(@RequestBody @Valid LlmProviderDTO dto) {
        return R.ok(providerService.create(dto));
    }

    @Operation(summary = "更新提供商")
    @SaCheckPermission("agent:provider:update")
    @Log(title = "LLM提供商管理", operType = OperType.UPDATE)
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody @Valid LlmProviderDTO dto) {
        providerService.update(id, dto);
        return R.ok();
    }

    @Operation(summary = "删除提供商")
    @SaCheckPermission("agent:provider:delete")
    @Log(title = "LLM提供商管理", operType = OperType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        providerService.delete(id);
        return R.ok();
    }

    @Operation(summary = "设为默认")
    @SaCheckPermission("agent:provider:update")
    @Log(title = "LLM提供商管理", operType = OperType.UPDATE)
    @PutMapping("/{id}/default")
    public R<Void> setDefault(@PathVariable Long id) {
        providerService.setDefault(id);
        return R.ok();
    }
}
