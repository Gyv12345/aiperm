package com.devlovecode.aiperm.modules.system.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author devlovecode
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户实体")
public class SysUser extends BaseEntity {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    @JsonIgnore
    private String password;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "用户性别（0=未知，1=男，2=女）")
    private Integer gender;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "岗位ID列表（逗号分隔）")
    private String postIds;

    @Schema(description = "角色ID列表（逗号分隔）")
    private String roleIds;

    @Schema(description = "用户状态（0=正常，1=停用）")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;
}
