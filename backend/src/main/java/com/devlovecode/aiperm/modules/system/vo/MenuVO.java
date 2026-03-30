package com.devlovecode.aiperm.modules.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单响应VO
 *
 * @author DevLoveCode
 */
@Data
@Schema(description = "菜单信息")
public class MenuVO {

	@Schema(description = "菜单ID", example = "1")
	private Long id;

	@Schema(description = "父菜单ID", example = "0")
	private Long parentId;

	@Schema(description = "菜单名称", example = "系统管理")
	private String menuName;

	@Schema(description = "菜单类型（M=目录，C=菜单，F=按钮）", example = "M")
	private String menuType;

	@Schema(description = "路由地址", example = "/system")
	private String path;

	@Schema(description = "组件路径", example = "/system/index")
	private String component;

	@Schema(description = "权限标识", example = "system:user:list")
	private String permission;

	@Schema(description = "菜单图标", example = "system")
	private String icon;

	@Schema(description = "排序", example = "1")
	private Integer sort;

	@Schema(description = "是否可见（0=隐藏，1=显示）", example = "1")
	private Integer visible;

	@Schema(description = "是否缓存（0=不缓存，1=缓存）", example = "1")
	private Integer isCache;

	@Schema(description = "是否外链（0=否，1=是）", example = "0")
	private Integer isFrame;

	@Schema(description = "状态（0=禁用，1=启用）", example = "1")
	private Integer status;

	@Schema(description = "子菜单列表")
	private List<MenuVO> children;

	@Schema(description = "创建时间", example = "2024-01-01 12:00:00")
	private LocalDateTime createTime;

	@Schema(description = "更新时间", example = "2024-01-01 12:00:00")
	private LocalDateTime updateTime;

}
