package com.devlovecode.aiperm.modules.captcha.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "发送验证码请求")
public class SendCaptchaDTO {

    @Schema(description = "目标地址（手机号或邮箱）")
    @NotBlank(message = "目标地址不能为空")
    private String target;

    @Schema(description = "类型：SMS/EMAIL")
    @NotBlank(message = "类型不能为空")
    private String type;

    @Schema(description = "场景：LOGIN/BIND/RESET")
    @NotBlank(message = "场景不能为空")
    private String scene;
}
