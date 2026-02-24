package com.devlovecode.aiperm.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devlovecode.aiperm.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 菜单实体类
 *
 * @author devlovecode
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
@Schema(description = "菜单实体")
public class SysMenu extends BaseEntity {

    @Schema(description = "菜单名称")
    @TableField("menu_name")
    private String menuName;

    @Schema(description = "父菜单ID（0为根菜单）")
    @TableField("parent_id")
    private Long parentId;

    @Schema(description = "菜单类型（M=目录，C=菜单，F=按钮）")
    @TableField("menu_type")
    private String menuType;

    @Schema(description = "显示顺序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "路由地址")
    @TableField("path")
    private String path;

    @Schema(description = "组件路径")
    @TableField("component")
    private String component;

    @Schema(description = "菜单图标")
    @TableField("icon")
    private String icon;

    @Schema(description = "是否为外链（0=否，1=是）")
    @TableField("is_external")
    private Integer isExternal;

    @Schema(description = "是否缓存（0=不缓存，1=缓存）")
    @TableField("is_cache")
    private Integer isCache;

    @Schema(description = "是否显示（0=隐藏，1=显示）")
    @TableField("visible")
    private Integer visible;

    @Schema(description = "菜单状态（0=正常，1=停用）")
    @TableField("status")
    private Integer status;

    @Schema(description = "权限标识")
    @TableField("permission")
    private String permission;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "子菜单列表")
    @TableField(exist = false)
    private List<SysMenu> children;
}
