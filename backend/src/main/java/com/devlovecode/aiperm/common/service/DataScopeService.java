package com.devlovecode.aiperm.common.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.enums.DataScopeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据权限服务
 *
 * @author DevLoveCode
 */
@Service
@RequiredArgsConstructor
public class DataScopeService {

    private final JdbcClient db;

    /**
     * 构建当前用户的数据权限 SQL
     *
     * @param deptAlias 部门表别名
     * @param userAlias 用户表别名
     * @return SQL 片段
     */
    public String buildDataScopeSql(String deptAlias, String userAlias) {
        // 未登录则无限制
        if (!StpUtil.isLogin()) {
            return "";
        }

        Long userId = StpUtil.getLoginIdAsLong();

        // 获取用户角色中最大的数据权限范围（数字越小权限越大）
        Integer dataScope = getMaxDataScope(userId);
        DataScopeEnum scopeEnum = DataScopeEnum.of(dataScope);

        return switch (scopeEnum) {
            case ALL -> "";
            case DEPT -> {
                Long deptId = getUserDeptId(userId);
                if (deptId == null || deptId == 0) {
                    yield "";
                }
                yield String.format(" AND %s.id = %d", deptAlias, deptId);
            }
            case DEPT_AND_CHILD -> {
                List<Long> deptIds = getDeptAndChildIds(userId);
                if (deptIds.isEmpty()) {
                    yield "";
                }
                yield String.format(" AND %s.id IN (%s)", deptAlias,
                        deptIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
            }
            case SELF -> String.format(" AND %s.id = %d", userAlias, userId);
        };
    }

    /**
     * 获取用户最大的数据权限范围（数字越小权限越大）
     */
    private Integer getMaxDataScope(Long userId) {
        String sql = """
            SELECT MIN(r.data_scope)
            FROM sys_role r
            JOIN sys_user_role ur ON r.id = ur.role_id
            WHERE ur.user_id = ? AND r.deleted = 0 AND r.status = 1
            """;
        Integer result = db.sql(sql)
                .param(userId)
                .query(Integer.class)
                .optional()
                .orElse(null);
        return result != null ? result : DataScopeEnum.ALL.getCode();
    }

    /**
     * 获取用户的部门 ID
     */
    private Long getUserDeptId(Long userId) {
        String sql = "SELECT dept_id FROM sys_user WHERE id = ? AND deleted = 0";
        return db.sql(sql)
                .param(userId)
                .query(Long.class)
                .optional()
                .orElse(0L);
    }

    /**
     * 获取用户部门及所有子部门 ID
     */
    private List<Long> getDeptAndChildIds(Long userId) {
        Long deptId = getUserDeptId(userId);
        if (deptId == null || deptId == 0) {
            return List.of();
        }

        // 查询部门及所有子部门（使用 ancestors 字段）
        String sql = """
            SELECT id FROM sys_dept
            WHERE deleted = 0 AND status = 1
            AND (id = ? OR FIND_IN_SET(?, ancestors))
            """;
        return db.sql(sql)
                .param(deptId)
                .param(deptId)
                .query(Long.class)
                .list();
    }
}
