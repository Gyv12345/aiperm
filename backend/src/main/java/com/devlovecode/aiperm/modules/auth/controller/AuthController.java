package com.devlovecode.aiperm.modules.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.devlovecode.aiperm.common.annotation.RateLimit;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.AccessLimitScope;
import com.devlovecode.aiperm.modules.auth.dto.UnifiedLoginDTO;
import com.devlovecode.aiperm.modules.auth.dto.request.LoginRequest;
import com.devlovecode.aiperm.modules.auth.service.AuthService;
import com.devlovecode.aiperm.modules.auth.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
	@RateLimit(count = 30, windowSeconds = 60, scope = AccessLimitScope.IP, key = "auth:captcha",
			message = "验证码请求过于频繁，请稍后重试")
	public R<CaptchaVO> captcha() {
		return R.ok(authService.generateCaptcha());
	}

	@Operation(summary = "登录（传统方式，保持兼容）")
	@PostMapping("/login")
	@RateLimit(count = 10, windowSeconds = 60, scope = AccessLimitScope.IP, key = "auth:login",
			message = "登录请求过于频繁，请稍后重试")
	public R<LoginVO> login(@RequestBody @Valid LoginRequest request) {
		return R.ok(authService.login(request));
	}

	@Operation(summary = "统一登录（支持多种登录方式）")
	@PostMapping("/unified-login")
	@RateLimit(count = 10, windowSeconds = 60, scope = AccessLimitScope.IP, key = "auth:unified-login",
			message = "登录请求过于频繁，请稍后重试")
	public R<LoginVO> unifiedLogin(@RequestBody @Valid UnifiedLoginDTO dto, HttpServletRequest request) {
		return R.ok(authService.unifiedLogin(dto, request));
	}

	@Operation(summary = "获取登录配置（控制前端显示哪些登录方式）")
	@GetMapping("/login-config")
	public R<LoginConfigVO> loginConfig() {
		return R.ok(authService.getLoginConfig());
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
