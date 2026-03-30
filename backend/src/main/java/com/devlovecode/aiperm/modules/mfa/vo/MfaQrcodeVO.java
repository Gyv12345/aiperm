package com.devlovecode.aiperm.modules.mfa.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 2FA绑定二维码 VO
 */
@Data
@Builder
@Schema(description = "2FA绑定二维码")
public class MfaQrcodeVO {

	@Schema(description = "TOTP URI（用于生成二维码）")
	private String totpUri;

	@Schema(description = "密钥（用于手动输入）")
	private String secretKey;

}
