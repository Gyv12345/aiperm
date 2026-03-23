package com.devlovecode.aiperm.modules.agent.repository;

import com.devlovecode.aiperm.modules.agent.entity.SysAgentMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Agent 消息仓储
 */
@Repository
public interface AgentMessageRepository extends JpaRepository<SysAgentMessage, Long> {

    /**
     * 根据会话ID查询消息（按创建时间升序）
     */
    List<SysAgentMessage> findBySessionIdOrderByCreateTimeAsc(String sessionId);

    /**
     * 更新消息确认状态
     */
    @Modifying
    @Query("UPDATE SysAgentMessage m SET m.confirmed = :confirmed WHERE m.id = :id")
    int updateConfirmed(@Param("id") Long id, @Param("confirmed") Boolean confirmed);

    /**
     * 根据会话ID删除消息
     */
    @Modifying
    @Query("DELETE FROM SysAgentMessage m WHERE m.sessionId = :sessionId")
    int deleteBySessionId(@Param("sessionId") String sessionId);
}
