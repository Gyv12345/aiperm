package com.devlovecode.aiperm.modules.approval.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.approval.entity.SysImConfig;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImConfigRepository extends BaseJpaRepository<SysImConfig> {

	Optional<SysImConfig> findByPlatform(String platform);

	Optional<SysImConfig> findByIdAndDeleted(Long id, Integer deleted);

	List<SysImConfig> findAllByOrderByIdAsc();

}
