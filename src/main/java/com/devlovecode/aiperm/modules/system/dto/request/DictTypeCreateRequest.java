package com.devlovecode.aiperm.modules.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建字典类型请求")
public class DictTypeCreateRequest {

    @NotBlank(message = "字典名称不能为空")
    @Schema(description = "字典名称")
    private String dictName;

    @NotBlank(message = "字典类型不能为空")
    @Schema(description = "字典类型标识（如 sys_gender）")
    private String dictType;

    @Schema(description = "备注")
    private String remark;
}
