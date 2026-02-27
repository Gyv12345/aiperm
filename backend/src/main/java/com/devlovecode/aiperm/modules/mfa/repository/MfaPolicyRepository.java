package com.devlovecode.aiperm.modules.mfa.repository;

import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.modules.mfa.entity.SysMfaPolicy;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 2FA策略配置 Repository
 */
@Repository
public class MfaPolicyRepository extends BaseRepository<SysMfaPolicy> {

    public MfaPolicyRepository(JdbcClient db) {
        super(db, "sys_mfa_policy", SysMfaPolicy.class);
    }

    /** 查询所有启用的策略 */
    public List<SysMfaPolicy> findAllEnabled() {
        String sql = "SELECT * FROM sys_mfa_policy WHERE enabled = 1 AND deleted = 0";
        return db.sql(sql).query(SysMfaPolicy.class).list();
    }

    public void insert(SysMfaPolicy policy) {
        String sql = """
            INSERT INTO sys_mfa_policy (name, perm_pattern, api_pattern, enabled, deleted, version, create_time, create_by)
            VALUES (:name, :permPattern, :apiPattern, :enabled, 0, 0, NOW(), :createBy)
            """;
        db.sql(sql)
                .param("name", policy.getName())
                .param("permPattern", policy.getPermPattern())
                .param("apiPattern", policy.getApiPattern())
                .param("enabled", policy.getEnabled() != null ? policy.getEnabled() : 1)
                .param("createBy", policy.getCreateBy())
                .update();
    }

    public int update(SysMfaPolicy policy) {
        String sql = """
            UPDATE sys_mfa_policy
            SET name = :name, perm_pattern = :permPattern, api_pattern = :apiPattern,
                enabled = :enabled, update_time = NOW(), update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("name", policy.getName())
                .param("permPattern", policy.getPermPattern())
                .param("apiPattern", policy.getApiPattern())
                .param("enabled", policy.getEnabled())
                .param("updateBy", policy.getUpdateBy())
                .param("id", policy.getId())
                .update();
    }
}
