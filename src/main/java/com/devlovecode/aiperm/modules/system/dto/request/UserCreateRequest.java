package com.devlovecode.aiperm.modules.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户创建请求DTO
 *
 * @author DevLoveCode
 */
@Data
@Schema(description = "用户创建请求")
public class UserCreateRequest {

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "zhangsan")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度必须在2-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    @Schema(description = "昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "昵称不能为空")
    @Size(max = 30, message = "昵称长度不能超过30个字符")
    private String nickname;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过50个字符")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "性别（0=未知，1=男，2=女）", example = "1")
    private Integer gender;

    @Schema(description = "头像", example = "https://example.com/avatar.jpg")
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatar;

    @Schema(description = "部门ID", example = "1")
    private Long deptId;

    @Schema(description = "角色ID列表", example = "[1, 2]")
    private java.util.List<Long> roleIds;

    @Schema(description = "状态（0=禁用，1=启用）", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "这是备注信息")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
