package com.devlovecode.aiperm.modules.notification.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "sys_message_template")
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
