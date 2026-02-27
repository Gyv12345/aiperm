package com.devlovecode.aiperm.modules.log.repository;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.modules.log.entity.SysLoginLog;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 登录日志 Repository
 */
@Repository
public class LoginLogRepository {

    private final JdbcClient db;
    private static final String TABLE_NAME = "sys_login_log";

    public LoginLogRepository(JdbcClient db) {
        this.db = db;
    }

    /**
     * 根据用户ID分页查询登录日志
     */
    public PageResult<SysLoginLog> queryPageByUserId(Long userId, int pageNum, int pageSize) {
        // 查总数
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE user_id = ?";
        Long total = db.sql(countSql).param(userId).query(Long.class).single();

        if (total == null || total == 0) {
            return PageResult.empty((long) pageNum, (long) pageSize);
        }

        // 查列表
        String listSql = "SELECT * FROM " + TABLE_NAME +
                " WHERE user_id = ? ORDER BY login_time DESC LIMIT ? OFFSET ?";
        List<SysLoginLog> list = db.sql(listSql)
                .params(userId, pageSize, (pageNum - 1) * pageSize)
                .query(SysLoginLog.class)
                .list();

        return PageResult.of(total, list, (long) pageNum, (long) pageSize);
    }
}
