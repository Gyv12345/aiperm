package com.devlovecode.aiperm.common.enums;

/**
 * 限流/幂等的作用域
 */
public enum AccessLimitScope {

	/**
	 * 基于当前登录用户（未登录时回退为 IP）
	 */
	USER,

	/**
	 * 基于客户端 IP
	 */
	IP,

	/**
	 * 全局共享
	 */
	GLOBAL

}
