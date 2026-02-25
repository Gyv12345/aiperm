package com.devlovecode.aiperm.modules.system.repository;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.common.repository.SqlBuilder;
import com.devlovecode.aiperm.modules.system.entity.SysRole;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class RoleRepository extends BaseRepository<SysRole> {

    public RoleRepository(JdbcClient db) {
        super(db, "sys_role", SysRole.class);
    }

    /**
     * 插入角色
     */
    public void insert(SysRole entity) {
        String sql = """
            INSERT INTO sys_role (role_name, role_code, sort, status, remark, is_builtin, deleted, version, create_time, create_by)
            VALUES (:roleName, :roleCode, :sort, :status, :remark, :isBuiltin, 0, 0, :createTime, :createBy)
            """;
        db.sql(sql)
                .param("roleName", entity.getRoleName())
                .param("roleCode", entity.getRoleCode())
                .param("sort", entity.getSort())
                .param("status", entity.getStatus())
                .param("remark", entity.getRemark())
                .param("isBuiltin", entity.getIsBuiltin() != null ? entity.getIsBuiltin() : 0)
                .param("createTime", LocalDateTime.now())
                .param("createBy", entity.getCreateBy())
                .update();
    }

    /**
     * 更新角色
     */
    public int update(SysRole entity) {
        String sql = """
            UPDATE sys_role
            SET role_name = :roleName, role_code = :roleCode, sort = :sort, status = :status, remark = :remark,
                update_time = :updateTime, update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("roleName", entity.getRoleName())
                .param("roleCode", entity.getRoleCode())
                .param("sort", entity.getSort())
                .param("status", entity.getStatus())
                .param("remark", entity.getRemark())
                .param("updateTime", LocalDateTime.now())
                .param("updateBy", entity.getUpdateBy())
                .param("id", entity.getId())
                .update();
    }

    /**
     * 检查角色编码是否存在
     */
    public boolean existsByRoleCode(String roleCode) {
        String sql = "SELECT COUNT(*) FROM sys_role WHERE role_code = :roleCode AND deleted = 0";
        Integer count = db.sql(sql).param("roleCode", roleCode).query(Integer.class).single();
        return count != null && count > 0;
    }

    /**
     * 检查角色编码是否存在（排除指定ID）
     */
    public boolean existsByRoleCodeExcludeId(String roleCode, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM sys_role WHERE role_code = :roleCode AND id != :id AND deleted = 0";
        Integer count = db.sql(sql)
                .param("roleCode", roleCode)
                .param("id", excludeId)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    /**
     * 分页查询
     */
    public PageResult<SysRole> queryPage(String roleName, String roleCode, Integer status, int pageNum, int pageSize) {
        SqlBuilder sb = new SqlBuilder();
        sb.likeIf(roleName != null && !roleName.isBlank(), "role_name", roleName)
          .likeIf(roleCode != null && !roleCode.isBlank(), "role_code", roleCode)
          .whereIf(status != null, "status = ?", status);

        return queryPage(sb.getWhereClause(), sb.getParams(), pageNum, pageSize);
    }

    /**
     * 检查是否为内置角色
     */
    public boolean isBuiltin(Long id) {
        String sql = "SELECT is_builtin FROM sys_role WHERE id = :id AND deleted = 0";
        Integer isBuiltin = db.sql(sql).param("id", id).query(Integer.class).single();
        return isBuiltin != null && isBuiltin == 1;
    }

    // ========== 角色菜单关联 ==========

    /**
     * 获取角色的菜单ID列表
     */
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        String sql = "SELECT menu_id FROM sys_role_menu WHERE role_id = :roleId AND deleted = 0";
        return db.sql(sql).param("roleId", roleId).query(Long.class).list();
    }

    /**
     * 删除角色的所有菜单关联
     */
    public void deleteRoleMenus(Long roleId) {
        String sql = "UPDATE sys_role_menu SET deleted = 1 WHERE role_id = :roleId AND deleted = 0";
        db.sql(sql).param("roleId", roleId).update();
    }

    /**
     * 添加角色菜单关联
     */
    public void insertRoleMenu(Long roleId, Long menuId) {
        String sql = """
            INSERT INTO sys_role_menu (role_id, menu_id, deleted, create_time)
            VALUES (:roleId, :menuId, 0, :createTime)
            ON DUPLICATE KEY UPDATE deleted = 0, create_time = :createTime
            """;
        db.sql(sql)
                .param("roleId", roleId)
                .param("menuId", menuId)
                .param("createTime", LocalDateTime.now())
                .update();
    }

    // ========== 用户角色关联 ==========

    /**
     * 检查角色是否被用户使用
     */
    public boolean isUsedByUser(Long roleId) {
        String sql = "SELECT COUNT(*) FROM sys_user_role WHERE role_id = :roleId AND deleted = 0";
        Integer count = db.sql(sql).param("roleId", roleId).query(Integer.class).single();
        return count != null && count > 0;
    }
}
