package com.devlovecode.aiperm.modules.approval.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class DingtalkApprovalAdapter extends BaseSimulationApprovalAdapter {

	public DingtalkApprovalAdapter(ObjectMapper objectMapper) {
		super(objectMapper);
	}

	@Override
	public String platform() {
		return "DINGTALK";
	}

}
