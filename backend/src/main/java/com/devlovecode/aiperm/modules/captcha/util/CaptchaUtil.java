package com.devlovecode.aiperm.modules.captcha.util;

import cn.hutool.core.util.RandomUtil;

/**
 * 验证码工具类
 *
 * @author devlovecode
 */
public class CaptchaUtil {

	private CaptchaUtil() {
	}

	/**
	 * 生成数字验证码
	 */
	public static String generateCode(int length) {
		return RandomUtil.randomNumbers(length);
	}

}
