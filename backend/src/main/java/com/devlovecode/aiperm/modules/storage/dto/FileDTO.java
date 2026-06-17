package com.devlovecode.aiperm.modules.storage.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件查询 DTO
 *
 * @author DevLoveCode
 */
@Data
@Schema(description = "文件查询参数")
public class FileDTO {

	@JsonView(Views.Query.class)
	@Schema(description = "原始文件名（模糊）")
	private String originalName;

	@JsonView(Views.Query.class)
	@Schema(description = "文件类型（模糊匹配 MIME）")
	private String fileType;

	@JsonView(Views.Query.class)
	@Schema(description = "存储类型：local/aliyun")
	private String storageType;

	@JsonView(Views.Query.class)
	@Schema(description = "页码", example = "1")
	private Integer page = 1;

	@JsonView(Views.Query.class)
	@Schema(description = "每页条数", example = "10")
	private Integer pageSize = 10;

}
