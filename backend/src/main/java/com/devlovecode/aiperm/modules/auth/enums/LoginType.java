package com.devlovecode.aiperm.modules.auth.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoginType {
    PASSWORD("PASSWORD", "密码登录"),
    SMS("SMS", "短信验证码登录"),
    EMAIL("EMAIL", "邮箱验证码登录"),
    OAUTH("OAUTH", "第三方OAuth登录");

    private final String code;
    private final String desc;
}
