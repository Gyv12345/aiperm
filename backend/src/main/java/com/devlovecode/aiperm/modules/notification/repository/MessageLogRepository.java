package com.devlovecode.aiperm.modules.notification.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.notification.entity.SysMessageLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MessageLogRepository extends BaseJpaRepository<SysMessageLog> {

    @Modifying
    @Query("UPDATE SysMessageLog m SET m.status = 'SUCCESS', m.sendTime = :sendTime, m.errorMsg = NULL, m.updateTime = :updateTime WHERE m.id = :id")
    int markSuccess(@Param("id") Long id, @Param("sendTime") LocalDateTime sendTime, @Param("updateTime") LocalDateTime updateTime);

    @Modifying
    @Query("UPDATE SysMessageLog m SET m.status = 'FAILED', m.errorMsg = :errorMsg, m.updateTime = :updateTime WHERE m.id = :id")
    int markFailed(@Param("id") Long id, @Param("errorMsg") String errorMsg, @Param("updateTime") LocalDateTime updateTime);

    Page<SysMessageLog> findAll(Specification<SysMessageLog> spec, Pageable pageable);

    Optional<SysMessageLog> findFirstByPlatformOrderBySendTimeDescUpdateTimeDescIdDesc(String platform);
}
