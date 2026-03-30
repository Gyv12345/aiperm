package com.devlovecode.aiperm.common.annotation;

import com.devlovecode.aiperm.common.enums.AccessLimitScope;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解
 *
 * @author shichenyang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

	/**
	 * 时间窗口内最多允许的请求数
	 */
	int count() default 60;

	/**
	 * 时间窗口（秒）
	 */
	int windowSeconds() default 60;

	/**
	 * 限流作用域
	 */
	AccessLimitScope scope() default AccessLimitScope.IP;

	/**
	 * 自定义业务标识（默认使用请求路径）
	 */
	String key() default "";

	/**
	 * 触发限流时提示语
	 */
	String message() default "请求过于频繁，请稍后重试";

}
