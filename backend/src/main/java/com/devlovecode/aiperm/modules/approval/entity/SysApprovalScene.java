package com.devlovecode.aiperm.modules.approval.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "sys_approval_scene")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysApprovalScene extends BaseEntity {

	private String sceneCode;

	private String sceneName;

	private String businessType;

	private String platform;

	private String templateId;

	private Integer enabled;

	@Column(name = "handler_bean_name")
	private String handlerBeanName;

	@Column(name = "auto_submit_enabled")
	private Integer autoSubmitEnabled;

	@Column(name = "allow_duplicate_pending")
	private Integer allowDuplicatePending;

	private Integer timeoutHours;

	private String timeoutAction;

	@Column(name = "notify_template_code")
	private String notifyTemplateCode;

	private String remark;

}
