package com.devlovecode.aiperm.modules.enterprise.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "定时任务数据")
public class JobDTO {

    // ========== 分页查询参数（仅 Query 场景）==========

    @JsonView(Views.Query.class)
    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @JsonView(Views.Query.class)
    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;

    // ========== 业务字段（多场景复用）==========

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "任务名称")
    @NotBlank(message = "任务名称不能为空", groups = {Views.Create.class, Views.Update.class})
    @Size(max = 100, message = "任务名称不能超过100个字符")
    private String jobName;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "任务分组")
    @Size(max = 50, message = "任务分组不能超过50个字符")
    private String jobGroup;

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "Cron表达式")
    @NotBlank(message = "Cron表达式不能为空", groups = {Views.Create.class, Views.Update.class})
    @Size(max = 100, message = "Cron表达式不能超过100个字符")
    private String cronExpression;

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "执行类")
    @NotBlank(message = "执行类不能为空", groups = {Views.Create.class, Views.Update.class})
    @Size(max = 200, message = "执行类不能超过200个字符")
    @JsonAlias("invokeTarget")
    private String beanClass;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "状态：0-暂停 1-运行")
    private Integer status;

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "错过策略（兼容字段）")
    private Integer misfirePolicy;

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "并发策略（兼容字段）")
    private Integer concurrent;

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "备注")
    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;

    @JsonProperty("invokeTarget")
    @Schema(description = "执行目标（前端兼容字段）")
    public String getInvokeTarget() {
        return beanClass;
    }

    @JsonProperty("invokeTarget")
    public void setInvokeTarget(String invokeTarget) {
        this.beanClass = invokeTarget;
    }
}
