package com.devlovecode.aiperm.modules.enterprise.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 消息中心实体
 */
@Entity
@Table(name = "sys_message")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMessage extends BaseEntity {

	private Long senderId;

	private Long receiverId;

	private String title;

	private String content;

	private Integer isRead;

	private LocalDateTime readTime;

}
