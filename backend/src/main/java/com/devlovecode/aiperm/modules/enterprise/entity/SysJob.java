package com.devlovecode.aiperm.modules.enterprise.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 定时任务实体
 */
@Entity
@Table(name = "sys_job")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysJob extends BaseEntity {

	private String jobName;

	private String jobGroup;

	private String cronExpression;

	private String beanClass;

	private Integer status;

	private String remark;

}
