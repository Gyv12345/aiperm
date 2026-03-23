package com.devlovecode.aiperm.modules.log.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "sys_login_log")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysLoginLog extends BaseEntity {
    private Long userId;
    private String username;
    private String ip;
    private String location;
    private String browser;
    private String os;
    private Integer status;
    private String msg;
    private LocalDateTime loginTime;
}
