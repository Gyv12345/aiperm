package com.devlovecode.aiperm.modules.approval.adapter;

import com.devlovecode.aiperm.modules.approval.entity.SysApprovalScene;
import com.devlovecode.aiperm.modules.approval.entity.SysImConfig;
import com.devlovecode.aiperm.modules.auth.oauth.entity.SysUserOauth;

import java.util.Map;

/**
 * 外部IM审批平台适配器。
 */
public interface ImApprovalAdapter {

	String platform();

	String createApproval(SysImConfig config, SysApprovalScene scene, SysUserOauth initiatorBinding,
			Map<String, Object> payload);

	ApprovalCallbackPayload parseCallback(SysImConfig config, String body, Map<String, String> headers);

	String resolveTodoUrl(SysImConfig config);

}
