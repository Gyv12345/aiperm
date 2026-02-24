package com.devlovecode.aiperm.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("sys_post")
@Schema(description = "岗位实体")
public class SysPost extends BaseEntity {

    @Schema(description = "岗位名称")
    @TableField("post_name")
    private String postName;

    @Schema(description = "岗位编码")
    @TableField("post_code")
    private String postCode;

    @Schema(description = "显示顺序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "岗位状态（0=正常，1=停用）")
    @TableField("status")
    private Integer status;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
