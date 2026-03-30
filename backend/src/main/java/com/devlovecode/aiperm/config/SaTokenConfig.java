package com.devlovecode.aiperm.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token配置
 *
 * @author DevLoveCode
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 注册Sa-Token拦截器，校验规则为StpUtil.checkLogin()登录校验
		registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
			.addPathPatterns("/**")
			.excludePathPatterns(
					// 排除认证相关接口
					"/auth/login", "/auth/unified-login", "/auth/captcha", "/auth/login-config",
					// 排除验证码发送接口
					"/captcha/**",
					// 排除 OAuth 登录接口
					"/oauth/login/**",
					// 审批回调接口（来自企业IM服务端）
					"/approval/callback/**",
					// 排除Swagger相关
					"/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs/**", "/webjars/**", "/doc.html",
					// 排除静态资源
					"/favicon.ico", "/error")
			.order(0);
	}

}
