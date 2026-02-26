package com.devlovecode.aiperm.modules.auth.service;

import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.auth.dto.request.LoginRequest;
import com.devlovecode.aiperm.modules.auth.vo.CaptchaVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("认证服务测试")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    @DisplayName("生成验证码")
    void testGenerateCaptcha() {
        CaptchaVO captcha = authService.generateCaptcha();
        assertNotNull(captcha.getCaptchaKey(), "验证码 Key 不应为空");
        assertNotNull(captcha.getCaptchaImage(), "验证码图片不应为空");
        assertTrue(captcha.getCaptchaImage().startsWith("data:image"), "应该是 Base64 图片");
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void testLoginUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent_user");
        request.setPassword("any_password");
        request.setCaptchaKey("test_key");
        request.setCaptcha("test");

        assertThrows(BusinessException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void testLoginWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong_password");
        request.setCaptchaKey("test_key");
        request.setCaptcha("test");

        assertThrows(BusinessException.class, () -> authService.login(request));
    }
}
