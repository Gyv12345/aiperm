package com.devlovecode.aiperm.modules.enterprise.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.enterprise.entity.SysConfig;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 系统配置 Repository
 */
public interface ConfigRepository extends BaseJpaRepository<SysConfig> {

    /**
     * 根据配置键查询
     */
    @Query("SELECT c FROM SysConfig c WHERE c.configKey = :configKey AND c.deleted = 0")
    Optional<SysConfig> findByConfigKey(@Param("configKey") String configKey);

    /**
     * 检查配置键是否存在
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM SysConfig c " +
           "WHERE c.configKey = :configKey AND c.deleted = 0")
    boolean existsByConfigKey(@Param("configKey") String configKey);

    /**
     * 检查配置键是否存在（排除指定ID）
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM SysConfig c " +
           "WHERE c.configKey = :configKey AND c.id != :excludeId AND c.deleted = 0")
    boolean existsByConfigKeyExcludeId(@Param("configKey") String configKey, @Param("excludeId") Long excludeId);
}
