package com.devlovecode.aiperm.modules.enterprise.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "消息数据")
public class MessageDTO {

    // ========== 分页查询参数 ==========

    @JsonView(Views.Query.class)
    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @JsonView(Views.Query.class)
    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;

    // ========== 业务字段 ==========

    @JsonView({Views.Create.class})
    @Schema(description = "接收人ID")
    @NotNull(message = "接收人不能为空", groups = Views.Create.class)
    private Long receiverId;

    @JsonView({Views.Create.class})
    @Schema(description = "标题")
    @NotBlank(message = "标题不能为空", groups = Views.Create.class)
    @Size(max = 200, message = "标题不能超过200个字符")
    private String title;

    @JsonView({Views.Create.class})
    @Schema(description = "内容")
    private String content;

    @JsonView(Views.Query.class)
    @Schema(description = "是否已读：0-未读 1-已读")
    private Integer isRead;

    @JsonView(Views.Query.class)
    @Schema(description = "消息箱体：1-收件箱 2-发件箱")
    private Integer boxType = 1;

    // ========== 批量操作参数 ==========

    @Schema(description = "消息ID列表（用于批量已读）")
    private List<Long> ids;
}
