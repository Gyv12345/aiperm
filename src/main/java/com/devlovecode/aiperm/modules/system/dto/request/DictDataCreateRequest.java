package com.devlovecode.aiperm.modules.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建字典数据请求")
public class DictDataCreateRequest {

    @NotBlank(message = "字典类型不能为空")
    @Schema(description = "字典类型")
    private String dictType;

    @NotBlank(message = "字典标签不能为空")
    @Schema(description = "字典标签（显示值）")
    private String dictLabel;

    @NotBlank(message = "字典键值不能为空")
    @Schema(description = "字典键值（存储值）")
    private String dictValue;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
