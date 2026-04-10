package com.devlovecode.aiperm.modules.system.rbac.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色响应VO
 *
 * @author DevLoveCode
 */
@Data
@Schema(description = "角色信息")
public class RoleVO {

	@Schema(description = "角色ID", example = "1")
	private Long id;

	@Schema(description = "角色名称", example = "管理员")
	private String roleName;

	@Schema(description = "角色编码", example = "admin")
	private String roleCode;

	@Schema(description = "排序", example = "1")
	private Integer sort;

	@Schema(description = "状态（0=禁用，1=启用）", example = "1")
	private Integer status;

	@Schema(description = "备注", example = "这是备注信息")
	private String remark;

	@Schema(description = "数据权限范围：1-全部，2-本部门，3-本部门及下级，4-仅本人", example = "1")
	private Integer dataScope;

	@Schema(description = "菜单列表")
	private List<MenuVO> menus;

	@Schema(description = "创建时间", example = "2024-01-01 12:00:00")
	private LocalDateTime createTime;

	@Schema(description = "更新时间", example = "2024-01-01 12:00:00")
	private LocalDateTime updateTime;

}
