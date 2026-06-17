package com.devlovecode.aiperm.modules.system.rbac.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门实体类
 *
 * @author devlovecode
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_dept")
public class SysDept extends BaseEntity {

	private String deptName;

	private Long parentId;

	/** 祖级列表，逗号分隔的祖先部门ID，如 "0,1,5"。根部门为 "0" */
	private String ancestors;

	/** 部门编码 */
	private String deptCode;

	private Integer sort;

	private String leader;

	private String phone;

	private String email;

	private Integer status;

	private String remark;

	@Transient
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<SysDept> children = new ArrayList<>();

}
