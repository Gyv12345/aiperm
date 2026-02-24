package com.devlovecode.aiperm.modules.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应VO
 *
 * @author DevLoveCode
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginVO {

    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "用户信息")
    private UserInfo userInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户信息")
    public static class UserInfo {

        @Schema(description = "用户ID", example = "1")
        private Long id;

        @Schema(description = "用户名", example = "admin")
        private String username;

        @Schema(description = "昵称", example = "管理员")
        private String nickname;

        @Schema(description = "头像", example = "https://example.com/avatar.jpg")
        private String avatar;

        @Schema(description = "邮箱", example = "admin@example.com")
        private String email;

        @Schema(description = "手机号", example = "13800138000")
        private String phone;
    }
}
