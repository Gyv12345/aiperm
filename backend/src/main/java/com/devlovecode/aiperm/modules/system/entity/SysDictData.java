package com.devlovecode.aiperm.modules.system.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_dict_data")
public class SysDictData extends BaseEntity {

	private String dictType;

	private String dictLabel;

	private String dictValue;

	private Integer sort;

	private Integer status;

	private String listClass;

	private String remark;

}
