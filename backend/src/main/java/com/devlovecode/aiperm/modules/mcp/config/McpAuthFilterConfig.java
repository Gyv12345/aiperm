package com.devlovecode.aiperm.modules.mcp.config;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * MCP 端点鉴权：校验 X-API-Key 请求头。
 *
 * <p>Sa-Token 登录拦截器已放行 /mcp/**（AI 客户端没有登录态），改由本过滤器做 API Key 鉴权。
 * 未配置 api-key 时整个 /mcp 端点一律 404，避免在未启用状态下暴露端点。
 */
@Configuration
@RequiredArgsConstructor
public class McpAuthFilterConfig {

	private static final String API_KEY_HEADER = "X-API-Key";

	private final McpApiProperties properties;

	@Bean
	public FilterRegistrationBean<Filter> mcpAuthFilter() {
		Filter filter = (request, response, chain) -> {
			HttpServletResponse res = (HttpServletResponse) response;
			String apiKey = properties.getApiKey();
			if (apiKey == null || apiKey.isBlank()) {
				// 未配置即视为功能未启用
				res.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			String provided = ((HttpServletRequest) request).getHeader(API_KEY_HEADER);
			boolean valid = provided != null && MessageDigest.isEqual(apiKey.getBytes(StandardCharsets.UTF_8),
					provided.getBytes(StandardCharsets.UTF_8));
			if (!valid) {
				res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "无效的 MCP API Key");
				return;
			}
			chain.doFilter(request, response);
		};

		FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(filter);
		registration.addUrlPatterns("/mcp/*");
		registration.setName("mcpAuthFilter");
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
		return registration;
	}

}
