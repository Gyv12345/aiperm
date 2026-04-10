package com.devlovecode.aiperm.modules.enterprise.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.modules.enterprise.entity.SysJobLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface JobLogRepository extends BaseJpaRepository<SysJobLog> {

	default Page<SysJobLog> queryPage(String jobName, Integer status, String triggerSource, LocalDateTime startTime,
			LocalDateTime endTime, Integer pageNum, Integer pageSize) {
		return findAll(
				SpecificationUtils.and(SpecificationUtils.like("jobName", jobName),
						SpecificationUtils.eq("status", status), SpecificationUtils.eq("triggerSource", triggerSource),
						SpecificationUtils.ge("startTime", startTime), SpecificationUtils.le("startTime", endTime)),
				PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "startTime")));
	}

}
