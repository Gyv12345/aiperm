package com.devlovecode.aiperm.modules.mfa.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 2FA策略配置实体
 * 定义哪些API需要2FA验证
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMfaPolicy extends BaseEntity {
    private String name;           // 策略名称
    private String permPattern;    // 权限标识匹配（支持通配符*）
    private String apiPattern;     // API路径匹配（支持通配符*）
    private Integer enabled;       // 1启用，0禁用
}
