package com.devlovecode.aiperm.modules.enterprise.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "消息响应VO")
public class MessageVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "发送人ID")
    private Long senderId;

    @Schema(description = "发送人名称")
    private String senderName;

    @Schema(description = "接收人ID")
    private Long receiverId;

    @Schema(description = "接收人名称")
    private String receiverName;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "是否已读：0-未读 1-已读")
    private Integer isRead;

    @Schema(description = "阅读时间")
    private LocalDateTime readTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
