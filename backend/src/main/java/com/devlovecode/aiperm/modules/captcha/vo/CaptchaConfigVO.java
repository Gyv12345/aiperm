package com.devlovecode.aiperm.modules.captcha.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 验证码配置响应 VO（密钥脱敏处理）
 */
@Data
@Schema(description = "验证码配置响应（密钥脱敏处理）")
public class CaptchaConfigVO {
    private Long id;
    private String type;             // SMS/EMAIL
    private Integer enabled;

    // SMS 配置（密钥脱敏）
    private String smsProvider;
    private String smsAccessKey;     // 返回时脱敏：前4位+****
    private String smsSignName;
    private String smsTemplateCode;

    // Email 配置（密码脱敏）
    private String emailHost;
    private Integer emailPort;
    private String emailUsername;
    private String emailFrom;
    private String emailFromName;

    // 通用
    private Integer codeLength;
    private Integer expireMinutes;
    private Integer dailyLimit;
}
