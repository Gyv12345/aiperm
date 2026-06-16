package com.devlovecode.aiperm.modules.approval.api;

import java.util.Optional;

/**
 * 业务模块接入审批桥接模块的统一入口。
 */
public interface ApprovalClient {

	/**
	 * 提交审批。
	 *
	 * @return 审批实例ID；若场景未启用或未开启自动提交，则返回 null
	 */
	Long submit(ApprovalSubmitCommand command);

	default Long submitOptional(String sceneCode, String businessType, Long businessId,
			java.util.Map<String, Object> payload) {
		return submit(ApprovalSubmitCommand.optional(sceneCode, businessType, businessId, payload));
	}

	default Long submitRequired(String sceneCode, String businessType, Long businessId,
			java.util.Map<String, Object> payload) {
		return submit(ApprovalSubmitCommand.required(sceneCode, businessType, businessId, payload));
	}

	/**
	 * 查询业务最新审批实例。
	 */
	Optional<ApprovalTaskContext> queryLatest(String businessType, Long businessId);

	/**
	 * 取消进行中的审批实例。
	 */
	void cancel(String sceneCode, String businessType, Long businessId, String reason);

}
