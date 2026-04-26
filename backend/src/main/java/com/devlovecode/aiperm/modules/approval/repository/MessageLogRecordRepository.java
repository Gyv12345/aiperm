package com.devlovecode.aiperm.modules.approval.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.approval.entity.SysMessageLogRecord;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageLogRecordRepository extends BaseJpaRepository<SysMessageLogRecord> {

	Optional<SysMessageLogRecord> findFirstByPlatformOrderBySendTimeDescCreateTimeDesc(String platform);

}
