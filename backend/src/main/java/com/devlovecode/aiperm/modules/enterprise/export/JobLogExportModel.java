package com.devlovecode.aiperm.modules.enterprise.export;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class JobLogExportModel {

	@ExcelProperty("任务ID")
	private Long jobId;

	@ExcelProperty("任务名称")
	private String jobName;

	@ExcelProperty("任务分组")
	private String jobGroup;

	@ExcelProperty("执行目标")
	private String beanClass;

	@ExcelProperty("触发来源")
	private String triggerSource;

	@ExcelProperty("执行状态")
	private String statusText;

	@ExcelProperty("执行结果")
	private String message;

	@ExcelProperty("开始时间")
	private String startTime;

	@ExcelProperty("结束时间")
	private String endTime;

	@ExcelProperty("耗时(ms)")
	private Long costTime;

}
