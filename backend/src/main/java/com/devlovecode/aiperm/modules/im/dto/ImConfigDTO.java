package com.devlovecode.aiperm.modules.im.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "IM平台配置")
public class ImConfigDTO {

    @JsonView({Views.Update.class})
    @Schema(description = "是否启用:0-否 1-是")
    private Integer enabled;

    @JsonView({Views.Update.class})
    @Schema(description = "应用ID")
    private String appId;

    @JsonView({Views.Update.class})
    @Schema(description = "应用密钥")
    private String appSecret;

    @JsonView({Views.Update.class})
    @Schema(description = "企业ID")
    private String corpId;

    @JsonView({Views.Update.class})
    @Schema(description = "回调token")
    private String callbackToken;

    @JsonView({Views.Update.class})
    @Schema(description = "回调加解密key")
    private String callbackAesKey;

    @JsonView({Views.Update.class})
    @Schema(description = "扩展配置(JSON字符串)")
    private String extraConfig;
}
