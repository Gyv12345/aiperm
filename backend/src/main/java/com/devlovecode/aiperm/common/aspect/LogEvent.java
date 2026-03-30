package com.devlovecode.aiperm.common.aspect;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LogEvent {

	private String title;

	private int operType;

	private String method;

	private String requestMethod;

	private String operUrl;

	private String operIp;

	private String operParam;

	private String jsonResult;

	private int status;

	private String errorMsg;

	private long costTime;

}
