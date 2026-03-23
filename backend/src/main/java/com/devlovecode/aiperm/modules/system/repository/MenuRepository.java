package com.devlovecode.aiperm.modules.system.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.system.entity.SysMenu;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends BaseJpaRepository<SysMenu> {

    /**
     * 根据父ID查询子菜单
     */
    List<SysMenu> findByParentIdOrderBySortAsc(Long parentId);

    /**
     * 查询所有菜单（按父ID和排序）
     */
    List<SysMenu> findAllByDeletedOrderByParentIdAscSortAsc(Integer deleted);

    /**
     * 检查是否有子菜单
     */
    @Query("SELECT COUNT(m) > 0 FROM SysMenu m WHERE m.parentId = :parentId AND m.deleted = 0")
    boolean hasChildren(@Param("parentId") Long parentId);

    /**
     * 检查菜单名称是否重复（同父级下）
     */
    @Query("SELECT COUNT(m) > 0 FROM SysMenu m WHERE m.menuName = :menuName AND m.parentId = :parentId AND m.deleted = 0")
    boolean existsByMenuNameAndParentId(@Param("menuName") String menuName, @Param("parentId") Long parentId);

    /**
     * 检查菜单名称是否重复（同父级下，排除指定ID）
     */
    @Query("SELECT COUNT(m) > 0 FROM SysMenu m WHERE m.menuName = :menuName AND m.parentId = :parentId AND m.id != :id AND m.deleted = 0")
    boolean existsByMenuNameAndParentIdExcludeId(@Param("menuName") String menuName, @Param("parentId") Long parentId, @Param("id") Long excludeId);

    /**
     * 根据用户ID查询菜单ID列表（通过角色关联）
     */
    @Query("""
        SELECT DISTINCT rm.menuId
        FROM SysRoleMenu rm
        WHERE rm.roleId IN (
            SELECT ur.roleId FROM SysUserRole ur WHERE ur.userId = :userId AND ur.deleted = 0
        )
        AND rm.deleted = 0
        """)
    List<Long> findMenuIdsByUserId(@Param("userId") Long userId);

    /**
     * 查询所有启用的菜单（用于超级管理员）
     */
    List<SysMenu> findByStatusAndDeletedOrderByParentIdAscSortAsc(Integer status, Integer deleted);

    /**
     * 查询所有启用的菜单（便捷方法）
     */
    default List<SysMenu> findAllEnabled() {
        return findByStatusAndDeletedOrderByParentIdAscSortAsc(1, 0);
    }

    /**
     * 根据ID列表查询菜单（便捷方法）
     */
    default List<SysMenu> findByIds(List<Long> ids) {
        return findAllById(ids);
    }

    /**
     * 获取用户角色标识
     */
    @Query(value = """
        SELECT r.role_key
        FROM sys_role r
        INNER JOIN sys_user_role ur ON r.id = ur.role_id
        WHERE ur.user_id = :userId AND r.status = 1 AND r.deleted = 0
        """, nativeQuery = true)
    List<String> findRoleKeysByUserId(@Param("userId") Long userId);

    /**
     * 获取用户权限标识
     */
    @Query(value = """
        SELECT DISTINCT m.perms
        FROM sys_menu m
        INNER JOIN sys_role_menu rm ON m.id = rm.menu_id
        INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id
        WHERE ur.user_id = :userId AND m.perms IS NOT NULL AND m.perms != ''
          AND m.status = 1 AND m.deleted = 0
        """, nativeQuery = true)
    List<String> findPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 获取所有启用的权限标识（超级管理员使用）
     */
    @Query(value = """
        SELECT DISTINCT perms
        FROM sys_menu
        WHERE perms IS NOT NULL AND perms != '' AND status = 1 AND deleted = 0
        """, nativeQuery = true)
    List<String> findAllEnabledPermissions();
}
