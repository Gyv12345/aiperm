package com.devlovecode.aiperm.modules.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户信息VO（包含角色和权限）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息")
public class UserInfoVO {

	@Schema(description = "用户ID")
	private Long id;

	@Schema(description = "用户名")
	private String username;

	@Schema(description = "昵称")
	private String nickname;

	@Schema(description = "头像")
	private String avatar;

	@Schema(description = "角色列表")
	private List<String> roles;

	@Schema(description = "权限列表")
	private List<String> permissions;

}
