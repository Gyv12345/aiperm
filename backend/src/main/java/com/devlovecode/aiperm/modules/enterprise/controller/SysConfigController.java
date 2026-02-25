package com.devlovecode.aiperm.modules.enterprise.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.enterprise.dto.ConfigDTO;
import com.devlovecode.aiperm.modules.enterprise.service.ConfigService;
import com.devlovecode.aiperm.modules.enterprise.vo.ConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "系统配置管理")
@RestController
@RequestMapping("/enterprise/config")
@SaCheckLogin
@RequiredArgsConstructor
public class SysConfigController {

    private final ConfigService configService;

    @Operation(summary = "分页查询系统配置")
    @SaCheckPermission("enterprise:config:list")
    @Log(title = "系统配置管理", operType = OperType.QUERY)
    @GetMapping
    public R<PageResult<ConfigVO>> list(@Validated({Default.class, Views.Query.class}) ConfigDTO dto) {
        return R.ok(configService.queryPage(dto));
    }

    @Operation(summary = "查询系统配置详情")
    @SaCheckPermission("enterprise:config:list")
    @GetMapping("/{id}")
    public R<ConfigVO> detail(@PathVariable Long id) {
        return R.ok(configService.findById(id));
    }

    @Operation(summary = "根据配置键查询")
    @SaCheckPermission("enterprise:config:list")
    @GetMapping("/key/{configKey}")
    public R<ConfigVO> getByKey(@PathVariable String configKey) {
        return R.ok(configService.findByConfigKey(configKey));
    }

    @Operation(summary = "创建系统配置")
    @SaCheckPermission("enterprise:config:create")
    @Log(title = "系统配置管理", operType = OperType.CREATE)
    @PostMapping
    public R<Long> create(@RequestBody @Validated({Default.class, Views.Create.class}) ConfigDTO dto) {
        return R.ok(configService.create(dto));
    }

    @Operation(summary = "更新系统配置")
    @SaCheckPermission("enterprise:config:update")
    @Log(title = "系统配置管理", operType = OperType.UPDATE)
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id,
                          @RequestBody @Validated({Default.class, Views.Update.class}) ConfigDTO dto) {
        configService.update(id, dto);
        return R.ok();
    }

    @Operation(summary = "删除系统配置")
    @SaCheckPermission("enterprise:config:delete")
    @Log(title = "系统配置管理", operType = OperType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        configService.delete(id);
        return R.ok();
    }
}
