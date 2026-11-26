package com.devlovecode.aiperm.modules.auth.core.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "登录配置响应")
public class LoginConfigVO {

	@Schema(description = "密码登录是否启用")
	private boolean passwordEnabled;

	@Schema(description = "短信验证码登录是否启用")
	private boolean smsEnabled;

	@Schema(description = "邮箱验证码登录是否启用")
	private boolean emailEnabled;

	@Schema(description = "已启用的第三方登录平台列表(GITHUB/GITEE)")
	private List<String> socialPlatforms;

}
