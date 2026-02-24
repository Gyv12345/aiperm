package com.devlovecode.aiperm.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devlovecode.aiperm.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体类
 *
 * @author devlovecode
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
@Schema(description = "角色实体")
public class SysRole extends BaseEntity {

    @Schema(description = "角色名称")
    @TableField("role_name")
    private String roleName;

    @Schema(description = "角色编码")
    @TableField("role_code")
    private String roleCode;

    @Schema(description = "显示顺序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "角色状态（0=正常，1=停用）")
    @TableField("status")
    private Integer status;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "是否为内置角色（0=否，1=是）")
    @TableField("is_builtin")
    private Integer isBuiltin;
}
