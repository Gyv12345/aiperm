package com.devlovecode.aiperm.modules.enterprise.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 公告通知实体
 */
@Entity
@Table(name = "sys_notice")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNotice extends BaseEntity {

	private String title;

	private String content;

	private Integer type;

	private Integer status;

	private LocalDateTime publishTime;

}
