package com.devlovecode.aiperm.modules.approval.api;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 业务处理器使用的审批上下文。
 */
public record ApprovalTaskContext(Long id, String sceneCode, String sceneName, String businessType, Long businessId,
		Long initiatorId, String initiatorName, String platform, String platformInstanceId, String status,
		Map<String, Object> payload, LocalDateTime createTime, LocalDateTime resultTime) {
}
