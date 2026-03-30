package com.devlovecode.aiperm.config;

import com.devlovecode.aiperm.common.interceptor.DataScopeInterceptor;
import com.devlovecode.aiperm.modules.mfa.interceptor.MfaInterceptor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 *
 * @author DevLoveCode
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

	private final DataScopeInterceptor dataScopeInterceptor;

	private final MfaInterceptor mfaInterceptor;

	/**
	 * 本地 OSS 文件访问映射
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/files/**").addResourceLocations("file:./uploads/");
	}

	/**
	 * 跨域配置
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOriginPatterns("*")
			.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
			.allowedHeaders("*")
			.allowCredentials(true)
			.maxAge(3600);
	}

	/**
	 * 拦截器配置
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 数据权限拦截器
		registry.addInterceptor(dataScopeInterceptor)
			.addPathPatterns("/**")
			.excludePathPatterns("/auth/**", "/error", "/v3/api-docs/**", "/swagger-ui/**");

		// 2FA 拦截器（检查敏感操作是否需要二次验证）
		registry.addInterceptor(mfaInterceptor)
			.addPathPatterns("/system/**", "/enterprise/**", "/mfa/unbind")
			.excludePathPatterns("/auth/**", "/captcha/**", "/error", "/v3/api-docs/**", "/swagger-ui/**",
					"/mfa/status", "/mfa/bind/**", "/mfa/verify");
	}

	/**
	 * OpenAPI配置
	 */
	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI().info(new Info().title("AiPerm RBAC权限系统API")
			.version("1.0.0")
			.description("基于Spring Boot 4 + Sa-Token的RBAC权限管理系统")
			.contact(new Contact().name("DevLoveCode").email("dev@lovecode.com"))
			.license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0.html")));
	}

	/**
	 * RestTemplate Bean（用于 OAuth HTTP 调用）
	 */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
