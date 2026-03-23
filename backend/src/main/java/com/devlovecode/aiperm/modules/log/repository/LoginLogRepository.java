package com.devlovecode.aiperm.modules.log.repository;

import com.devlovecode.aiperm.modules.log.entity.SysLoginLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginLogRepository extends JpaRepository<SysLoginLog, Long> {

    @Query("SELECT l FROM SysLoginLog l WHERE l.userId = :userId ORDER BY l.loginTime DESC")
    Page<SysLoginLog> queryPageByUserId(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);

    default Page<SysLoginLog> queryPageByUserId(Long userId, int pageNum, int pageSize) {
        return queryPageByUserId(userId, PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "loginTime")));
    }
}
