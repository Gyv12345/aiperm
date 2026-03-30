package com.devlovecode.aiperm.modules.system.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "部门数据")
public class DeptDTO {

	@JsonView({ Views.Create.class, Views.Update.class, Views.Query.class })
	@Schema(description = "部门名称")
	@NotBlank(message = "部门名称不能为空", groups = { Views.Create.class, Views.Update.class })
	@Size(max = 100, message = "部门名称不能超过100个字符")
	private String deptName;

	@JsonView({ Views.Create.class, Views.Update.class, Views.Query.class })
	@Schema(description = "父部门ID（0为根部门）")
	private Long parentId = 0L;

	@JsonView({ Views.Create.class, Views.Update.class, Views.Query.class })
	@Schema(description = "显示顺序")
	private Integer sort;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "负责人")
	@Size(max = 50, message = "负责人不能超过50个字符")
	private String leader;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "联系电话")
	@Size(max = 20, message = "联系电话不能超过20个字符")
	private String phone;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "邮箱")
	@Size(max = 100, message = "邮箱不能超过100个字符")
	private String email;

	@JsonView({ Views.Create.class, Views.Update.class, Views.Query.class })
	@Schema(description = "部门状态（0=正常，1=停用）")
	private Integer status;

	@JsonView({ Views.Create.class, Views.Update.class })
	@Schema(description = "备注")
	@Size(max = 500, message = "备注不能超过500个字符")
	private String remark;

}
