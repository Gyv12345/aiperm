package com.devlovecode.aiperm.modules.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色查询请求DTO
 *
 * @author DevLoveCode
 */
@Data
@Schema(description = "角色查询请求")
public class RoleQueryRequest {

    @Schema(description = "当前页码", example = "1")
    private Long current = 1L;

    @Schema(description = "每页大小", example = "10")
    private Long size = 10L;

    @Schema(description = "角色名称（模糊查询）", example = "管理")
    private String roleName;

    @Schema(description = "角色编码（模糊查询）", example = "admin")
    private String roleCode;

    @Schema(description = "状态（0=禁用，1=启用）", example = "1")
    private Integer status;
}
