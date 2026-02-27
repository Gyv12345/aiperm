package com.devlovecode.aiperm.modules.captcha.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CaptchaScene {
    LOGIN("LOGIN", "登录"),
    BIND("BIND", "绑定"),
    RESET("RESET", "重置密码");

    private final String code;
    private final String desc;
}
