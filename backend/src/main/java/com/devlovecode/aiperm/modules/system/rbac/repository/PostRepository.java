package com.devlovecode.aiperm.modules.system.rbac.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.modules.system.rbac.entity.SysPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

	SysPost findByPostCodeAndDeleted(String postCode, Integer deleted);

	/**
	 * 分页查询
	 */
	default Page<SysPost> queryPage(String postName, String postCode, Integer status, int pageNum, int pageSize) {
		return findAll(
				SpecificationUtils.and(SpecificationUtils.like("postName", postName),
						SpecificationUtils.like("postCode", postCode), SpecificationUtils.eq("status", status)),
				PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime")));
	}

}
