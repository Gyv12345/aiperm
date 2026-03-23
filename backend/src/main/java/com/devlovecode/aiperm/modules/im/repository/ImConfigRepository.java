package com.devlovecode.aiperm.modules.im.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.im.entity.SysImConfig;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImConfigRepository extends BaseJpaRepository<SysImConfig> {

    Optional<SysImConfig> findByPlatform(String platform);
}
