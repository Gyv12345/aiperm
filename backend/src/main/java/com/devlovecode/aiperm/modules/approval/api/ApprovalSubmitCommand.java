package com.devlovecode.aiperm.modules.approval.api;

import java.util.Map;

/**
 * 提交审批命令。
 */
public record ApprovalSubmitCommand(String sceneCode, String businessType, Long businessId,
		Map<String, Object> payload) {
}
