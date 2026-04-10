package com.devlovecode.aiperm.modules.auth.mfa.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 2FA绑定状态 VO
 */
@Data
@Builder
@Schema(description = "2FA绑定状态")
public class MfaStatusVO {

	@Schema(description = "是否已绑定")
	private boolean bound;

	@Schema(description = "是否强制要求（超管必须绑定）")
	private boolean required;

	@Schema(description = "当前Redis中是否已验证（30分钟内）")
	private boolean verified;

}
