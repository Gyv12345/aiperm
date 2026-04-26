package com.devlovecode.aiperm.modules.approval.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class FeishuApprovalAdapter extends BaseSimulationApprovalAdapter {

	public FeishuApprovalAdapter(ObjectMapper objectMapper) {
		super(objectMapper);
	}

	@Override
	public String platform() {
		return "FEISHU";
	}

}
