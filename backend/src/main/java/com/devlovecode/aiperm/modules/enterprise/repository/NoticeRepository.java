package com.devlovecode.aiperm.modules.enterprise.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.enterprise.entity.SysNotice;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告通知 Repository
 */
public interface NoticeRepository extends BaseJpaRepository<SysNotice> {

	/**
	 * 发布公告
	 */
	@Modifying
	@Query("UPDATE SysNotice n SET n.status = 1, n.publishTime = :publishTime, n.updateTime = :updateTime, n.updateBy = :updateBy WHERE n.id = :id AND n.deleted = 0")
	int publish(@Param("id") Long id, @Param("updateBy") String updateBy,
			@Param("publishTime") LocalDateTime publishTime, @Param("updateTime") LocalDateTime updateTime);

	/**
	 * 撤回公告
	 */
	@Modifying
	@Query("UPDATE SysNotice n SET n.status = 0, n.updateTime = :updateTime, n.updateBy = :updateBy WHERE n.id = :id AND n.deleted = 0")
	int withdraw(@Param("id") Long id, @Param("updateBy") String updateBy,
			@Param("updateTime") LocalDateTime updateTime);

	/**
	 * 查询已发布的公告列表
	 */
	@Query("SELECT n FROM SysNotice n WHERE n.status = 1 AND n.deleted = 0 " + "AND (:type IS NULL OR n.type = :type) "
			+ "ORDER BY n.publishTime DESC")
	List<SysNotice> findPublished(@Param("type") Integer type);

}
