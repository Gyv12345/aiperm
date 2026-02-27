package com.devlovecode.aiperm.modules.oauth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.auth.vo.LoginVO;
import com.devlovecode.aiperm.modules.oauth.service.OAuthService;
import com.devlovecode.aiperm.modules.oauth.vo.OauthBindingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 第三方 OAuth 控制器
 */
@Tag(name = "第三方OAuth")
@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    // ===== 登录相关（无需登录） =====

    @Operation(summary = "跳转第三方登录授权页")
    @GetMapping("/login/{platform}")
    public void login(@PathVariable String platform, HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString().replace("-", "");
        String authUrl = oAuthService.getAuthorizationUrl(platform, state);
        response.sendRedirect(authUrl);
    }

    @Operation(summary = "第三方登录回调")
    @GetMapping("/login/callback/{platform}")
    public R<LoginVO> loginCallback(@PathVariable String platform, @RequestParam String code) {
        return R.ok(oAuthService.oauthLogin(platform, code));
    }

    // ===== 绑定相关（需要登录） =====

    @Operation(summary = "获取已绑定的第三方账号列表")
    @SaCheckLogin
    @GetMapping("/bindings")
    public R<List<OauthBindingVO>> bindings() {
        return R.ok(oAuthService.getBindings());
    }

    @Operation(summary = "跳转绑定第三方账号授权页")
    @SaCheckLogin
    @GetMapping("/bind/{platform}")
    public void bindRedirect(@PathVariable String platform, HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString().replace("-", "");
        String authUrl = oAuthService.getAuthorizationUrl(platform, state);
        response.sendRedirect(authUrl);
    }

    @Operation(summary = "绑定回调")
    @SaCheckLogin
    @Log(title = "OAuth绑定", operType = OperType.CREATE)
    @GetMapping("/bind/callback/{platform}")
    public R<Void> bindCallback(@PathVariable String platform, @RequestParam String code) {
        oAuthService.bind(platform, code);
        return R.ok();
    }

    @Operation(summary = "解绑第三方账号")
    @SaCheckLogin
    @Log(title = "OAuth解绑", operType = OperType.DELETE)
    @DeleteMapping("/unbind/{platform}")
    public R<Void> unbind(@PathVariable String platform) {
        oAuthService.unbind(platform);
        return R.ok();
    }
}
