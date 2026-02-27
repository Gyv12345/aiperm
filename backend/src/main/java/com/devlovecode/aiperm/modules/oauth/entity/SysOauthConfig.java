package com.devlovecode.aiperm.modules.oauth.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * OAuth 平台配置实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysOauthConfig extends BaseEntity {
    private String platform;
    private Integer enabled;
    private String corpId;
    private String agentId;
    private String appKey;
    private String appSecret;
    private String callbackUrl;
    private String remark;
}
