package com.devlovecode.aiperm.modules.approval.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.approval.entity.SysApprovalScene;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApprovalSceneRepository extends BaseJpaRepository<SysApprovalScene> {

    Optional<SysApprovalScene> findBySceneCode(String sceneCode);

    boolean existsBySceneCode(String sceneCode);

    boolean existsBySceneCodeAndIdNot(String sceneCode, Long id);

    int countByPlatformAndEnabled(String platform, Integer enabled);

    Optional<SysApprovalScene> findFirstByPlatformAndEnabledOrderByUpdateTimeDescIdDesc(String platform, Integer enabled);
}
