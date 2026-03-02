package com.devlovecode.aiperm.modules.im.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "IM平台配置视图")
public class ImConfigVO {
    private Long id;
    private String platform;
    private Integer enabled;
    private String appId;
    private String appSecret;
    private String corpId;
    private String callbackToken;
    private String callbackAesKey;
    private String extraConfig;
}
