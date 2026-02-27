package com.devlovecode.aiperm.modules.mfa.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户2FA绑定记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserMfa extends BaseEntity {
    private Long userId;
    private String mfaType;       // TOTP
    private String secretKey;     // Base32 编码的密钥
    private LocalDateTime bindTime;
    private Integer status;       // 1启用，0禁用
}
