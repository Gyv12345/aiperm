package com.devlovecode.aiperm.modules.approval.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "IM平台配置")
public class ImConfigDTO {

	@JsonView({ Views.Update.class })
	@Schema(description = "是否启用")
	@NotNull(message = "启用状态不能为空", groups = Views.Update.class)
	private Integer enabled;

	@JsonView({ Views.Update.class })
	@Schema(description = "应用ID")
	@Size(max = 100, message = "应用ID不能超过100个字符")
	private String appId;

	@JsonView({ Views.Update.class })
	@Schema(description = "应用密钥")
	@Size(max = 200, message = "应用密钥不能超过200个字符")
	private String appSecret;

	@JsonView({ Views.Update.class })
	@Schema(description = "企业ID")
	@Size(max = 100, message = "企业ID不能超过100个字符")
	private String corpId;

	@JsonView({ Views.Update.class })
	@Schema(description = "回调验证Token")
	@Size(max = 200, message = "回调Token不能超过200个字符")
	private String callbackToken;

	@JsonView({ Views.Update.class })
	@Schema(description = "回调AES Key")
	@Size(max = 200, message = "回调AES Key不能超过200个字符")
	private String callbackAesKey;

	@JsonView({ Views.Update.class })
	@Schema(description = "扩展配置JSON")
	private String extraConfig;

	@JsonView({ Views.Update.class })
	@Schema(description = "备注")
	@Size(max = 500, message = "备注不能超过500个字符")
	private String remark;

}
