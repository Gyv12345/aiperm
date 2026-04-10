package com.devlovecode.aiperm.modules.auth.oauth.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户第三方账号绑定实体
 */
@Entity
@Table(name = "sys_user_oauth")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserOauth extends BaseEntity {

	private Long userId;

	private String platform; // WEWORK/DINGTALK/FEISHU

	private String openId; // 第三方平台用户标识

	private String unionId; // 企业统一标识（可选）

	private String nickname;

	private String avatar;

	private LocalDateTime lastLoginTime;

	private Integer status; // 1正常，0已解绑

}
