package com.devlovecode.aiperm.modules.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "缓存监控概览")
public class CacheMonitorVO {

	@Schema(description = "Redis 已用内存文本")
	private String usedMemoryHuman;

	@Schema(description = "连接客户端数")
	private Long connectedClients;

	@Schema(description = "数据库总 Key 数")
	private Long totalKeys;

	@Schema(description = "命中次数")
	private Long hits;

	@Schema(description = "未命中次数")
	private Long misses;

	@Schema(description = "命中率")
	private Double hitRate;

	@Schema(description = "缓存条目列表")
	private List<CacheEntryVO> entries = new ArrayList<>();

}
