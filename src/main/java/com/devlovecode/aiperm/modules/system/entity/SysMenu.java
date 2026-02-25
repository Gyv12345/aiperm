package com.devlovecode.aiperm.modules.system.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单实体类
 *
 * @author devlovecode
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "菜单实体")
public class SysMenu extends BaseEntity {

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "父菜单ID（0为根菜单）")
    private Long parentId;

    @Schema(description = "菜单类型（M=目录，C=菜单，F=按钮）")
    private String menuType;

    @Schema(description = "显示顺序")
    private Integer sort;

    @Schema(description = "路由地址")
    private String path;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "权限标识（按钮用，如 system:user:add）")
    private String perms;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "是否为外链（0=否，1=是）")
    private Integer isExternal;

    @Schema(description = "是否缓存（0=不缓存，1=缓存）")
    private Integer isCache;

    @Schema(description = "是否显示（0=隐藏，1=显示）")
    private Integer visible;

    @Schema(description = "菜单状态（0=正常，1=停用）")
    private Integer status;

    @Schema(description = "权限标识")
    private String permission;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "子菜单列表")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<SysMenu> children = new ArrayList<>();
}
