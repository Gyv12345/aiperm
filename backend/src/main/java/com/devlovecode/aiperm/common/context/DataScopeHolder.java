package com.devlovecode.aiperm.common.context;

/**
 * 数据权限上下文持有者：使用 ThreadLocal 存储当前请求的数据权限判定结果。
 *
 * <p>由 {@code DataScopeInterceptor} 在请求开始时填充，业务 Service 通过
 * {@code DataScopeService.getDataScopeContext()} 或直接读取本 Holder 消费。
 * SSE 等异步场景可通过 {@link #disable()} 临时关闭。
 *
 * @author DevLoveCode
 */
public class DataScopeHolder {

	private static final ThreadLocal<DataScopeContext> CONTEXT_HOLDER = new ThreadLocal<>();

	private static final ThreadLocal<Boolean> ENABLED_HOLDER = new ThreadLocal<>();

	private DataScopeHolder() {
	}

	/**
	 * 设置数据权限上下文。
	 */
	public static void set(DataScopeContext context) {
		CONTEXT_HOLDER.set(context);
	}

	/**
	 * 获取数据权限上下文。
	 * @return 上下文；未设置或已禁用时返回 ALL（不过滤）。
	 */
	public static DataScopeContext get() {
		// 如果已禁用，返回 ALL（不过滤）
		if (Boolean.FALSE.equals(ENABLED_HOLDER.get())) {
			return DataScopeContext.all();
		}
		DataScopeContext context = CONTEXT_HOLDER.get();
		return context != null ? context : DataScopeContext.all();
	}

	/**
	 * 禁用当前线程的数据权限，用于 SSE 等异步场景。
	 */
	public static void disable() {
		ENABLED_HOLDER.set(false);
	}

	/**
	 * 启用当前线程的数据权限。
	 */
	public static void enable() {
		ENABLED_HOLDER.set(true);
	}

	/**
	 * 检查数据权限是否启用。
	 */
	public static boolean isEnabled() {
		Boolean enabled = ENABLED_HOLDER.get();
		return enabled == null || enabled;
	}

	/**
	 * 清理当前线程的数据权限。
	 */
	public static void clear() {
		CONTEXT_HOLDER.remove();
		ENABLED_HOLDER.remove();
	}

}
