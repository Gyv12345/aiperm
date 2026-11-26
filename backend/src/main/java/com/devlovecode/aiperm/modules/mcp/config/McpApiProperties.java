package com.devlovecode.aiperm.modules.mcp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MCP Server 应用侧配置。
 *
 * <p>服务端本身（传输/开关）由 spring.ai.mcp.server.* 配置；这里只放应用层的 API Key。
 * api-key 为空表示 MCP 功能未启用，{@link McpAuthFilterConfig} 会把 /mcp 端点全部拒绝为 404。
 */
@Data
@Component
@ConfigurationProperties(prefix = "aiperm.mcp")
public class McpApiProperties {

	/** 客户端通过 X-API-Key 请求头携带；建议为随机长串（生产环境用 MCP_API_KEY 环境变量注入） */
	private String apiKey;

}
