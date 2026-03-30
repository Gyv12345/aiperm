package com.devlovecode.aiperm.modules.oauth.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * OAuth 平台配置实体
 */
@Entity
@Table(name = "sys_oauth_config")
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
