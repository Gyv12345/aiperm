package com.devlovecode.aiperm.modules.enterprise.repository;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.common.repository.SqlBuilder;
import com.devlovecode.aiperm.modules.enterprise.entity.SysConfig;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class ConfigRepository extends BaseRepository<SysConfig> {

    public ConfigRepository(JdbcClient db) {
        super(db, "sys_config", SysConfig.class);
    }

    /**
     * 插入系统配置
     */
    public void insert(SysConfig entity) {
        String sql = """
            INSERT INTO sys_config (config_key, config_value, config_type, remark, deleted, version, create_time, create_by)
            VALUES (:configKey, :configValue, :configType, :remark, 0, 0, :createTime, :createBy)
            """;
        db.sql(sql)
                .param("configKey", entity.getConfigKey())
                .param("configValue", entity.getConfigValue())
                .param("configType", entity.getConfigType())
                .param("remark", entity.getRemark())
                .param("createTime", LocalDateTime.now())
                .param("createBy", entity.getCreateBy())
                .update();
    }

    /**
     * 更新系统配置
     */
    public int update(SysConfig entity) {
        String sql = """
            UPDATE sys_config
            SET config_key = :configKey, config_value = :configValue, config_type = :configType,
                remark = :remark, update_time = :updateTime, update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("configKey", entity.getConfigKey())
                .param("configValue", entity.getConfigValue())
                .param("configType", entity.getConfigType())
                .param("remark", entity.getRemark())
                .param("updateTime", LocalDateTime.now())
                .param("updateBy", entity.getUpdateBy())
                .param("id", entity.getId())
                .update();
    }

    /**
     * 根据配置键查询
     */
    public Optional<SysConfig> findByConfigKey(String configKey) {
        String sql = "SELECT * FROM sys_config WHERE config_key = :configKey AND deleted = 0";
        return db.sql(sql).param("configKey", configKey).query(SysConfig.class).optional();
    }

    /**
     * 检查配置键是否存在
     */
    public boolean existsByConfigKey(String configKey) {
        String sql = "SELECT COUNT(*) FROM sys_config WHERE config_key = :configKey AND deleted = 0";
        Integer count = db.sql(sql).param("configKey", configKey).query(Integer.class).single();
        return count != null && count > 0;
    }

    /**
     * 检查配置键是否存在（排除指定ID）
     */
    public boolean existsByConfigKeyExcludeId(String configKey, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM sys_config WHERE config_key = :configKey AND id != :id AND deleted = 0";
        Integer count = db.sql(sql)
                .param("configKey", configKey)
                .param("id", excludeId)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    /**
     * 分页查询
     */
    public PageResult<SysConfig> queryPage(String configKey, String configType, int pageNum, int pageSize) {
        SqlBuilder sb = new SqlBuilder();
        sb.likeIf(configKey != null && !configKey.isBlank(), "config_key", configKey)
          .likeIf(configType != null && !configType.isBlank(), "config_type", configType);

        return queryPage(sb.getWhereClause(), sb.getParams(), pageNum, pageSize);
    }
}
