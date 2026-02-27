package com.devlovecode.aiperm.modules.auth.strategy;

import com.devlovecode.aiperm.modules.auth.vo.LoginVO;

public interface LoginStrategy {
    /** 获取登录类型 */
    String getLoginType();

    /** 执行登录 */
    LoginVO login(String identifier, String credential, String ip);
}
