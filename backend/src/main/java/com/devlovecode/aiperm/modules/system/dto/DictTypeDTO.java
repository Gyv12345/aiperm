package com.devlovecode.aiperm.modules.system.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "字典类型数据")
public class DictTypeDTO {

    // ========== 分页查询参数（仅 Query 场景）==========

    @JsonView(Views.Query.class)
    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @JsonView(Views.Query.class)
    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;

    // ========== 业务字段（多场景复用）==========

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "字典名称")
    @NotBlank(message = "字典名称不能为空", groups = {Views.Create.class, Views.Update.class})
    @Size(max = 100, message = "字典名称不能超过100个字符")
    private String dictName;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "字典类型")
    @NotBlank(message = "字典类型不能为空", groups = Views.Create.class)
    @Size(max = 100, message = "字典类型不能超过100个字符")
    private String dictType;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "备注")
    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
