package com.devlovecode.aiperm.modules.captcha.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.captcha.dto.CaptchaConfigDTO;
import com.devlovecode.aiperm.modules.captcha.service.CaptchaConfigService;
import com.devlovecode.aiperm.modules.captcha.vo.CaptchaConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 验证码配置管理控制器
 */
@Tag(name = "验证码配置管理")
@RestController
@RequestMapping("/system/captcha-config")
@SaCheckLogin
@RequiredArgsConstructor
public class CaptchaConfigController {

    private final CaptchaConfigService captchaConfigService;

    @Operation(summary = "获取验证码配置（SMS或EMAIL）")
    @SaCheckPermission("system:captcha:config")
    @GetMapping("/{type}")
    public R<CaptchaConfigVO> getConfig(@PathVariable String type) {
        return R.ok(captchaConfigService.getConfig(type));
    }

    @Operation(summary = "更新验证码配置")
    @SaCheckPermission("system:captcha:config")
    @Log(title = "验证码配置", operType = OperType.UPDATE)
    @PutMapping("/{type}")
    public R<Void> updateConfig(
            @PathVariable String type,
            @RequestBody @Validated({jakarta.validation.groups.Default.class, Views.Update.class}) CaptchaConfigDTO dto) {
        captchaConfigService.updateConfig(type, dto);
        return R.ok();
    }
}
