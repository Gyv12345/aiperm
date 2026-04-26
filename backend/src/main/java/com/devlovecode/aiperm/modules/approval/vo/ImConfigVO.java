package com.devlovecode.aiperm.modules.approval.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "IM平台配置响应")
public class ImConfigVO {

	private Long id;

	private String platform;

	private Integer enabled;

	private String appId;

	private String appSecret;

	private String corpId;

	private String callbackToken;

	private String callbackAesKey;

	private String extraConfig;

	private String remark;

	private Boolean configReady;

	private List<String> missingFields;

}
