package com.devlovecode.aiperm.modules.oauth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 第三方账号绑定信息 VO
 */
@Data
@Schema(description = "第三方账号绑定信息")
public class OauthBindingVO {
    @Schema(description = "平台：WEWORK/DINGTALK/FEISHU")
    private String platform;

    @Schema(description = "第三方昵称")
    private String nickname;

    @Schema(description = "第三方头像")
    private String avatar;

    @Schema(description = "绑定时间")
    private LocalDateTime createTime;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;
}
