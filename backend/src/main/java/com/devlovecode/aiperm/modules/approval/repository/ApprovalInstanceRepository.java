package com.devlovecode.aiperm.modules.approval.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.approval.entity.SysApprovalInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ApprovalInstanceRepository extends BaseJpaRepository<SysApprovalInstance> {

    Optional<SysApprovalInstance> findByPlatformInstanceId(String platformInstanceId);

    boolean existsByBusinessTypeAndBusinessIdAndStatus(String businessType, Long businessId, String status);

    Page<SysApprovalInstance> findAll(Specification<SysApprovalInstance> spec, Pageable pageable);

    @Modifying
    @Query("UPDATE SysApprovalInstance i SET i.status = :status, i.resultTime = :resultTime, i.updateTime = :updateTime, i.updateBy = :updateBy WHERE i.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("resultTime") LocalDateTime resultTime,
                     @Param("updateTime") LocalDateTime updateTime, @Param("updateBy") String updateBy);

    Optional<SysApprovalInstance> findFirstByPlatformAndStatusInOrderByResultTimeDescUpdateTimeDescIdDesc(String platform, java.util.List<String> statuses);
}
