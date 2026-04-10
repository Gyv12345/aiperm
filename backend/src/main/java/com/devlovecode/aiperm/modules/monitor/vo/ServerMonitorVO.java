package com.devlovecode.aiperm.modules.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "服务监控概览")
public class ServerMonitorVO {

	@Schema(description = "应用名称")
	private String appName;

	@Schema(description = "激活环境")
	private List<String> activeProfiles = new ArrayList<>();

	@Schema(description = "健康状态")
	private String status;

	@Schema(description = "运行时长(毫秒)")
	private Long uptime;

	@Schema(description = "Java 版本")
	private String javaVersion;

	@Schema(description = "操作系统")
	private String osName;

	@Schema(description = "CPU 核数")
	private Integer processors;

	@Schema(description = "系统 CPU 使用率")
	private Double systemCpuUsage;

	@Schema(description = "进程 CPU 使用率")
	private Double processCpuUsage;

	@Schema(description = "堆内存已用")
	private Long heapUsed;

	@Schema(description = "堆内存最大")
	private Long heapMax;

	@Schema(description = "非堆内存已用")
	private Long nonHeapUsed;

	@Schema(description = "非堆内存最大")
	private Long nonHeapMax;

	@Schema(description = "磁盘总量")
	private Long diskTotal;

	@Schema(description = "磁盘可用")
	private Long diskUsable;

	@Schema(description = "活动线程数")
	private Integer liveThreads;

	@Schema(description = "守护线程数")
	private Integer daemonThreads;

	@Schema(description = "峰值线程数")
	private Integer peakThreads;

	@Schema(description = "健康检查组件")
	private List<HealthComponentVO> healthComponents = new ArrayList<>();

}
