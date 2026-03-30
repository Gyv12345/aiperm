package com.devlovecode.aiperm.modules.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户响应VO
 *
 * @author DevLoveCode
 */
@Data
@Schema(description = "用户信息")
public class UserVO {

	@Schema(description = "用户ID", example = "1")
	private Long id;

	@Schema(description = "用户名", example = "zhangsan")
	private String username;

	@Schema(description = "昵称", example = "张三")
	private String nickname;

	@Schema(description = "邮箱", example = "zhangsan@example.com")
	private String email;

	@Schema(description = "手机号", example = "13800138000")
	private String phone;

	@Schema(description = "性别（0=未知，1=男，2=女）", example = "1")
	private Integer gender;

	@Schema(description = "头像", example = "https://example.com/avatar.jpg")
	private String avatar;

	@Schema(description = "部门ID", example = "1")
	private Long deptId;

	@Schema(description = "部门名称", example = "技术部")
	private String deptName;

	@Schema(description = "岗位ID列表", example = "[1, 2]")
	private List<Long> postIds;

	@Schema(description = "岗位名称", example = "经理, 开发")
	private String postNames;

	@Schema(description = "角色ID列表", example = "[1, 2]")
	private List<Long> roleIds;

	@Schema(description = "角色名称", example = "管理员, 普通用户")
	private String roleNames;

	@Schema(description = "角色列表")
	private List<RoleVO> roles;

	@Schema(description = "状态（0=禁用，1=启用）", example = "1")
	private Integer status;

	@Schema(description = "备注", example = "这是备注信息")
	private String remark;

	@Schema(description = "创建时间", example = "2024-01-01 12:00:00")
	private LocalDateTime createTime;

	@Schema(description = "更新时间", example = "2024-01-01 12:00:00")
	private LocalDateTime updateTime;

}
