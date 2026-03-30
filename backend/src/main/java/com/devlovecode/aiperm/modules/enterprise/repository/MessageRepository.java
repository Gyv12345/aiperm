package com.devlovecode.aiperm.modules.enterprise.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.enterprise.entity.SysMessage;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息中心 Repository
 */
public interface MessageRepository extends BaseJpaRepository<SysMessage> {

	/**
	 * 标记消息为已读
	 */
	@Modifying
	@Query("UPDATE SysMessage m SET m.isRead = 1, m.readTime = :readTime, m.updateTime = :updateTime WHERE m.id = :id AND m.deleted = 0")
	int markAsRead(@Param("id") Long id, @Param("readTime") LocalDateTime readTime,
			@Param("updateTime") LocalDateTime updateTime);

	/**
	 * 批量标记用户所有未读消息为已读
	 */
	@Modifying
	@Query("UPDATE SysMessage m SET m.isRead = 1, m.readTime = :readTime, m.updateTime = :updateTime "
			+ "WHERE m.receiverId = :receiverId AND m.isRead = 0 AND m.deleted = 0")
	int markAllAsRead(@Param("receiverId") Long receiverId, @Param("readTime") LocalDateTime readTime,
			@Param("updateTime") LocalDateTime updateTime);

	/**
	 * 批量标记指定消息为已读
	 */
	@Modifying
	@Query("UPDATE SysMessage m SET m.isRead = 1, m.readTime = :readTime, m.updateTime = :updateTime "
			+ "WHERE m.id IN :ids AND m.deleted = 0")
	int markAsReadByIds(@Param("ids") List<Long> ids, @Param("readTime") LocalDateTime readTime,
			@Param("updateTime") LocalDateTime updateTime);

	/**
	 * 统计用户未读消息数量
	 */
	@Query("SELECT COUNT(m) FROM SysMessage m WHERE m.receiverId = :receiverId AND m.isRead = 0 AND m.deleted = 0")
	long countUnread(@Param("receiverId") Long receiverId);

	/**
	 * 查询用户的所有消息
	 */
	@Query("SELECT m FROM SysMessage m WHERE m.receiverId = :receiverId AND m.deleted = 0 ORDER BY m.createTime DESC")
	List<SysMessage> findByReceiverId(@Param("receiverId") Long receiverId);

}
