package com.devlovecode.aiperm.modules.enterprise.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 消息中心实体
 */
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
