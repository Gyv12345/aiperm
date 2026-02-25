package com.devlovecode.aiperm.modules.system.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "角色数据")
public class RoleDTO {

    // ========== 分页查询参数 ==========

    @JsonView(Views.Query.class)
    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @JsonView(Views.Query.class)
    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;

    // ========== 业务字段 ==========

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "角色名称")
    @NotBlank(message = "角色名称不能为空", groups = {Views.Create.class, Views.Update.class})
    @Size(max = 100, message = "角色名称不能超过100个字符")
    private String roleName;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "角色编码")
    @NotBlank(message = "角色编码不能为空", groups = {Views.Create.class, Views.Update.class})
    @Size(max = 100, message = "角色编码不能超过100个字符")
    private String roleCode;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "显示顺序")
    private Integer sort;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "角色状态（0=正常，1=停用）")
    private Integer status;

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "备注")
    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;

    // ========== 关联字段 ==========

    @JsonView(Views.Query.class)
    @Schema(description = "角色ID（用于分配菜单）")
    private Long roleId;

    @JsonView(Views.Query.class)
    @Schema(description = "菜单ID列表")
    private List<Long> menuIds;
}
