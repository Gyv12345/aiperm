package com.devlovecode.aiperm.modules.agent.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.agent.entity.SysLlmProvider;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * LLM 提供商仓储
 */
@Repository
public interface LlmProviderRepository extends BaseJpaRepository<SysLlmProvider> {

    /**
     * 查询所有启用的提供商
     */
    List<SysLlmProvider> findByStatusAndDeletedOrderBySortAsc(Integer status, Integer deleted);

    /**
     * 查找默认提供商
     */
    Optional<SysLlmProvider> findByIsDefaultAndStatusAndDeleted(Boolean isDefault, Integer status, Integer deleted);

    /**
     * 根据名称查询
     */
    Optional<SysLlmProvider> findByNameAndDeleted(String name, Integer deleted);

    /**
     * 清除所有默认标记
     */
    @Modifying
    @Query("UPDATE SysLlmProvider p SET p.isDefault = false, p.updateTime = :updateTime WHERE p.deleted = :deleted")
    int clearDefault(@Param("updateTime") LocalDateTime updateTime, @Param("deleted") Integer deleted);

    /**
     * 设置默认提供商
     */
    @Modifying
    @Query("UPDATE SysLlmProvider p SET p.isDefault = true, p.updateTime = :updateTime WHERE p.id = :id AND p.deleted = :deleted")
    int setDefault(@Param("id") Long id, @Param("updateTime") LocalDateTime updateTime, @Param("deleted") Integer deleted);
}
