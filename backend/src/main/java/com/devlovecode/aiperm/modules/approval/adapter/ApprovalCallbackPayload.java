package com.devlovecode.aiperm.modules.approval.adapter;

/**
 * 平台回调统一抽象。
 */
public record ApprovalCallbackPayload(String platformInstanceId, String status, String sceneCode, String errorMessage,
		String rawPayload) {
}
