package com.devlovecode.aiperm.modules.oauth.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.oauth.entity.SysUserOauth;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户第三方账号绑定 Repository
 */
@Repository
public interface UserOauthRepository extends BaseJpaRepository<SysUserOauth> {

	/** 根据平台和openId查找绑定关系 */
	Optional<SysUserOauth> findByPlatformAndOpenId(String platform, String openId);

	/** 查询用户已绑定的所有第三方账号 */
	List<SysUserOauth> findByUserIdAndStatus(Long userId, Integer status);

	/** 查询用户对某平台的绑定 */
	Optional<SysUserOauth> findByUserIdAndPlatform(Long userId, String platform);

	@Modifying
	@Query("UPDATE SysUserOauth o SET o.lastLoginTime = :lastLoginTime WHERE o.id = :id")
	int updateLastLoginTime(@Param("id") Long id, @Param("lastLoginTime") java.time.LocalDateTime lastLoginTime);

	@Modifying
	@Query("UPDATE SysUserOauth o SET o.status = 0, o.updateTime = :updateTime WHERE o.userId = :userId AND o.platform = :platform")
	int unbind(@Param("userId") Long userId, @Param("platform") String platform,
			@Param("updateTime") java.time.LocalDateTime updateTime);

}
