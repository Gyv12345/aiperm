package com.devlovecode.aiperm.modules.notification.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysMessageTemplate extends BaseEntity {
    private String templateCode;
    private String templateName;
    private String category;
    private String platform;
    private String title;
    private String content;
}
