package com.devlovecode.aiperm.modules.auth.mfa.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.auth.mfa.entity.SysMfaPolicy;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 2FA策略配置 Repository
 */
@Repository
public interface MfaPolicyRepository extends BaseJpaRepository<SysMfaPolicy> {

	List<SysMfaPolicy> findByEnabled(Integer enabled);

}
