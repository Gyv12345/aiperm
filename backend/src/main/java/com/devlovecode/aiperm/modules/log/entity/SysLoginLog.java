package com.devlovecode.aiperm.modules.log.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "sys_login_log")
@SQLRestriction("coalesce(deleted, 0) = 0")
@Data
public class SysLoginLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;

	private String username;

	private String ip;

	private String location;

	private String browser;

	private String os;

	private Integer status;

	private String msg;

	private LocalDateTime loginTime;

	private Integer deleted;

	private LocalDateTime createTime;

}
