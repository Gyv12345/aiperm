package com.devlovecode.aiperm.modules.oauth.repository;

import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.modules.oauth.entity.SysOauthConfig;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * OAuth 平台配置 Repository
 */
@Repository
public class OauthConfigRepository extends BaseRepository<SysOauthConfig> {

    public OauthConfigRepository(JdbcClient db) {
        super(db, "sys_oauth_config", SysOauthConfig.class);
    }

    public Optional<SysOauthConfig> findByPlatform(String platform) {
        String sql = "SELECT * FROM sys_oauth_config WHERE platform = :platform AND deleted = 0";
        return db.sql(sql).param("platform", platform).query(SysOauthConfig.class).optional();
    }

    public int update(SysOauthConfig config) {
        String sql = """
            UPDATE sys_oauth_config
            SET enabled = :enabled, corp_id = :corpId, agent_id = :agentId,
                app_key = :appKey, app_secret = :appSecret, callback_url = :callbackUrl,
                remark = :remark, update_time = NOW(), update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("enabled", config.getEnabled())
                .param("corpId", config.getCorpId())
                .param("agentId", config.getAgentId())
                .param("appKey", config.getAppKey())
                .param("appSecret", config.getAppSecret())
                .param("callbackUrl", config.getCallbackUrl())
                .param("remark", config.getRemark())
                .param("updateBy", config.getUpdateBy())
                .param("id", config.getId())
                .update();
    }
}
