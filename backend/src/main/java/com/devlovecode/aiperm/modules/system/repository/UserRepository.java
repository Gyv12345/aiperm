package com.devlovecode.aiperm.modules.system.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.modules.system.entity.SysUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends BaseJpaRepository<SysUser> {

	Optional<SysUser> findByUsername(String username);

	Optional<SysUser> findByPhone(String phone);

	Optional<SysUser> findByEmail(String email);

	boolean existsByUsername(String username);

	@Query("SELECT COUNT(u) > 0 FROM SysUser u WHERE u.username = :username AND u.id != :id AND u.deleted = 0")
	boolean existsByUsernameExcludeId(@Param("username") String username, @Param("id") Long excludeId);

	@Modifying
	@Transactional
	@Query("UPDATE SysUser u SET u.password = :password, u.updateTime = :updateTime WHERE u.id = :id AND u.deleted = 0")
	int updatePassword(@Param("id") Long id, @Param("password") String newPassword,
			@Param("updateTime") LocalDateTime updateTime);

	@Modifying
	@Transactional
	@Query("UPDATE SysUser u SET u.status = :status, u.updateTime = :updateTime WHERE u.id = :id AND u.deleted = 0")
	int updateStatus(@Param("id") Long id, @Param("status") Integer status,
			@Param("updateTime") LocalDateTime updateTime);

	@Modifying
	@Transactional
	@Query("UPDATE SysUser u SET u.lastLoginIp = :loginIp, u.lastLoginTime = :loginTime WHERE u.id = :id AND u.deleted = 0")
	int updateLoginInfo(@Param("id") Long id, @Param("loginIp") String loginIp,
			@Param("loginTime") LocalDateTime loginTime);

	@Query("""
			SELECT COUNT(u) > 0
			FROM SysUser u
			WHERE u.id = :id
			  AND u.deleted = 0
			  AND u.isAdmin = 1
			""")
	boolean isAdmin(@Param("id") Long id);

	@Query("SELECT ur.roleId FROM SysUserRole ur WHERE ur.userId = :userId AND ur.deleted = 0")
	List<Long> findRoleIdsByUserId(@Param("userId") Long userId);

	@Query("""
			SELECT u FROM SysUser u
			WHERE u.status = 1
			  AND u.deleted = 0
			  AND u.id <> :excludeUserId
			ORDER BY u.realName ASC, u.nickname ASC, u.username ASC
			""")
	List<SysUser> findEnabledReceivers(@Param("excludeUserId") Long excludeUserId);

	/**
	 * 分页查询
	 */
	default Page<SysUser> queryPage(String username, String phone, Long deptId, Integer status, int pageNum,
			int pageSize) {
		return findAll(
				SpecificationUtils.and(SpecificationUtils.like("username", username),
						SpecificationUtils.like("phone", phone), SpecificationUtils.eq("deptId", deptId),
						SpecificationUtils.eq("status", status)),
				PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime")));
	}

	/**
	 * 更新用户的角色关联
	 */
	@Modifying
	@Transactional
	@Query(value = """
			UPDATE sys_user_role SET deleted = 1 WHERE user_id = :userId
			""", nativeQuery = true)
	void softDeleteUserRoles(@Param("userId") Long userId);

	@Modifying
	@Transactional
	@Query(value = """
			INSERT INTO sys_user_role (user_id, role_id, deleted, create_time, create_by)
			VALUES (:userId, :roleId, 0, :createTime, :createBy)
			""", nativeQuery = true)
	void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId,
			@Param("createTime") LocalDateTime createTime, @Param("createBy") String createBy);

	default void updateUserRoles(Long userId, List<Long> roleIds, String createBy) {
		softDeleteUserRoles(userId);
		if (roleIds == null || roleIds.isEmpty()) {
			return;
		}
		LocalDateTime now = LocalDateTime.now();
		for (Long roleId : roleIds) {
			insertUserRole(userId, roleId, now, createBy);
		}
	}

}
