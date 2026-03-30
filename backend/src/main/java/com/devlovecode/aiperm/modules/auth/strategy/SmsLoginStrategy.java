package com.devlovecode.aiperm.modules.auth.strategy;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.auth.enums.LoginType;
import com.devlovecode.aiperm.modules.auth.vo.LoginVO;
import com.devlovecode.aiperm.modules.captcha.enums.CaptchaScene;
import com.devlovecode.aiperm.modules.captcha.service.CaptchaService;
import com.devlovecode.aiperm.modules.log.service.LoginLogService;
import com.devlovecode.aiperm.modules.system.entity.SysUser;
import com.devlovecode.aiperm.modules.system.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SmsLoginStrategy implements LoginStrategy {

    private final UserRepository userRepo;
    private final LoginLogService loginLogService;

    @Qualifier("smsCaptchaService")
    private final CaptchaService smsCaptchaService;

    @Override
    public String getLoginType() {
        return LoginType.SMS.getCode();
    }

    @Override
    public LoginVO login(String identifier, String credential, String ip, String userAgent, HttpServletRequest request) {
        // identifier 是手机号，credential 是验证码
        // 验证短信验证码
        if (!smsCaptchaService.verify(identifier, credential, CaptchaScene.LOGIN)) {
            throw new BusinessException("验证码错误或已过期");
        }

        SysUser user = userRepo.findByPhone(identifier)
                .orElseThrow(() -> new BusinessException("该手机号未注册"));

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        StpUtil.login(user.getId());
        userRepo.updateLoginInfo(user.getId(), ip, LocalDateTime.now());
        loginLogService.recordSuccess(user.getId(), user.getUsername(), ip, userAgent, request);

        return LoginVO.builder()
                .token(StpUtil.getTokenValue())
                .userInfo(buildUserInfo(user))
                .build();
    }

    private LoginVO.UserInfo buildUserInfo(SysUser user) {
        return LoginVO.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }
}
