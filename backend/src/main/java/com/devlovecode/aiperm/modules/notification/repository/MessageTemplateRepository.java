package com.devlovecode.aiperm.modules.notification.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.notification.entity.SysMessageTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageTemplateRepository extends BaseJpaRepository<SysMessageTemplate> {

    Optional<SysMessageTemplate> findByTemplateCode(String templateCode);

    boolean existsByTemplateCode(String templateCode);

    boolean existsByTemplateCodeAndIdNot(String templateCode, Long id);
}
