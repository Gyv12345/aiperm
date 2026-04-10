package com.devlovecode.aiperm.modules.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "缓存条目概览")
public class CacheEntryVO {

	@Schema(description = "缓存名称")
	private String cacheName;

	@Schema(description = "Redis Key 前缀")
	private String keyPrefix;

	@Schema(description = "估算 Key 数量")
	private Long estimatedSize;

	@Schema(description = "样例 TTL（秒）")
	private Long sampleTtl;

}
