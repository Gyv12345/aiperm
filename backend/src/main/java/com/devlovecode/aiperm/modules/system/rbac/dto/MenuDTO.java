package com.devlovecode.aiperm.modules.system.rbac.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "菜单数据")
public class MenuDTO {

	@JsonView({ Views.Create.class, Views.Update.class, Views.Query.class })
	@Schema(description = "菜单名称")
	@NotBlank(message = "菜单名称不能为空", groups = { Views.Create.class, Views.Update.class })
	@Size(max = 100, message = "菜单名称不能超过100个字符")
	private String menuName;

	@JsonView({ Views.Create.class, Views.Update.class, Views.Query.class })
	@Schema(description = "父菜单ID（0为根菜单）")
	private Long parentId = 0L;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "菜单类型（M=目录，C=菜单，F=按钮）")
	@NotBlank(message = "菜单类型不能为空", groups = { Views.Create.class, Views.Update.class })
	private String menuType;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "显示顺序")
	private Integer sort;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "路由地址")
	@Size(max = 200, message = "路由地址不能超过200个字符")
	private String path;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "组件路径")
	@Size(max = 200, message = "组件路径不能超过200个字符")
	private String component;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "权限标识（按钮用，如 system:user:add）")
	@Size(max = 100, message = "权限标识不能超过100个字符")
	private String perms;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "菜单图标")
	@Size(max = 100, message = "菜单图标不能超过100个字符")
	private String icon;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "是否为外链（0=否，1=是）")
	private Integer isExternal;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "是否缓存（0=不缓存，1=缓存）")
	private Integer isCache;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "是否显示（0=隐藏，1=显示）")
	private Integer visible;

	@JsonView({ Views.Create.class, Views.Update.class, Views.Query.class })
	@Schema(description = "菜单状态（0=正常，1=停用）")
	private Integer status;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "权限标识")
	@Size(max = 100, message = "权限标识不能超过100个字符")
	private String permission;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "备注")
	@Size(max = 500, message = "备注不能超过500个字符")
	private String remark;

}
