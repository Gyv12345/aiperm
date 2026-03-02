package com.devlovecode.aiperm.modules.im.repository;

import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.modules.im.entity.SysImConfig;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ImConfigRepository extends BaseRepository<SysImConfig> {

    public ImConfigRepository(JdbcClient db) {
        super(db, "sys_im_config", SysImConfig.class);
    }

    public Optional<SysImConfig> findByPlatform(String platform) {
        String sql = "SELECT * FROM sys_im_config WHERE platform = :platform AND deleted = 0";
        return db.sql(sql).param("platform", platform).query(SysImConfig.class).optional();
    }

    public int update(SysImConfig entity) {
        String sql = """
            UPDATE sys_im_config
            SET enabled = :enabled, app_id = :appId, app_secret = :appSecret,
                corp_id = :corpId, callback_token = :callbackToken, callback_aes_key = :callbackAesKey,
                extra_config = :extraConfig, update_time = NOW(), update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("enabled", entity.getEnabled())
                .param("appId", entity.getAppId())
                .param("appSecret", entity.getAppSecret())
                .param("corpId", entity.getCorpId())
                .param("callbackToken", entity.getCallbackToken())
                .param("callbackAesKey", entity.getCallbackAesKey())
                .param("extraConfig", entity.getExtraConfig())
                .param("updateBy", entity.getUpdateBy())
                .param("id", entity.getId())
                .update();
    }
}
