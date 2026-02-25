package com.devlovecode.aiperm.modules.system.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门实体类
 *
 * @author devlovecode
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "部门实体")
public class SysDept extends BaseEntity {

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "父部门ID（0为根部门）")
    private Long parentId;

    @Schema(description = "显示顺序")
    private Integer sort;

    @Schema(description = "负责人")
    private String leader;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "部门状态（0=正常，1=停用）")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "子部门列表")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<SysDept> children = new ArrayList<>();
}
