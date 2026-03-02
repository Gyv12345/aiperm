package com.devlovecode.aiperm.modules.notification.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "消息记录查询DTO")
public class MessageLogDTO {
    @JsonView(Views.Query.class)
    private Integer page = 1;

    @JsonView(Views.Query.class)
    private Integer pageSize = 10;

    @JsonView(Views.Query.class)
    private String templateCode;

    @JsonView(Views.Query.class)
    private String platform;

    @JsonView(Views.Query.class)
    private String status;
}
