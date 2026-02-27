package com.devlovecode.aiperm.modules.captcha.controller;

import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.modules.captcha.dto.SendCaptchaDTO;
import com.devlovecode.aiperm.modules.captcha.enums.CaptchaScene;
import com.devlovecode.aiperm.modules.captcha.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "验证码")
@RestController
@RequestMapping("/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    @Qualifier("smsCaptchaService")
    private final CaptchaService smsCaptchaService;

    @Qualifier("emailCaptchaService")
    private final CaptchaService emailCaptchaService;

    @Operation(summary = "发送验证码（短信或邮件）")
    @PostMapping("/send")
    public R<Void> send(@RequestBody @Valid SendCaptchaDTO dto, HttpServletRequest request) {
        String ip = getClientIp(request);
        CaptchaScene scene = CaptchaScene.valueOf(dto.getScene().toUpperCase());

        CaptchaService service = "SMS".equalsIgnoreCase(dto.getType())
                ? smsCaptchaService
                : emailCaptchaService;

        service.send(dto.getTarget(), scene, ip);
        return R.ok();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
