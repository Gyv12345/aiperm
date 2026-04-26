package com.devlovecode.aiperm.modules.approval.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "sys_approval_callback_log")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysApprovalCallbackLog extends BaseEntity {

	private String platform;

	private String sceneCode;

	private String platformInstanceId;

	private String callbackStatus;

	private String handleResult;

	private String errorMessage;

	@Column(columnDefinition = "longtext")
	private String payload;

	private LocalDateTime processedTime;

}
