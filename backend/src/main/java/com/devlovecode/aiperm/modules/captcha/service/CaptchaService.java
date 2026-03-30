package com.devlovecode.aiperm.modules.captcha.service;

import com.devlovecode.aiperm.modules.captcha.enums.CaptchaScene;

public interface CaptchaService {

	/** 发送验证码（包含限流检查） */
	void send(String target, CaptchaScene scene, String ip);

	/** 验证验证码（验证成功后删除） */
	boolean verify(String target, String code, CaptchaScene scene);

}
