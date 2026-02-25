package com.devlovecode.aiperm.modules.enterprise.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 公告通知实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNotice extends BaseEntity {

    private String title;

    private String content;

    private Integer type;

    private Integer status;

    private LocalDateTime publishTime;
}
