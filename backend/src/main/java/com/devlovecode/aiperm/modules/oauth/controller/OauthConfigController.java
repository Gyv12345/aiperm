package com.devlovecode.aiperm.modules.oauth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.oauth.dto.OauthConfigDTO;
import com.devlovecode.aiperm.modules.oauth.service.OauthConfigService;
import com.devlovecode.aiperm.modules.oauth.vo.OauthConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * OAuth 配置管理控制器
 */
@Tag(name = "OAuth配置管理")
@RestController
@RequestMapping("/system/oauth-config")
@SaCheckLogin
@RequiredArgsConstructor
public class OauthConfigController {

    private final OauthConfigService oauthConfigService;

    @Operation(summary = "获取OAuth平台配置")
    @SaCheckPermission("system:oauth:config")
    @GetMapping("/{platform}")
    public R<OauthConfigVO> getConfig(@PathVariable String platform) {
        return R.ok(oauthConfigService.getConfig(platform));
    }

    @Operation(summary = "更新OAuth平台配置")
    @SaCheckPermission("system:oauth:config")
    @Log(title = "OAuth配置管理", operType = OperType.UPDATE)
    @PutMapping("/{platform}")
    public R<Void> updateConfig(@PathVariable String platform,
                                @RequestBody OauthConfigDTO dto) {
        oauthConfigService.updateConfig(platform, dto);
        return R.ok();
    }
}
