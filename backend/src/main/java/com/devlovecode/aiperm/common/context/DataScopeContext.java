package com.devlovecode.aiperm.common.context;

import com.devlovecode.aiperm.common.enums.DataScopeEnum;

import java.util.Set;

/**
 * 数据权限上下文：结构化的数据权限判定结果，供 Specification 层消费。
 *
 * <p>取代旧的"拼 SQL 字符串"方案（DataScopeService.buildDataScopeSql），避免与 JPA
 * Criteria 查询模式不兼容的问题。业务查询通过 {@link
 * com.devlovecode.aiperm.common.repository.SpecificationUtils#dataScope} 读取本对象生成谓词。
 *
 * @author DevLoveCode
 */
public record DataScopeContext(DataScopeEnum scope, Long userId, Long deptId, Set<Long> deptIds) {

	/**
	 * 构造一个"全部数据"上下文（超管 / 未登录 / 无角色时使用，不过滤）。
	 */
	public static DataScopeContext all() {
		return new DataScopeContext(DataScopeEnum.ALL, null, null, Set.of());
	}

	/**
	 * 是否无需过滤（ALL，或 DEPT/DEPT_AND_CHILD 缺少必要的部门信息）。
	 * 返回 true 时，Specification 层应跳过（返回 null 谓词）。
	 */
	public boolean noFilter() {
		return switch (scope) {
			case ALL -> true;
			case DEPT -> deptId == null || deptId == 0L;
			case DEPT_AND_CHILD -> deptIds == null || deptIds.isEmpty();
			case SELF -> userId == null;
		};
	}

}
