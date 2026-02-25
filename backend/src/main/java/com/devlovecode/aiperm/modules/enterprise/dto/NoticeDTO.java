package com.devlovecode.aiperm.modules.enterprise.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "公告通知数据")
public class NoticeDTO {

    // ========== 分页查询参数 ==========

    @JsonView(Views.Query.class)
    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @JsonView(Views.Query.class)
    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;

    // ========== 业务字段 ==========

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "标题")
    @NotBlank(message = "标题不能为空", groups = {Views.Create.class, Views.Update.class})
    @Size(max = 200, message = "标题不能超过200个字符")
    private String title;

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "内容")
    private String content;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "类型：1-通知 2-公告")
    private Integer type;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "状态：0-草稿 1-发布")
    private Integer status;
}
