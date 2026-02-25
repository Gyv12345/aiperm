package com.devlovecode.aiperm.modules.enterprise.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 定时任务实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysJob extends BaseEntity {

    private String jobName;

    private String jobGroup;

    private String cronExpression;

    private String beanClass;

    private Integer status;

    private String remark;
}
