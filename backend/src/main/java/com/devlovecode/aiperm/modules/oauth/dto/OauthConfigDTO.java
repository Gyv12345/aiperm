package com.devlovecode.aiperm.modules.oauth.dto;

import lombok.Data;

/**
 * OAuth 平台配置 DTO
 */
@Data
public class OauthConfigDTO {
    private Integer enabled;
    private String corpId;
    private String agentId;
    private String appKey;
    private String appSecret;   // 更新时传入，获取时脱敏
    private String callbackUrl;
    private String remark;
}
