package com.devlovecode.aiperm.common.repository;

import com.devlovecode.aiperm.common.domain.PageResult;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository 通用基类
 * 提供基础 CRUD 操作
 *
 * @param <T> 实体类型
 */
public abstract class BaseRepository<T> {

    protected final JdbcClient db;
    protected final String tableName;
    protected final Class<T> entityClass;

    protected BaseRepository(JdbcClient db, String tableName, Class<T> entityClass) {
        this.db = db;
        this.tableName = tableName;
        this.entityClass = entityClass;
    }

    /**
     * 根据 ID 查询（排除已删除）
     */
    public Optional<T> findById(Long id) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = :id AND deleted = 0";
        return db.sql(sql)
                .param("id", id)
                .query(entityClass)
                .optional();
    }

    /**
     * 查询所有（排除已删除）
     */
    public List<T> findAll() {
        String sql = "SELECT * FROM " + tableName + " WHERE deleted = 0 ORDER BY create_time DESC";
        return db.sql(sql).query(entityClass).list();
    }

    /**
     * 软删除
     */
    public int deleteById(Long id) {
        String sql = "UPDATE " + tableName + " SET deleted = 1, update_time = :updateTime WHERE id = :id";
        return db.sql(sql)
                .param("id", id)
                .param("updateTime", LocalDateTime.now())
                .update();
    }

    /**
     * 批量软删除
     */
    public int deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        // 使用参数化查询防止 SQL 注入
        String placeholders = ids.stream().map(id -> "?").collect(java.util.stream.Collectors.joining(","));
        String sql = "UPDATE " + tableName + " SET deleted = 1, update_time = ? WHERE id IN (" + placeholders + ")";

        // 构建参数列表：先添加 updateTime，再添加所有 id
        List<Object> params = new java.util.ArrayList<>();
        params.add(LocalDateTime.now());
        params.addAll(ids);

        return db.sql(sql)
                .params(params)
                .update();
    }

    /**
     * 统计总数（排除已删除）
     */
    public long count() {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE deleted = 0";
        return db.sql(sql).query(Long.class).single();
    }

    /**
     * 检查是否存在（排除已删除）
     */
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE id = :id AND deleted = 0";
        Integer count = db.sql(sql).param("id", id).query(Integer.class).single();
        return count > 0;
    }

    /**
     * 通用分页查询
     */
    protected PageResult<T> queryPage(String whereClause, List<Object> params, int pageNum, int pageSize) {
        // 处理 whereClause：可能以 "WHERE " 开头，也可能为空
        String condition = "";
        if (whereClause != null && !whereClause.isEmpty()) {
            if (whereClause.trim().startsWith("WHERE ")) {
                condition = " AND " + whereClause.substring(6).trim(); // 去掉 "WHERE "
            } else {
                condition = " AND " + whereClause.trim();
            }
        }

        // 查总数
        String countSql = "SELECT COUNT(*) FROM " + tableName + " WHERE deleted = 0" + condition;
        Long total = db.sql(countSql).params(params).query(Long.class).single();

        if (total == null || total == 0) {
            return PageResult.empty((long) pageNum, (long) pageSize);
        }

        // 查列表
        String listSql = "SELECT * FROM " + tableName + " WHERE deleted = 0" + condition +
                " ORDER BY create_time DESC LIMIT ? OFFSET ?";
        List<Object> listParams = new java.util.ArrayList<>(params);
        listParams.add(pageSize);
        listParams.add((pageNum - 1) * pageSize);

        List<T> list = db.sql(listSql)
                .params(listParams)
                .query(entityClass)
                .list();

        return PageResult.of(total, list, (long) pageNum, (long) pageSize);
    }
}
