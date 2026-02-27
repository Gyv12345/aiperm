package com.devlovecode.aiperm.modules.captcha.repository;

import com.devlovecode.aiperm.modules.captcha.entity.SysCaptchaLog;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CaptchaLogRepository {

    private final JdbcClient db;

    public void insert(SysCaptchaLog log) {
        String sql = """
            INSERT INTO sys_captcha_log (type, target, code, scene, status, fail_reason, ip, create_time)
            VALUES (:type, :target, :code, :scene, :status, :failReason, :ip, NOW())
            """;
        db.sql(sql)
                .param("type", log.getType())
                .param("target", log.getTarget())
                .param("code", log.getCode())
                .param("scene", log.getScene())
                .param("status", log.getStatus())
                .param("failReason", log.getFailReason())
                .param("ip", log.getIp())
                .update();
    }

    /** 查询今日发送次数 */
    public int countTodayByTarget(String target) {
        String sql = """
            SELECT COUNT(*) FROM sys_captcha_log
            WHERE target = :target AND status = 1
              AND create_time >= :startOfDay
            """;
        Integer count = db.sql(sql)
                .param("target", target)
                .param("startOfDay", LocalDate.now().atStartOfDay())
                .query(Integer.class).single();
        return count != null ? count : 0;
    }

    /** 查询最近一次发送时间（限流用） */
    public Optional<LocalDateTime> findLastSendTime(String target) {
        String sql = """
            SELECT MAX(create_time) FROM sys_captcha_log
            WHERE target = :target AND status = 1
            """;
        return db.sql(sql).param("target", target)
                .query(LocalDateTime.class).optional();
    }
}
