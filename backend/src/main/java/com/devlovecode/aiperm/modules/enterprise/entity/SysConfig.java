package com.devlovecode.aiperm.modules.enterprise.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置实体
 */
@Entity
@Table(name = "sys_config")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysConfig extends BaseEntity {

	private String configKey;

	private String configValue;

	private String configType;

	private String remark;

}
