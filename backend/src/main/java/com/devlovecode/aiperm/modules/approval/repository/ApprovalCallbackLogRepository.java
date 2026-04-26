package com.devlovecode.aiperm.modules.approval.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.approval.entity.SysApprovalCallbackLog;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApprovalCallbackLogRepository extends BaseJpaRepository<SysApprovalCallbackLog> {

	Optional<SysApprovalCallbackLog> findFirstByPlatformOrderByProcessedTimeDescCreateTimeDesc(String platform);

}
