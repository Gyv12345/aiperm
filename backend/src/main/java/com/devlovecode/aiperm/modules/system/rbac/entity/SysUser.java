package com.devlovecode.aiperm.modules.system.rbac.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author devlovecode
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_user")
public class SysUser extends BaseEntity {

	private String username;

	@JsonIgnore
	private String password;

	private String nickname;

	private String realName;

	private String email;

	private String phone;

	private Integer gender;

	private String avatar;

	private Long deptId;

	private Long postId;

	private Integer isAdmin;

	private Integer status;

	private String remark;

	private String lastLoginIp;

	private LocalDateTime lastLoginTime;

	/** 密码最后更新时间（NULL=未记录，不参与有效期判定） */
	private LocalDateTime pwdUpdateTime;

}
