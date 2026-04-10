package com.devlovecode.aiperm.modules.system.rbac.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_dict_type")
public class SysDictType extends BaseEntity {

	private String dictName;

	private String dictType;

	private Integer status;

	private String remark;

}
