package com.devlovecode.aiperm.modules.auth.strategy;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.auth.enums.LoginType;
import com.devlovecode.aiperm.modules.auth.vo.LoginVO;
import com.devlovecode.aiperm.modules.log.service.LoginLogService;
import com.devlovecode.aiperm.modules.system.entity.SysUser;
import com.devlovecode.aiperm.modules.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PasswordLoginStrategy implements LoginStrategy {

    private final UserRepository userRepo;
    private final StringRedisTemplate redisTemplate;
    private final LoginLogService loginLogService;

    private static final String CAPTCHA_PREFIX = "captcha:";

    @Override
    public String getLoginType() {
        return LoginType.PASSWORD.getCode();
    }

    @Override
    public LoginVO login(String identifier, String credential, String ip) {
        // identifier 是用户名，credential 是密码
        // 注意：图形验证码在 Controller 层已验证

        SysUser user = userRepo.findByUsername(identifier)
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        if (!BCrypt.checkpw(credential, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        StpUtil.login(user.getId());
        userRepo.updateLoginInfo(user.getId(), ip, LocalDateTime.now());
        loginLogService.recordSuccess(user.getId(), user.getUsername(), ip);

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
