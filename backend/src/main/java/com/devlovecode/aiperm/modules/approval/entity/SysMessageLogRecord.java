package com.devlovecode.aiperm.modules.approval.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "sys_message_log")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMessageLogRecord extends BaseEntity {

	private String templateCode;

	private String platform;

	private Long receiverId;

	private String platformUserId;

	private String title;

	private String content;

	private String status;

	private String errorMsg;

	private LocalDateTime sendTime;

}
