package com.devlovecode.aiperm.common.context;

/**
 * 数据权限上下文持有者
 * 使用 ThreadLocal 存储当前请求的数据权限 SQL 片段
 *
 * @author DevLoveCode
 */
public class DataScopeHolder {

    private static final ThreadLocal<String> SQL_HOLDER = new ThreadLocal<>();

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
     *
     * @return SQL 片段，未设置时返回空字符串
     */
    public static String get() {
        String sql = SQL_HOLDER.get();
        return sql != null ? sql : "";
    }

    /**
     * 清理当前线程的数据权限
     */
    public static void clear() {
        SQL_HOLDER.remove();
    }
}
