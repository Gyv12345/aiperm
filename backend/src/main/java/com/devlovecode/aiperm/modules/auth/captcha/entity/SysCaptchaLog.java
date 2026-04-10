package com.devlovecode.aiperm.modules.auth.captcha.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 验证码发送日志（无软删除，不继承 BaseEntity）
 */
@Data
@Entity
@Table(name = "sys_captcha_log")
public class SysCaptchaLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String type;

	private String target;

	private String code;

	private String scene;

	private Integer status; // 1成功,0失败

	private String failReason;

	private String ip;

	private LocalDateTime createTime;

}
