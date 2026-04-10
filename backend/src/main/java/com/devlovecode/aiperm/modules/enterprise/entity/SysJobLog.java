package com.devlovecode.aiperm.modules.enterprise.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "sys_job_log")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysJobLog extends BaseEntity {

	private Long jobId;

	private String jobName;

	private String jobGroup;

	private String beanClass;

	private String triggerSource;

	private Integer status;

	private String message;

	@Column(columnDefinition = "TEXT")
	private String exceptionInfo;

	private LocalDateTime startTime;

	private LocalDateTime endTime;

	private Long costTime;

}
