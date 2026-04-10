package com.devlovecode.aiperm.modules.system.rbac.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "用户数据")
public class UserDTO {

	// ========== 分页查询参数 ==========

	@JsonView(Views.Query.class)
	@Schema(description = "页码", example = "1")
	private Integer page = 1;

	@JsonView(Views.Query.class)
	@Schema(description = "每页条数", example = "10")
	private Integer pageSize = 10;

	// ========== 业务字段 ==========

	@JsonView({ Views.Create.class, Views.Update.class, Views.Query.class })
	@Schema(description = "用户名")
	@NotBlank(message = "用户名不能为空", groups = Views.Create.class)
	@Size(max = 50, message = "用户名不能超过50个字符")
	private String username;

	@JsonView(Views.Create.class)
	@Schema(description = "密码")
	@NotBlank(message = "密码不能为空", groups = Views.Create.class)
	@Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
	private String password;

	@JsonView({ Views.Create.class, Views.Update.class, Views.Query.class })
	@Schema(description = "昵称")
	@Size(max = 50, message = "昵称不能超过50个字符")
	private String nickname;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "真实姓名")
	@Size(max = 50, message = "真实姓名不能超过50个字符")
	private String realName;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "用户邮箱")
	@Size(max = 100, message = "邮箱不能超过100个字符")
	private String email;

	@JsonView({ Views.Create.class, Views.Update.class, Views.Query.class })
	@Schema(description = "手机号码")
	@Size(max = 20, message = "手机号码不能超过20个字符")
	private String phone;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "用户性别（0=未知，1=男，2=女）")
	private Integer gender;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "头像地址")
	@Size(max = 500, message = "头像地址不能超过500个字符")
	private String avatar;

	@JsonView({ Views.Create.class, Views.Update.class, Views.Query.class })
	@Schema(description = "部门ID")
	private Long deptId;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "岗位ID")
	private Long postId;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "角色ID列表")
	private List<Long> roleIds;

	@JsonView({ Views.Create.class, Views.Update.class, Views.Query.class })
	@Schema(description = "用户状态（0=正常，1=停用）")
	private Integer status;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "备注")
	@Size(max = 500, message = "备注不能超过500个字符")
	private String remark;

	// ========== 特殊操作参数 ==========

	@JsonView(Views.Update.class)
	@Schema(description = "新密码（重置密码用）")
	private String newPassword;

}
