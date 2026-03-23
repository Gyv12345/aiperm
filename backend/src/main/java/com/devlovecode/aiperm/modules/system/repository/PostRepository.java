package com.devlovecode.aiperm.modules.system.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.system.entity.SysPost;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends BaseJpaRepository<SysPost> {

    /**
     * 检查岗位编码是否存在
     */
    boolean existsByPostCode(String postCode);

    /**
     * 检查岗位编码是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(p) > 0 FROM SysPost p WHERE p.postCode = :postCode AND p.id != :id AND p.deleted = 0")
    boolean existsByPostCodeExcludeId(@Param("postCode") String postCode, @Param("id") Long excludeId);
}
