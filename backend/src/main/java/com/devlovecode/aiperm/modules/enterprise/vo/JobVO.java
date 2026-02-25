package com.devlovecode.aiperm.modules.enterprise.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "定时任务响应VO")
public class JobVO {

    @Schema(description = "任务ID")
    private Long id;

    @Schema(description = "任务名称")
    private String jobName;

    @Schema(description = "任务分组")
    private String jobGroup;

    @Schema(description = "Cron表达式")
    private String cronExpression;

    @Schema(description = "执行类")
    private String beanClass;

    @Schema(description = "状态：0-暂停 1-运行")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
