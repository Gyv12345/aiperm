package com.devlovecode.aiperm.modules.system.rbac.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单实体类
 *
 * @author devlovecode
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_menu")
public class SysMenu extends BaseEntity {

	private String menuName;

	private Long parentId;

	private String menuType;

	private Integer sort;

	private String path;

	private String component;

	private String perms;

	private String icon;

	private Integer isExternal;

	private Integer isCache;

	private Integer visible;

	private Integer status;

	private String remark;

	@Transient
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<SysMenu> children = new ArrayList<>();

	/**
	 * 历史兼容字段：数据库已统一为 perms，不再持久化 permission 列。
	 */
	@Transient
	@JsonProperty("permission")
	public String getPermission() {
		return this.perms;
	}

	public void setPermission(String permission) {
		this.perms = permission;
	}

}
