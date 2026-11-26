package com.devlovecode.aiperm.modules.auth.social.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "第三方授权链接")
public class SocialAuthorizeVO {

	@Schema(description = "第三方平台授权地址，前端跳转至此")
	private String authorizeUrl;

}
