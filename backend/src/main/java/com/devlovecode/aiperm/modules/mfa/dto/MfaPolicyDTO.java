package com.devlovecode.aiperm.modules.mfa.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 2FA 策略配置 DTO
 */
@Data
@Schema(description = "2FA策略配置")
public class MfaPolicyDTO {

    @JsonView(Views.Query.class)
    private Integer page = 1;

    @JsonView(Views.Query.class)
    private Integer pageSize = 10;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @NotBlank(message = "策略名称不能为空", groups = {Views.Create.class, Views.Update.class})
    private String name;

    @JsonView({Views.Create.class, Views.Update.class})
    private String permPattern;

    @JsonView({Views.Create.class, Views.Update.class})
    private String apiPattern;

    @JsonView({Views.Create.class, Views.Update.class})
    private Integer enabled;
}
