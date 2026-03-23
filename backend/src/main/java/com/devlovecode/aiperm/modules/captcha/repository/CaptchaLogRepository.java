package com.devlovecode.aiperm.modules.captcha.repository;

import com.devlovecode.aiperm.modules.captcha.entity.SysCaptchaLog;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CaptchaLogRepository extends org.springframework.data.jpa.repository.JpaRepository<SysCaptchaLog, Long> {

    /** 查询今日发送次数 */
    @Query("SELECT COUNT(l) FROM SysCaptchaLog l WHERE l.target = :target AND l.status = 1 AND l.createTime >= :startOfDay")
    int countTodayByTarget(@Param("target") String target, @Param("startOfDay") LocalDateTime startOfDay);

    /** 查询最近一次发送时间（限流用） */
    @Query("SELECT MAX(l.createTime) FROM SysCaptchaLog l WHERE l.target = :target AND l.status = 1")
    Optional<LocalDateTime> findLastSendTime(@Param("target") String target);
}
