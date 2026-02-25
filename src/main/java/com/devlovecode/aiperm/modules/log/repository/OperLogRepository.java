package com.devlovecode.aiperm.modules.log.repository;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.modules.log.entity.SysOperLog;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OperLogRepository {

    private final JdbcClient db;
    private static final String TABLE_NAME = "sys_oper_log";

    public OperLogRepository(JdbcClient db) {
        this.db = db;
    }

    /**
     * 插入日志
     */
    public void insert(SysOperLog entity) {
        String sql = """
            INSERT INTO sys_oper_log (title, oper_type, method, request_method, oper_url, oper_ip,
                oper_param, json_result, status, error_msg, cost_time, oper_user, oper_name, create_time)
            VALUES (:title, :operType, :method, :requestMethod, :operUrl, :operIp,
                :operParam, :jsonResult, :status, :errorMsg, :costTime, :operUser, :operName, :createTime)
            """;
        db.sql(sql)
                .param("title", entity.getTitle())
                .param("operType", entity.getOperType())
                .param("method", entity.getMethod())
                .param("requestMethod", entity.getRequestMethod())
                .param("operUrl", entity.getOperUrl())
                .param("operIp", entity.getOperIp())
                .param("operParam", entity.getOperParam())
                .param("jsonResult", entity.getJsonResult())
                .param("status", entity.getStatus())
                .param("errorMsg", entity.getErrorMsg())
                .param("costTime", entity.getCostTime())
                .param("operUser", entity.getOperUser())
                .param("operName", entity.getOperName())
                .param("createTime", entity.getCreateTime())
                .update();
    }

    /**
     * 分页查询
     */
    public PageResult<SysOperLog> queryPage(String title, Integer status, int pageNum, int pageSize) {
        StringBuilder whereClause = new StringBuilder();
        List<Object> params = new java.util.ArrayList<>();

        if (title != null && !title.isBlank()) {
            whereClause.append(" AND title LIKE ?");
            params.add("%" + title + "%");
        }
        if (status != null) {
            whereClause.append(" AND status = ?");
            params.add(status);
        }

        // 查总数
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE 1=1" + whereClause;
        Long total = db.sql(countSql).params(params).query(Long.class).single();

        if (total == null || total == 0) {
            return PageResult.empty((long) pageNum, (long) pageSize);
        }

        // 查列表
        String listSql = "SELECT * FROM " + TABLE_NAME + " WHERE 1=1" + whereClause +
                " ORDER BY create_time DESC LIMIT ? OFFSET ?";
        List<Object> listParams = new java.util.ArrayList<>(params);
        listParams.add(pageSize);
        listParams.add((pageNum - 1) * pageSize);

        List<SysOperLog> list = db.sql(listSql)
                .params(listParams)
                .query(SysOperLog.class)
                .list();

        return PageResult.of(total, list, (long) pageNum, (long) pageSize);
    }

    /**
     * 根据ID查询
     */
    public java.util.Optional<SysOperLog> findById(Long id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = :id";
        return db.sql(sql).param("id", id).query(SysOperLog.class).optional();
    }

    /**
     * 删除
     */
    public int deleteById(Long id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = :id";
        return db.sql(sql).param("id", id).update();
    }

    /**
     * 清空所有
     */
    public void deleteAll() {
        String sql = "DELETE FROM " + TABLE_NAME;
        db.sql(sql).update();
    }
}
