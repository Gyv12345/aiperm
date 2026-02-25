package com.devlovecode.aiperm.modules.system.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "字典数据")
public class SysDictData extends BaseEntity {

    @Schema(description = "字典类型")
    private String dictType;

    @Schema(description = "字典标签（显示值）")
    private String dictLabel;

    @Schema(description = "字典键值（存储值）")
    private String dictValue;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
