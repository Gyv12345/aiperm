package com.devlovecode.aiperm.modules.agent.repository;

import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.modules.agent.entity.SysLlmProvider;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class LlmProviderRepository extends BaseRepository<SysLlmProvider> {

    public LlmProviderRepository(JdbcClient db) {
        super(db, "sys_llm_provider", SysLlmProvider.class);
    }

    public List<SysLlmProvider> findAllEnabled() {
        String sql = "SELECT * FROM sys_llm_provider WHERE status = 0 AND deleted = 0 ORDER BY sort ASC";
        return db.sql(sql).query(SysLlmProvider.class).list();
    }

    public Optional<SysLlmProvider> findDefault() {
        String sql = "SELECT * FROM sys_llm_provider WHERE is_default = 1 AND status = 0 AND deleted = 0 LIMIT 1";
        return db.sql(sql).query(SysLlmProvider.class).optional();
    }

    public Optional<SysLlmProvider> findByName(String name) {
        String sql = "SELECT * FROM sys_llm_provider WHERE name = :name AND deleted = 0";
        return db.sql(sql).param("name", name).query(SysLlmProvider.class).optional();
    }

    public void clearDefault() {
        String sql = "UPDATE sys_llm_provider SET is_default = 0, update_time = :updateTime WHERE deleted = 0";
        db.sql(sql).param("updateTime", LocalDateTime.now()).update();
    }

    public void setDefault(Long id) {
        String sql = "UPDATE sys_llm_provider SET is_default = 1, update_time = :updateTime WHERE id = :id AND deleted = 0";
        db.sql(sql)
            .param("id", id)
            .param("updateTime", LocalDateTime.now())
            .update();
    }

    public void insert(SysLlmProvider entity) {
        String sql = """
            INSERT INTO sys_llm_provider (name, display_name, protocol, api_key, base_url, model, is_default, status, sort, remark, deleted, version, create_time, create_by, update_time, update_by)
            VALUES (:name, :displayName, :protocol, :apiKey, :baseUrl, :model, :isDefault, :status, :sort, :remark, 0, 0, :createTime, :createBy, :updateTime, :updateBy)
            """;
        db.sql(sql)
            .param("name", entity.getName())
            .param("displayName", entity.getDisplayName())
            .param("protocol", entity.getProtocol())
            .param("apiKey", entity.getApiKey())
            .param("baseUrl", entity.getBaseUrl())
            .param("model", entity.getModel())
            .param("isDefault", entity.getIsDefault() != null && entity.getIsDefault() ? 1 : 0)
            .param("status", entity.getStatus() != null ? entity.getStatus() : 0)
            .param("sort", entity.getSort() != null ? entity.getSort() : 0)
            .param("remark", entity.getRemark())
            .param("createTime", LocalDateTime.now())
            .param("createBy", entity.getCreateBy())
            .param("updateTime", LocalDateTime.now())
            .param("updateBy", entity.getUpdateBy())
            .update();
    }

    public int update(SysLlmProvider entity) {
        String sql = """
            UPDATE sys_llm_provider
            SET display_name = :displayName, protocol = :protocol, api_key = :apiKey, base_url = :baseUrl, model = :model,
                is_default = :isDefault, status = :status, sort = :sort, remark = :remark,
                update_time = :updateTime, update_by = :updateBy, version = version + 1
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
            .param("id", entity.getId())
            .param("displayName", entity.getDisplayName())
            .param("protocol", entity.getProtocol())
            .param("apiKey", entity.getApiKey())
            .param("baseUrl", entity.getBaseUrl())
            .param("model", entity.getModel())
            .param("isDefault", entity.getIsDefault() != null && entity.getIsDefault() ? 1 : 0)
            .param("status", entity.getStatus())
            .param("sort", entity.getSort())
            .param("remark", entity.getRemark())
            .param("updateTime", LocalDateTime.now())
            .param("updateBy", entity.getUpdateBy())
            .update();
    }
}
