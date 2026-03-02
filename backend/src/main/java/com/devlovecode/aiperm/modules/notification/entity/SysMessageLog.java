package com.devlovecode.aiperm.modules.notification.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysMessageLog extends BaseEntity {
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
