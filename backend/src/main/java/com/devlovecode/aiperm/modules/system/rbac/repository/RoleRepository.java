package com.devlovecode.aiperm.modules.system.rbac.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.modules.system.rbac.entity.SysRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
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
	@Query("SELECT COUNT(r) > 0 FROM SysRole r WHERE r.roleCode = :roleCode AND r.id != :id")
	boolean existsByRoleCodeExcludeId(@Param("roleCode") String roleCode, @Param("id") Long excludeId);

	/**
	 * 获取角色的菜单ID列表
	 */
	@Query("SELECT rm.menuId FROM SysRoleMenu rm WHERE rm.roleId = :roleId")
	List<Long> getMenuIdsByRoleId(@Param("roleId") Long roleId);

	/**
	 * 检查角色是否被用户使用
	 */
	@Query("SELECT COUNT(ur) > 0 FROM SysUserRole ur WHERE ur.roleId = :roleId")
	boolean isUsedByUser(@Param("roleId") Long roleId);

	/**
	 * 删除角色的所有菜单关联（物理删除有效记录，避免软删除唯一键冲突）
	 */
	@Modifying
	@Query(value = "DELETE FROM sys_role_menu WHERE role_id = :roleId AND deleted = 0", nativeQuery = true)
	void deleteRoleMenus(@Param("roleId") Long roleId);

	/**
	 * 添加角色菜单关联
	 */
	@Modifying
	@Query(value = """
			INSERT INTO sys_role_menu (role_id, menu_id, deleted, create_time)
			VALUES (:roleId, :menuId, 0, :createTime)
			ON DUPLICATE KEY UPDATE deleted = 0, create_time = :createTime
			""", nativeQuery = true)
	void insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId,
			@Param("createTime") LocalDateTime createTime);

	/**
	 * 获取角色的自定义部门ID列表
	 */
	@Query(value = "SELECT dept_id FROM sys_role_dept WHERE role_id = :roleId", nativeQuery = true)
	List<Long> getDeptIdsByRoleId(@Param("roleId") Long roleId);

	/**
	 * 删除角色的自定义部门关联（物理删除，与角色菜单同策略）
	 */
	@Modifying
	@Query(value = "DELETE FROM sys_role_dept WHERE role_id = :roleId", nativeQuery = true)
	void deleteRoleDepts(@Param("roleId") Long roleId);

	/**
	 * 添加角色自定义部门关联
	 */
	@Modifying
	@Query(value = """
			INSERT INTO sys_role_dept (role_id, dept_id, deleted, create_time, create_by)
			VALUES (:roleId, :deptId, 0, :createTime, :createBy)
			ON DUPLICATE KEY UPDATE deleted = 0, create_time = :createTime
			""", nativeQuery = true)
	void insertRoleDept(@Param("roleId") Long roleId, @Param("deptId") Long deptId,
			@Param("createTime") LocalDateTime createTime, @Param("createBy") String createBy);

	/**
	 * 检查是否为内置角色（超级管理员）
	 */
	@Query("SELECT COUNT(r) > 0 FROM SysRole r WHERE r.id = :id AND r.roleCode = 'admin'")
	boolean isBuiltin(@Param("id") Long id);

	SysRole findByRoleCodeAndDeleted(String roleCode, Integer deleted);

	/**
	 * 分页查询
	 */
	default Page<SysRole> queryPage(String roleName, String roleCode, Integer status, int pageNum, int pageSize) {
		return findAll(
				SpecificationUtils.and(SpecificationUtils.like("roleName", roleName),
						SpecificationUtils.like("roleCode", roleCode), SpecificationUtils.eq("status", status)),
				PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime")));
	}

}
