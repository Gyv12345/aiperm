package com.devlovecode.aiperm.common.repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态 SQL 构建器
 * 简化条件查询的 SQL 拼接
 */
public class SqlBuilder {

    private final StringBuilder sql;
    private final List<Object> params;

    public SqlBuilder() {
        this.sql = new StringBuilder();
        this.params = new ArrayList<>();
    }

    /**
     * 添加 WHERE 条件
     */
    public SqlBuilder where(String condition, Object... args) {
        if (!sql.isEmpty()) {
            sql.append(" AND ");
        } else {
            sql.append(" WHERE ");
        }
        sql.append(condition);
        for (Object arg : args) {
            params.add(arg);
        }
        return this;
    }

    /**
     * 添加 WHERE 条件（条件为 true 时）
     */
    public SqlBuilder whereIf(boolean condition, String clause, Object arg) {
        if (condition) {
            where(clause, arg);
        }
        return this;
    }

    /**
     * 添加 LIKE 条件（自动添加通配符）
     */
    public SqlBuilder likeIf(boolean condition, String column, String value) {
        if (condition && value != null && !value.isBlank()) {
            where(column + " LIKE ?", "%" + value + "%");
        }
        return this;
    }

    /**
     * 添加 ORDER BY
     */
    public SqlBuilder orderBy(String clause) {
        sql.append(" ORDER BY ").append(clause);
        return this;
    }

    /**
     * 获取 WHERE 子句
     */
    public String getWhereClause() {
        return sql.toString();
    }

    /**
     * 获取参数列表
     */
    public List<Object> getParams() {
        return params;
    }

    /**
     * 清空构建器
     */
    public void clear() {
        sql.setLength(0);
        params.clear();
    }
}
