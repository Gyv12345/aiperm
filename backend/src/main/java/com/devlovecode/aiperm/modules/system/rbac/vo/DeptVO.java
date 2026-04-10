package com.devlovecode.aiperm.modules.system.rbac.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门响应VO
 *
 * @author DevLoveCode
 */
@Data
@Schema(description = "部门信息")
public class DeptVO {

	@Schema(description = "部门ID", example = "1")
	private Long id;

	@Schema(description = "父部门ID", example = "0")
	private Long parentId;

	@Schema(description = "部门名称", example = "技术部")
	private String deptName;

	@Schema(description = "排序", example = "1")
	private Integer sort;

	@Schema(description = "负责人", example = "张三")
	private String leader;

	@Schema(description = "联系电话", example = "13800138000")
	private String phone;

	@Schema(description = "邮箱", example = "tech@example.com")
	private String email;

	@Schema(description = "状态（0=禁用，1=启用）", example = "1")
	private Integer status;

	@Schema(description = "子部门列表")
	private List<DeptVO> children;

	@Schema(description = "创建时间", example = "2024-01-01 12:00:00")
	private LocalDateTime createTime;

	@Schema(description = "更新时间", example = "2024-01-01 12:00:00")
	private LocalDateTime updateTime;

}
