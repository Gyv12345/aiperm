package com.devlovecode.aiperm.modules.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 角色创建请求DTO
 *
 * @author DevLoveCode
 */
@Data
@Schema(description = "角色创建请求")
public class RoleCreateRequest {

    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "管理员")
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String roleName;

    @Schema(description = "角色编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过50个字符")
    private String roleCode;

    @Schema(description = "排序", example = "1")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "状态（0=禁用，1=启用）", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "这是备注信息")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    @Schema(description = "菜单ID列表", example = "[1, 2, 3]")
    private List<Long> menuIds;
}
