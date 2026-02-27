package com.devlovecode.aiperm.modules.mfa.repository;

import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.modules.mfa.entity.SysUserMfa;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户2FA绑定记录 Repository
 */
@Repository
public class UserMfaRepository extends BaseRepository<SysUserMfa> {

    public UserMfaRepository(JdbcClient db) {
        super(db, "sys_user_mfa", SysUserMfa.class);
    }

    public Optional<SysUserMfa> findByUserId(Long userId) {
        String sql = "SELECT * FROM sys_user_mfa WHERE user_id = :userId AND deleted = 0";
        return db.sql(sql).param("userId", userId).query(SysUserMfa.class).optional();
    }

    public void insert(SysUserMfa mfa) {
        String sql = """
            INSERT INTO sys_user_mfa (user_id, mfa_type, secret_key, bind_time, status, deleted, version, create_time, create_by)
            VALUES (:userId, :mfaType, :secretKey, :bindTime, :status, 0, 0, NOW(), :createBy)
            """;
        db.sql(sql)
                .param("userId", mfa.getUserId())
                .param("mfaType", mfa.getMfaType() != null ? mfa.getMfaType() : "TOTP")
                .param("secretKey", mfa.getSecretKey())
                .param("bindTime", mfa.getBindTime())
                .param("status", mfa.getStatus() != null ? mfa.getStatus() : 1)
                .param("createBy", mfa.getCreateBy())
                .update();
    }

    public int updateStatus(Long userId, Integer status) {
        String sql = "UPDATE sys_user_mfa SET status = :status, update_time = NOW() WHERE user_id = :userId AND deleted = 0";
        return db.sql(sql).param("status", status).param("userId", userId).update();
    }

    public int deleteByUserId(Long userId) {
        String sql = "UPDATE sys_user_mfa SET deleted = 1, update_time = NOW() WHERE user_id = :userId";
        return db.sql(sql).param("userId", userId).update();
    }
}
