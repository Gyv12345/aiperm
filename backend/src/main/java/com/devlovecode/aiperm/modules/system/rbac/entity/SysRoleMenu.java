package com.devlovecode.aiperm.modules.system.rbac.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色菜单关联实体
 */
@Data
@Entity
@Table(name = "sys_role_menu")
@SQLRestriction("deleted = 0")
public class SysRoleMenu implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long roleId;

	private Long menuId;

	private Integer deleted;

	private LocalDateTime createTime;

	private String createBy;

}
