package com.devlovecode.aiperm.common.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * 所有实体类的父类，包含通用字段
 *
 * @author devlovecode
 */
@Data
@Schema(description = "基础实体类")
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "逻辑删除标记（0=未删除，1=已删除）")
    private Integer deleted;

    @Schema(description = "乐观锁版本号")
    private Integer version;
}
