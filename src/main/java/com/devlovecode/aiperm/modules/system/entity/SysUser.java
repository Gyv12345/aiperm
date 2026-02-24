package com.devlovecode.aiperm.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devlovecode.aiperm.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 *
 * @author devlovecode
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "用户实体")
public class SysUser extends BaseEntity {

    @Schema(description = "用户名")
    @TableField("username")
    private String username;

    @Schema(description = "密码")
    @TableField("password")
    private String password;

    @Schema(description = "昵称")
    @TableField("nickname")
    private String nickname;

    @Schema(description = "真实姓名")
    @TableField("real_name")
    private String realName;

    @Schema(description = "用户邮箱")
    @TableField("email")
    private String email;

    @Schema(description = "手机号码")
    @TableField("phone")
    private String phone;

    @Schema(description = "用户性别（0=未知，1=男，2=女）")
    @TableField("gender")
    private Integer gender;

    @Schema(description = "头像地址")
    @TableField("avatar")
    private String avatar;

    @Schema(description = "部门ID")
    @TableField("dept_id")
    private Long deptId;

    @Schema(description = "岗位ID列表（逗号分隔）")
    @TableField("post_ids")
    private String postIds;

    @Schema(description = "角色ID列表（逗号分隔）")
    @TableField("role_ids")
    private String roleIds;

    @Schema(description = "用户状态（0=正常，1=停用）")
    @TableField("status")
    private Integer status;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "最后登录IP")
    @TableField("last_login_ip")
    private String lastLoginIp;

    @Schema(description = "最后登录时间")
    @TableField("last_login_time")
    private java.time.LocalDateTime lastLoginTime;
}
