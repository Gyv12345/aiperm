package com.devlovecode.aiperm.modules.auth.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单VO（用于返回给前端）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "菜单信息")
public class MenuVO {

    @Schema(description = "菜单ID")
    private Long id;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "父菜单ID")
    private Long parentId;

    @Schema(description = "菜单类型：1-目录，2-菜单，3-按钮")
    private String menuType;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "权限标识")
    private String perms;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否可见")
    private Integer visible;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "子菜单")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MenuVO> children = new ArrayList<>();
}
