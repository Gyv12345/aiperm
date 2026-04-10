package com.devlovecode.aiperm.modules.audit.repository;

import com.devlovecode.aiperm.modules.audit.entity.SysLoginLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginLogRepository extends JpaRepository<SysLoginLog, Long>, JpaSpecificationExecutor<SysLoginLog> {

	@Query("""
			SELECT l
			FROM SysLoginLog l
			WHERE COALESCE(l.deleted, 0) = 0
			  AND (l.userId = :userId OR (l.userId IS NULL AND l.username = :username))
			ORDER BY l.loginTime DESC
			""")
	Page<SysLoginLog> queryPageByUser(@Param("userId") Long userId, @Param("username") String username,
			org.springframework.data.domain.Pageable pageable);

	default Page<SysLoginLog> queryPageByUser(Long userId, String username, int pageNum, int pageSize) {
		return queryPageByUser(userId, username,
				PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "loginTime")));
	}

	@Modifying
	@Query("UPDATE SysLoginLog l SET l.deleted = 1 WHERE l.id = :id AND COALESCE(l.deleted, 0) = 0")
	int softDelete(@Param("id") Long id);

	@Modifying
	@Query("UPDATE SysLoginLog l SET l.deleted = 1 WHERE COALESCE(l.deleted, 0) = 0")
	int softDeleteAll();

}
