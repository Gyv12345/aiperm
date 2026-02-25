package com.devlovecode.aiperm.modules.system.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位实体类
 *
 * @author devlovecode
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "岗位实体")
public class SysPost extends BaseEntity {

    @Schema(description = "岗位名称")
    private String postName;

    @Schema(description = "岗位编码")
    private String postCode;

    @Schema(description = "显示顺序")
    private Integer sort;

    @Schema(description = "岗位状态（0=正常，1=停用）")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
