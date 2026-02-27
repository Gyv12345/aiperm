package com.devlovecode.aiperm.modules.captcha.repository;

import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.modules.captcha.entity.SysCaptchaConfig;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CaptchaConfigRepository extends BaseRepository<SysCaptchaConfig> {

    public CaptchaConfigRepository(JdbcClient db) {
        super(db, "sys_captcha_config", SysCaptchaConfig.class);
    }

    public Optional<SysCaptchaConfig> findByType(String type) {
        String sql = "SELECT * FROM sys_captcha_config WHERE type = :type AND deleted = 0";
        return db.sql(sql).param("type", type).query(SysCaptchaConfig.class).optional();
    }

    public int update(SysCaptchaConfig config) {
        String sql = """
            UPDATE sys_captcha_config
            SET enabled = :enabled,
                sms_provider = :smsProvider, sms_access_key = :smsAccessKey,
                sms_secret_key = :smsSecretKey, sms_sign_name = :smsSignName,
                sms_template_code = :smsTemplateCode,
                email_host = :emailHost, email_port = :emailPort,
                email_username = :emailUsername, email_password = :emailPassword,
                email_from = :emailFrom, email_from_name = :emailFromName,
                code_length = :codeLength, expire_minutes = :expireMinutes,
                daily_limit = :dailyLimit,
                update_time = NOW(), update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("enabled", config.getEnabled())
                .param("smsProvider", config.getSmsProvider())
                .param("smsAccessKey", config.getSmsAccessKey())
                .param("smsSecretKey", config.getSmsSecretKey())
                .param("smsSignName", config.getSmsSignName())
                .param("smsTemplateCode", config.getSmsTemplateCode())
                .param("emailHost", config.getEmailHost())
                .param("emailPort", config.getEmailPort())
                .param("emailUsername", config.getEmailUsername())
                .param("emailPassword", config.getEmailPassword())
                .param("emailFrom", config.getEmailFrom())
                .param("emailFromName", config.getEmailFromName())
                .param("codeLength", config.getCodeLength())
                .param("expireMinutes", config.getExpireMinutes())
                .param("dailyLimit", config.getDailyLimit())
                .param("updateBy", config.getUpdateBy())
                .param("id", config.getId())
                .update();
    }
}
