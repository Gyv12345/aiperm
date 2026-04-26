package com.devlovecode.aiperm.modules.approval.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "sys_im_config")
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

	@Column(columnDefinition = "json")
	private String extraConfig;

	private String remark;

}
