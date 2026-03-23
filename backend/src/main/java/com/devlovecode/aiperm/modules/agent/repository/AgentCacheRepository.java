package com.devlovecode.aiperm.modules.agent.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.agent.entity.SysAgentCache;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Agent 语义缓存仓储
 */
@Repository
public interface AgentCacheRepository extends BaseJpaRepository<SysAgentCache> {

    /**
     * 根据用户ID查询缓存
     */
    List<SysAgentCache> findByUserIdAndDeleted(Long userId, Integer deleted);

    /**
     * 增加命中次数
     */
    @Modifying
    @Query("UPDATE SysAgentCache c SET c.hitCount = c.hitCount + 1 WHERE c.id = :id")
    int incrementHitCount(@Param("id") Long id);

    /**
     * 删除陈旧缓存（未使用且超过指定天数）
     */
    @Modifying
    @Query("DELETE FROM SysAgentCache c WHERE c.hitCount = 0 AND c.createTime < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL :days DAY)")
    int deleteStale(@Param("days") int days);
}
