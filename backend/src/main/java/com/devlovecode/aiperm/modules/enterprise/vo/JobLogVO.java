package com.devlovecode.aiperm.modules.enterprise.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "任务执行日志")
public class JobLogVO {

	@Schema(description = "日志ID")
	private Long id;

	@Schema(description = "任务ID")
	private Long jobId;

	@Schema(description = "任务名称")
	private String jobName;

	@Schema(description = "任务分组")
	private String jobGroup;

	@Schema(description = "执行目标")
	private String beanClass;

	@Schema(description = "触发来源")
	private String triggerSource;

	@Schema(description = "执行状态")
	private Integer status;

	@Schema(description = "执行结果")
	private String message;

	@Schema(description = "异常信息")
	private String exceptionInfo;

	@Schema(description = "开始时间")
	private LocalDateTime startTime;

	@Schema(description = "结束时间")
	private LocalDateTime endTime;

	@Schema(description = "耗时(ms)")
	private Long costTime;

}
