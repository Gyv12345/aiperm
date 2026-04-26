package com.devlovecode.aiperm.modules.approval.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.approval.entity.SysApprovalInstance;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApprovalInstanceRepository extends BaseJpaRepository<SysApprovalInstance> {

	Optional<SysApprovalInstance> findByIdAndDeleted(Long id, Integer deleted);

	Optional<SysApprovalInstance> findByPlatformInstanceId(String platformInstanceId);

	Optional<SysApprovalInstance> findFirstByBusinessTypeAndBusinessIdOrderByCreateTimeDesc(String businessType,
			Long businessId);

	Optional<SysApprovalInstance> findFirstBySceneCodeAndBusinessTypeAndBusinessIdOrderByCreateTimeDesc(String sceneCode,
			String businessType, Long businessId);

	Optional<SysApprovalInstance> findByActiveInstanceKey(String activeInstanceKey);

}
