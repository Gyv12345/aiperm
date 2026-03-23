package com.devlovecode.aiperm.modules.enterprise.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.enterprise.entity.SysJob;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

/**
 * 定时任务 Repository
 */
public interface JobRepository extends BaseJpaRepository<SysJob> {

    /**
     * 更新任务状态
     */
    @Modifying
    @Query("UPDATE SysJob j SET j.status = :status, j.updateTime = :updateTime, j.updateBy = :updateBy " +
           "WHERE j.id = :id AND j.deleted = 0")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status,
                     @Param("updateBy") String updateBy, @Param("updateTime") LocalDateTime updateTime);
}
