package com.devlovecode.aiperm.modules.system.entity;

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
@Schema(description = "角色实体")
public class SysRole extends BaseEntity {

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "显示顺序")
    private Integer sort;

    @Schema(description = "角色状态（0=正常，1=停用）")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "是否为内置角色（0=否，1=是）")
    private Integer isBuiltin;
}
