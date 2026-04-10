package com.devlovecode.aiperm.modules.auth.core.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码响应VO
 *
 * @author DevLoveCode
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "验证码响应")
public class CaptchaVO {

	@Schema(description = "验证码Key", example = "captcha_key_123")
	private String captchaKey;

	@Schema(description = "验证码图片（Base64）", example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...")
	private String captchaImage;

}
