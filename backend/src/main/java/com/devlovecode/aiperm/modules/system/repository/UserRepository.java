package com.devlovecode.aiperm.modules.system.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.system.entity.SysUser;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends BaseJpaRepository<SysUser> {

    /**
     * 根据用户名查询
     */
    Optional<SysUser> findByUsername(String username);

    /**
     * 根据手机号查询
     */
    Optional<SysUser> findByPhone(String phone);

    /**
     * 根据邮箱查询
     */
    Optional<SysUser> findByEmail(String email);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查用户名是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(u) > 0 FROM SysUser u WHERE u.username = :username AND u.id != :id AND u.deleted = 0")
    boolean existsByUsernameExcludeId(@Param("username") String username, @Param("id") Long excludeId);

    /**
     * 更新密码
     */
    @Modifying
    @Query("UPDATE SysUser u SET u.password = :password, u.updateTime = :updateTime WHERE u.id = :id AND u.deleted = 0")
    int updatePassword(@Param("id") Long id, @Param("password") String newPassword, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 更新状态
     */
    @Modifying
    @Query("UPDATE SysUser u SET u.status = :status, u.updateTime = :updateTime WHERE u.id = :id AND u.deleted = 0")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 更新最后登录信息
     */
    @Modifying
    @Query("UPDATE SysUser u SET u.lastLoginIp = :loginIp, u.lastLoginTime = :loginTime WHERE u.id = :id AND u.deleted = 0")
    int updateLoginInfo(@Param("id") Long id, @Param("loginIp") String loginIp, @Param("loginTime") LocalDateTime loginTime);

    /**
     * 查询用户的角色ID列表（通过用户角色关联表）
     */
    @Query("SELECT ur.roleId FROM SysUserRole ur WHERE ur.userId = :userId AND ur.deleted = 0")
    List<Long> findRoleIdsByUserId(@Param("userId") Long userId);
}
