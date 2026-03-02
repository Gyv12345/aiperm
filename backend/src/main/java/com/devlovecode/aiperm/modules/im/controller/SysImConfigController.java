package com.devlovecode.aiperm.modules.im.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.im.dto.ImConfigDTO;
import com.devlovecode.aiperm.modules.im.service.ImConfigService;
import com.devlovecode.aiperm.modules.im.vo.ImConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "IM平台配置")
@RestController
@RequestMapping("/system/im-config")
@SaCheckLogin
@RequiredArgsConstructor
public class SysImConfigController {

    private final ImConfigService imConfigService;

    @Operation(summary = "查询平台配置列表")
    @SaCheckPermission("system:im-config:list")
    @GetMapping
    public R<List<ImConfigVO>> list() {
        return R.ok(imConfigService.list());
    }

    @Operation(summary = "根据平台查询配置")
    @SaCheckPermission("system:im-config:list")
    @GetMapping("/{platform}")
    public R<ImConfigVO> get(@PathVariable String platform) {
        return R.ok(imConfigService.getByPlatform(platform));
    }

    @Operation(summary = "更新平台配置")
    @SaCheckPermission("system:im-config:update")
    @Log(title = "IM平台配置", operType = OperType.UPDATE)
    @PutMapping("/{platform}")
    public R<Void> update(@PathVariable String platform, @RequestBody ImConfigDTO dto) {
        imConfigService.update(platform, dto);
        return R.ok();
    }
}
