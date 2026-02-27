package com.devlovecode.aiperm.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.modules.mcp.security.McpSecurityInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token配置
 *
 * @author DevLoveCode
 */
@Configuration
@RequiredArgsConstructor
public class SaTokenConfig implements WebMvcConfigurer {

    private final McpSecurityInterceptor mcpSecurityInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 MCP 安全拦截器（优先级最高）
        registry.addInterceptor(mcpSecurityInterceptor)
                .addPathPatterns("/mcp/**")
                .order(0);

        // 注册Sa-Token拦截器，校验规则为StpUtil.checkLogin()登录校验
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 排除认证相关接口
                        "/auth/login",
                        "/auth/captcha",
                        // 排除 MCP 端点（由 McpSecurityInterceptor 单独处理）
                        "/mcp/**",
                        // 排除Swagger相关
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/doc.html",
                        // 排除静态资源
                        "/favicon.ico",
                        "/error"
                )
                .order(1);
    }
}
