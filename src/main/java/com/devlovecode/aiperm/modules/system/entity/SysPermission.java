package com.devlovecode.aiperm.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devlovecode.aiperm.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限实体类
 *
 * @author devlovecode
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
@Schema(description = "权限实体")
public class SysPermission extends BaseEntity {

    @Schema(description = "权限名称")
    @TableField("permission_name")
    private String permissionName;

    @Schema(description = "权限编码")
    @TableField("permission_code")
    private String permissionCode;

    @Schema(description = "权限类型（1=菜单，2=按钮）")
    @TableField("permission_type")
    private Integer permissionType;

    @Schema(description = "关联菜单ID")
    @TableField("menu_id")
    private Long menuId;

    @Schema(description = "显示顺序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "权限状态（0=正常，1=停用）")
    @TableField("status")
    private Integer status;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
