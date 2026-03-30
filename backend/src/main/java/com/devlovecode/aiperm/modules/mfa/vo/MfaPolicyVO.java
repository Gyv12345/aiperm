package com.devlovecode.aiperm.modules.mfa.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 2FA 策略配置 VO
 */
@Data
public class MfaPolicyVO {

	private Long id;

	private String name;

	private String permPattern;

	private String apiPattern;

	private Integer enabled;

	private LocalDateTime createTime;

}
