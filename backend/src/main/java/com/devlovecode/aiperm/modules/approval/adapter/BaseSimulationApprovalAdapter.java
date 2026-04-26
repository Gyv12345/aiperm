package com.devlovecode.aiperm.modules.approval.adapter;

import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.approval.entity.SysApprovalScene;
import com.devlovecode.aiperm.modules.approval.entity.SysImConfig;
import com.devlovecode.aiperm.modules.auth.oauth.entity.SysUserOauth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class BaseSimulationApprovalAdapter implements ImApprovalAdapter {

	private final ObjectMapper objectMapper;

	@Override
	public String createApproval(SysImConfig config, SysApprovalScene scene, SysUserOauth initiatorBinding,
			Map<String, Object> payload) {
		JsonNode extraConfig = readExtraConfig(config.getExtraConfig());
		if (!extraConfig.path("simulationMode").asBoolean(false)) {
			throw new BusinessException(
					platform() + " 平台审批适配器尚未接入真实API，请在 IM 配置 extraConfig 中开启 simulationMode，或补充适配器实现");
		}
		return platform() + "-" + UUID.randomUUID();
	}

	@Override
	public ApprovalCallbackPayload parseCallback(SysImConfig config, String body, Map<String, String> headers) {
		try {
			JsonNode root = objectMapper.readTree(body);
			String instanceId = text(root, "instanceId", "platformInstanceId");
			if (isBlank(instanceId)) {
				throw new BusinessException("回调缺少 instanceId/platformInstanceId");
			}

			String status = normalizeStatus(text(root, "status", "approvalStatus"));
			String sceneCode = text(root, "sceneCode");
			String errorMessage = text(root, "errorMessage", "message");
			return new ApprovalCallbackPayload(instanceId, status, sceneCode, errorMessage, body);
		}
		catch (BusinessException e) {
			throw e;
		}
		catch (Exception e) {
			throw new BusinessException(platform() + " 平台回调解析失败");
		}
	}

	@Override
	public String resolveTodoUrl(SysImConfig config) {
		JsonNode extraConfig = readExtraConfig(config.getExtraConfig());
		return text(extraConfig, "todoUrl", "approvalTodoUrl");
	}

	private JsonNode readExtraConfig(String extraConfig) {
		try {
			if (isBlank(extraConfig)) {
				return objectMapper.createObjectNode();
			}
			return objectMapper.readTree(extraConfig);
		}
		catch (Exception e) {
			throw new BusinessException(platform() + " 平台扩展配置不是合法 JSON");
		}
	}

	private String normalizeStatus(String status) {
		if (isBlank(status)) {
			return "PENDING";
		}
		String normalized = status.trim().toUpperCase();
		return switch (normalized) {
			case "PASS", "PASSED", "APPROVE", "APPROVED", "SUCCESS" -> "APPROVED";
			case "REJECT", "REJECTED", "FAIL", "FAILED" -> "REJECTED";
			case "CANCEL", "CANCELED", "CANCELLED" -> "CANCELED";
			default -> normalized;
		};
	}

	private String text(JsonNode node, String... fieldNames) {
		for (String fieldName : fieldNames) {
			JsonNode field = node.path(fieldName);
			if (!field.isMissingNode() && !field.isNull()) {
				String value = field.asText();
				if (!isBlank(value)) {
					return value;
				}
			}
		}
		return "";
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

}
