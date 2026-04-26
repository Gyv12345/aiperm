package com.devlovecode.aiperm.modules.approval.api;

/**
 * 业务模块审批结果处理器。
 */
public interface ApprovalHandler {

	void onApproved(ApprovalTaskContext context);

	void onRejected(ApprovalTaskContext context);

	default void onCanceled(ApprovalTaskContext context) {
		// 默认不处理取消事件
	}

}
