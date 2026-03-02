package com.devlovecode.aiperm.modules.im.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysImConfig extends BaseEntity {
    private String platform;
    private Integer enabled;
    private String appId;
    private String appSecret;
    private String corpId;
    private String callbackToken;
    private String callbackAesKey;
    private String extraConfig;
}
