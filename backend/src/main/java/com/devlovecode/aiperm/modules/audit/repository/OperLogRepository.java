package com.devlovecode.aiperm.modules.audit.repository;

import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.modules.audit.entity.SysOperLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OperLogRepository extends JpaRepository<SysOperLog, Long>, JpaSpecificationExecutor<SysOperLog> {

	default Page<SysOperLog> queryPage(String title, Integer status, int pageNum, int pageSize) {
		return findAll(
				SpecificationUtils.and(SpecificationUtils.like("title", title),
						SpecificationUtils.eq("status", status)),
				PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime")));
	}

}
