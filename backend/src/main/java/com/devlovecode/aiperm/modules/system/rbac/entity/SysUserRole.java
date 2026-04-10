package com.devlovecode.aiperm.modules.system.rbac.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户角色关联实体
 */
@Data
@Entity
@Table(name = "sys_user_role")
@SQLRestriction("deleted = 0")
public class SysUserRole implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;

	private Long roleId;

	private Integer deleted;

	private LocalDateTime createTime;

	private String createBy;

}
