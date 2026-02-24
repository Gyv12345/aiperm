package com.devlovecode.aiperm.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devlovecode.aiperm.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门实体类
 *
 * @author devlovecode
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
@Schema(description = "部门实体")
public class SysDept extends BaseEntity {

    @Schema(description = "部门名称")
    @TableField("dept_name")
    private String deptName;

    @Schema(description = "父部门ID（0为根部门）")
    @TableField("parent_id")
    private Long parentId;

    @Schema(description = "显示顺序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "负责人")
    @TableField("leader")
    private String leader;

    @Schema(description = "联系电话")
    @TableField("phone")
    private String phone;

    @Schema(description = "邮箱")
    @TableField("email")
    private String email;

    @Schema(description = "部门状态（0=正常，1=停用）")
    @TableField("status")
    private Integer status;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
