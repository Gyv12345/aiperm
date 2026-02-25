package com.devlovecode.aiperm.modules.system.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "岗位数据")
public class PostDTO {

    // ========== 分页查询参数 ==========

    @JsonView(Views.Query.class)
    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @JsonView(Views.Query.class)
    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;

    @JsonView(Views.Query.class)
    @Schema(description = "岗位名称（模糊查询）")
    private String postName;

    @JsonView(Views.Query.class)
    @Schema(description = "岗位编码（模糊查询）")
    private String postCode;

    // ========== 业务字段 ==========

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "岗位名称")
    @NotBlank(message = "岗位名称不能为空", groups = {Views.Create.class, Views.Update.class})
    @Size(max = 100, message = "岗位名称不能超过100个字符")
    private String postName;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "岗位编码")
    @NotBlank(message = "岗位编码不能为空", groups = {Views.Create.class, Views.Update.class})
    @Size(max = 100, message = "岗位编码不能超过100个字符")
    private String postCode;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "显示顺序")
    private Integer sort;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "岗位状态（0=正常，1=停用）")
    private Integer status;

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "备注")
    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
