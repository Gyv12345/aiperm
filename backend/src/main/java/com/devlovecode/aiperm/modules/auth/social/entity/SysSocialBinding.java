package com.devlovecode.aiperm.modules.auth.social.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 第三方账号绑定关系：系统用户 ↔ 第三方账号(source+openid)。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_social_binding")
public class SysSocialBinding extends BaseEntity {

	private Long userId;

	/** 第三方来源：GITHUB/GITEE */
	private String source;

	/** 第三方唯一ID(JustAuth uuid) */
	private String openid;

	private String nickname;

	private String avatar;

	private String email;

}
