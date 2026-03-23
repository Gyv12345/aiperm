package com.devlovecode.aiperm.modules.system.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.system.entity.SysRole;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoleRepository extends BaseJpaRepository<SysRole> {

    /**
     * 检查角色编码是否存在
     */
    boolean existsByRoleCode(String roleCode);

    /**
     * 检查角色编码是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(r) > 0 FROM SysRole r WHERE r.roleCode = :roleCode AND r.id != :id AND r.deleted = 0")
    boolean existsByRoleCodeExcludeId(@Param("roleCode") String roleCode, @Param("id") Long excludeId);

    /**
     * 获取角色的菜单ID列表
     */
    @Query("SELECT rm.menuId FROM SysRoleMenu rm WHERE rm.roleId = :roleId AND rm.deleted = 0")
    List<Long> getMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 检查角色是否被用户使用
     */
    @Query("SELECT COUNT(ur) > 0 FROM SysUserRole ur WHERE ur.roleId = :roleId AND ur.deleted = 0")
    boolean isUsedByUser(@Param("roleId") Long roleId);

    /**
     * 删除角色的所有菜单关联（软删除）
     */
    @Query("UPDATE SysRoleMenu rm SET rm.deleted = 1, rm.updateTime = :updateTime WHERE rm.roleId = :roleId AND rm.deleted = 0")
    void deleteRoleMenus(@Param("roleId") Long roleId, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 添加角色菜单关联
     */
    @Query(value = """
        INSERT INTO sys_role_menu (role_id, menu_id, deleted, create_time, update_time)
        VALUES (:roleId, :menuId, 0, :createTime, :createTime)
        ON DUPLICATE KEY UPDATE deleted = 0, create_time = :createTime
        """, nativeQuery = true)
    void insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId, @Param("createTime") LocalDateTime createTime);
}
