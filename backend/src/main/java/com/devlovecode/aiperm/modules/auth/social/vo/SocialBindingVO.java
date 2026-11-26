package com.devlovecode.aiperm.modules.auth.social.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "第三方账号绑定信息")
public class SocialBindingVO {

	@Schema(description = "第三方来源：GITHUB/GITEE")
	private String source;

	@Schema(description = "第三方昵称")
	private String nickname;

	@Schema(description = "第三方头像")
	private String avatar;

	@Schema(description = "第三方邮箱")
	private String email;

	@Schema(description = "绑定时间")
	private LocalDateTime boundAt;

}
