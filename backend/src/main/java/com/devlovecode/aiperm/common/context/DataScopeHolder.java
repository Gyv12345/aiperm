package com.devlovecode.aiperm.common.context;

/**
 * 数据权限上下文持有者 使用 ThreadLocal 存储当前请求的数据权限 SQL 片段
 *
 * @author DevLoveCode
 */
public class DataScopeHolder {

	private static final ThreadLocal<String> SQL_HOLDER = new ThreadLocal<>();

	private static final ThreadLocal<Boolean> ENABLED_HOLDER = new ThreadLocal<>();

	private DataScopeHolder() {
	}

	/**
	 * 设置数据权限 SQL 片段
	 */
	public static void set(String sql) {
		SQL_HOLDER.set(sql);
	}

	/**
	 * 获取数据权限 SQL 片段
	 * @return SQL 片段，未设置或已禁用时返回空字符串
	 */
	public static String get() {
		// 如果已禁用，返回空字符串
		if (Boolean.FALSE.equals(ENABLED_HOLDER.get())) {
			return "";
		}
		String sql = SQL_HOLDER.get();
		return sql != null ? sql : "";
	}

	/**
	 * 禁用当前线程的数据权限 用于 SSE 等异步场景
	 */
	public static void disable() {
		ENABLED_HOLDER.set(false);
	}

	/**
	 * 启用当前线程的数据权限
	 */
	public static void enable() {
		ENABLED_HOLDER.set(true);
	}

	/**
	 * 检查数据权限是否启用
	 */
	public static boolean isEnabled() {
		Boolean enabled = ENABLED_HOLDER.get();
		return enabled == null || enabled;
	}

	/**
	 * 清理当前线程的数据权限
	 */
	public static void clear() {
		SQL_HOLDER.remove();
		ENABLED_HOLDER.remove();
	}

}
