package com.devlovecode.aiperm.modules.auth.social.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.auth.social.entity.SysSocialBinding;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SocialBindingRepository extends BaseJpaRepository<SysSocialBinding> {

	/** 按第三方来源+openid 查找绑定（用于登录时确定系统用户） */
	Optional<SysSocialBinding> findBySourceAndOpenid(String source, String openid);

	/** 查某用户的全部绑定 */
	List<SysSocialBinding> findByUserId(Long userId);

	/** 查某用户在某平台的绑定 */
	Optional<SysSocialBinding> findByUserIdAndSource(Long userId, String source);

	/** 某第三方账号是否已被绑定 */
	boolean existsBySourceAndOpenid(String source, String openid);

	/** 软删除某用户在某平台的绑定 */
	@Modifying
	@Query("UPDATE SysSocialBinding e SET e.deleted = 1, e.updateTime = :now WHERE e.userId = :userId AND e.source = :source")
	int softDeleteByUserIdAndSource(@Param("userId") Long userId, @Param("source") String source, @Param("now") LocalDateTime now);

}
