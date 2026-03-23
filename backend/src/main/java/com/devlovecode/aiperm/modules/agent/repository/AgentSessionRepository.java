package com.devlovecode.aiperm.modules.agent.repository;

import com.devlovecode.aiperm.modules.agent.entity.SysAgentSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent 会话仓储
 */
@Repository
public interface AgentSessionRepository extends JpaRepository<SysAgentSession, String> {

    /**
     * 根据用户ID查询活跃会话
     */
    List<SysAgentSession> findByUserIdAndStatusOrderByLastActiveDesc(Long userId, Integer status);

    /**
     * 更新会话最后活跃时间
     */
    @Modifying
    @Query("UPDATE SysAgentSession s SET s.lastActive = :lastActive, s.updateTime = :updateTime WHERE s.id = :id")
    int updateLastActive(@Param("id") String id, @Param("lastActive") LocalDateTime lastActive, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 使会话过期
     */
    @Modifying
    @Query("UPDATE SysAgentSession s SET s.status = 1, s.updateTime = :updateTime WHERE s.id = :id")
    int expireSession(@Param("id") String id, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 根据用户ID删除会话
     */
    @Modifying
    @Query("DELETE FROM SysAgentSession s WHERE s.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
