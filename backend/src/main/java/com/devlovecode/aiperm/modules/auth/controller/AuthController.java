package com.devlovecode.aiperm.modules.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.modules.auth.dto.request.LoginRequest;
import com.devlovecode.aiperm.modules.auth.service.AuthService;
import com.devlovecode.aiperm.modules.auth.vo.CaptchaVO;
import com.devlovecode.aiperm.modules.auth.vo.LoginVO;
import com.devlovecode.aiperm.modules.auth.vo.MenuVO;
import com.devlovecode.aiperm.modules.auth.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证控制器
 *
 * @author DevLoveCode
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "获取验证码")
    @GetMapping("/captcha")
    public R<CaptchaVO> captcha() {
        return R.ok(authService.generateCaptcha());
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public R<LoginVO> login(@RequestBody @Valid LoginRequest request) {
        return R.ok(authService.login(request));
    }

    @Operation(summary = "登出")
    @SaCheckLogin
    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    @Operation(summary = "获取当前用户信息（含角色权限）")
    @SaCheckLogin
    @GetMapping("/info")
    public R<UserInfoVO> info() {
        return R.ok(authService.getUserInfo());
    }

    @Operation(summary = "获取当前用户菜单")
    @SaCheckLogin
    @GetMapping("/menus")
    public R<List<MenuVO>> menus() {
        return R.ok(authService.getUserMenus());
    }
}
