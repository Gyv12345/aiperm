package com.devlovecode.aiperm.modules.mfa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 2FA验证请求 DTO
 */
@Data
@Schema(description = "2FA验证请求")
public class MfaVerifyDTO {

    @Schema(description = "TOTP 6位验证码")
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须为6位数字")
    private String code;
}
