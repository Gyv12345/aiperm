package com.devlovecode.aiperm.modules.storage.repository;

import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.modules.storage.entity.SysFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 文件记录 Repository
 *
 * <p>sys_file 表不继承 BaseEntity，故直接继承 JpaRepository + JpaSpecificationExecutor，
 * 手写 softDelete（与 OperLogRepository 同模式）。
 *
 * @author DevLoveCode
 */
@Repository
public interface SysFileRepository extends JpaRepository<SysFile, Long>, JpaSpecificationExecutor<SysFile> {

	/**
	 * 软删除（sys_file 表无 update_time，仅置 deleted=1）。
	 */
	@Modifying
	@Query("UPDATE SysFile f SET f.deleted = 1 WHERE f.id = :id")
	int softDelete(@Param("id") Long id);

	/**
	 * 分页查询：按原始文件名模糊、文件类型精确。
	 */
	default Page<SysFile> queryPage(String originalName, String fileType, String storageType, int pageNum,
			int pageSize) {
		return findAll(
				SpecificationUtils.and(SpecificationUtils.like("originalName", originalName),
						SpecificationUtils.like("fileType", fileType),
						SpecificationUtils.eq("storageType", storageType)),
				PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime")));
	}

}
