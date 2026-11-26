package com.devlovecode.aiperm.modules.auth.social.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.common.util.ClientIpUtils;
import com.devlovecode.aiperm.modules.auth.social.service.SocialAuthService;
import com.devlovecode.aiperm.modules.auth.social.vo.SocialAuthorizeVO;
import com.devlovecode.aiperm.modules.auth.social.vo.SocialBindingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * 第三方社交登录。
 *
 * - authorize/callback 免登录（SaTokenConfig 排除）；bind 场景的登录态由 service 校验。
 * - bindings/unbind 需登录（@SaCheckLogin）。
 */
@Tag(name = "第三方社交登录")
@RestController
@RequiredArgsConstructor
public class SocialAuthController {

	private final SocialAuthService socialAuthService;

	@Operation(summary = "获取第三方授权链接")
	@GetMapping("/auth/social/authorize")
	public R<SocialAuthorizeVO> authorize(@RequestParam String platform,
			@RequestParam(defaultValue = "login") String action) {
		String url = socialAuthService.buildAuthorizeUrl(platform, action);
		return R.ok(SocialAuthorizeVO.builder().authorizeUrl(url).build());
	}

	@Operation(summary = "第三方登录/绑定回调（处理并 302 重定向到前端回调页）")
	@GetMapping("/auth/social/callback")
	public void callback(@RequestParam String code, @RequestParam(required = false) String state,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		String ip = ClientIpUtils.getClientIp(request);
		String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
		String target = socialAuthService.handleCallback(code, state, ip, userAgent, request);
		response.sendRedirect(target);
	}

	@Operation(summary = "我的第三方账号绑定列表")
	@SaCheckLogin
	@GetMapping("/profile/social/bindings")
	public R<List<SocialBindingVO>> bindings() {
		return R.ok(socialAuthService.listBindingsOfCurrentUser());
	}

	@Operation(summary = "解绑第三方账号")
	@SaCheckLogin
	@Log(title = "第三方账号绑定", operType = OperType.DELETE)
	@DeleteMapping("/profile/social/unbind")
	public R<Void> unbind(@RequestParam String platform) {
		socialAuthService.unbind(platform);
		return R.ok();
	}

}
