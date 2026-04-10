package com.devlovecode.aiperm.modules.auth.captcha.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_captcha_config")
public class SysCaptchaConfig extends BaseEntity {

	private String type; // SMS/EMAIL

	private Integer enabled; // 是否启用

	private String smsProvider; // 短信服务商

	private String smsAccessKey;

	private String smsSecretKey;

	private String smsSignName;

	private String smsTemplateCode;

	private String emailHost;

	private Integer emailPort;

	private String emailUsername;

	private String emailPassword;

	private String emailFrom;

	private String emailFromName;

	private Integer codeLength; // 验证码长度

	private Integer expireMinutes; // 过期时间

	private Integer dailyLimit; // 每日上限

}
