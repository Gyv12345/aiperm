package com.devlovecode.aiperm.modules.enterprise.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "公告通知响应VO")
public class NoticeVO {

	@Schema(description = "ID")
	private Long id;

	@Schema(description = "标题")
	private String title;

	@Schema(description = "内容")
	private String content;

	@Schema(description = "类型：1-通知 2-公告")
	private Integer type;

	@Schema(description = "状态：0-草稿 1-发布")
	private Integer status;

	@Schema(description = "发布时间")
	private LocalDateTime publishTime;

	@Schema(description = "创建时间")
	private LocalDateTime createTime;

	@Schema(description = "创建人")
	private String createBy;

}
