package com.devlovecode.aiperm.common.annotation;

import com.devlovecode.aiperm.common.enums.AccessLimitScope;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口幂等注解
 *
 * @author shichenyang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

	/**
	 * 幂等锁有效时长（秒）
	 */
	long expireSeconds() default 5;

	/**
	 * 幂等作用域
	 */
	AccessLimitScope scope() default AccessLimitScope.USER;

	/**
	 * 自定义业务标识（默认使用请求路径）
	 */
	String key() default "";

	/**
	 * 幂等键请求头名
	 */
	String header() default "Idempotency-Key";

	/**
	 * 是否强制要求请求头中必须携带幂等键
	 */
	boolean requireHeader() default false;

	/**
	 * 触发幂等冲突时提示语
	 */
	String message() default "请勿重复提交";

}
