package com.devlovecode.aiperm.modules.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "在线用户")
public class OnlineUserVO {

	@Schema(description = "记录ID")
	private Long id;

	@Schema(description = "用户ID")
	private Long userId;

	@Schema(description = "用户名")
	private String username;

	@Schema(description = "昵称")
	private String nickname;

	@Schema(description = "部门名称")
	private String deptName;

	@Schema(description = "角色名称")
	private String roleNames;

	@Schema(description = "Token")
	private String token;

	@Schema(description = "登录IP")
	private String ip;

	@Schema(description = "浏览器")
	private String browser;

	@Schema(description = "操作系统")
	private String os;

	@Schema(description = "登录时间")
	private LocalDateTime loginTime;

	@Schema(description = "最后活跃时间")
	private LocalDateTime lastAccessTime;

	@Schema(description = "Token 剩余秒数")
	private Long tokenTimeout;

	@Schema(description = "是否当前会话")
	private Boolean currentSession;

}
