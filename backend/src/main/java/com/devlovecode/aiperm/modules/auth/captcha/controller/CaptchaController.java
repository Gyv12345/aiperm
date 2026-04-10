package com.devlovecode.aiperm.modules.auth.captcha.controller;

import com.devlovecode.aiperm.common.annotation.RateLimit;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.AccessLimitScope;
import com.devlovecode.aiperm.common.util.ClientIpUtils;
import com.devlovecode.aiperm.modules.auth.captcha.dto.SendCaptchaDTO;
import com.devlovecode.aiperm.modules.auth.captcha.enums.CaptchaScene;
import com.devlovecode.aiperm.modules.auth.captcha.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "验证码")
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

	private final CaptchaService smsCaptchaService;

	private final CaptchaService emailCaptchaService;

	public CaptchaController(@Qualifier("smsCaptchaService") CaptchaService smsCaptchaService,
			@Qualifier("emailCaptchaService") CaptchaService emailCaptchaService) {
		this.smsCaptchaService = smsCaptchaService;
		this.emailCaptchaService = emailCaptchaService;
	}

	@Operation(summary = "发送验证码（短信或邮件）")
	@PostMapping("/send")
	@RateLimit(count = 10, windowSeconds = 60, scope = AccessLimitScope.IP, key = "captcha:send",
			message = "验证码发送请求过于频繁，请稍后重试")
	public R<Void> send(@RequestBody @Valid SendCaptchaDTO dto, HttpServletRequest request) {
		String ip = ClientIpUtils.getClientIp(request);
		CaptchaScene scene = CaptchaScene.valueOf(dto.getScene().toUpperCase());

		CaptchaService service = "SMS".equalsIgnoreCase(dto.getType()) ? smsCaptchaService : emailCaptchaService;

		service.send(dto.getTarget(), scene, ip);
		return R.ok();
	}

}
