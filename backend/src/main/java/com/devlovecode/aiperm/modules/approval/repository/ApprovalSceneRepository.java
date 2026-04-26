package com.devlovecode.aiperm.modules.approval.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.approval.entity.SysApprovalScene;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalSceneRepository extends BaseJpaRepository<SysApprovalScene> {

	Optional<SysApprovalScene> findBySceneCode(String sceneCode);

	Optional<SysApprovalScene> findByIdAndDeleted(Long id, Integer deleted);

	boolean existsBySceneCode(String sceneCode);

	@Query("SELECT COUNT(s) > 0 FROM SysApprovalScene s WHERE s.sceneCode = :sceneCode AND s.id <> :id AND s.deleted = 0")
	boolean existsBySceneCodeExcludeId(@Param("sceneCode") String sceneCode, @Param("id") Long id);

	long countByPlatformAndEnabledAndDeleted(String platform, Integer enabled, Integer deleted);

	Optional<SysApprovalScene> findFirstByPlatformAndEnabledAndDeletedOrderByIdAsc(String platform, Integer enabled,
			Integer deleted);

	List<SysApprovalScene> findByPlatformAndEnabledAndDeletedOrderBySceneNameAsc(String platform, Integer enabled,
			Integer deleted);

}
