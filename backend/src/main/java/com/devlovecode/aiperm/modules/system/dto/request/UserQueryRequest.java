package com.devlovecode.aiperm.modules.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户查询请求DTO
 *
 * @author DevLoveCode
 */
@Data
@Schema(description = "用户查询请求")
public class UserQueryRequest {

    @Schema(description = "当前页码", example = "1")
    private Long current = 1L;

    @Schema(description = "每页大小", example = "10")
    private Long size = 10L;

    @Schema(description = "用户名（模糊查询）", example = "zhang")
    private String username;

    @Schema(description = "昵称（模糊查询）", example = "张")
    private String nickname;

    @Schema(description = "邮箱（模糊查询）", example = "example")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "部门ID", example = "1")
    private Long deptId;

    @Schema(description = "状态（0=禁用，1=启用）", example = "1")
    private Integer status;

    @Schema(description = "性别（0=未知，1=男，2=女）", example = "1")
    private Integer gender;
}
