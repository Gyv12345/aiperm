package com.devlovecode.aiperm.modules.approval.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class WeworkApprovalAdapter extends BaseSimulationApprovalAdapter {

	public WeworkApprovalAdapter(ObjectMapper objectMapper) {
		super(objectMapper);
	}

	@Override
	public String platform() {
		return "WEWORK";
	}

}
