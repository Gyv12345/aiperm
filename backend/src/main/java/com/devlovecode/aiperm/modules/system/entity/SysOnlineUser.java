package com.devlovecode.aiperm.modules.system.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysOnlineUser extends BaseEntity {
    private Long userId;
    private String username;
    private String token;
    private String ip;
    private String browser;
    private String os;
    private LocalDateTime loginTime;
    private LocalDateTime lastAccessTime;
}
