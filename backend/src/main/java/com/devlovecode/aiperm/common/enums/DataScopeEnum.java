package com.devlovecode.aiperm.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据权限范围枚举
 *
 * @author DevLoveCode
 */
@Getter
@AllArgsConstructor
public enum DataScopeEnum {

	// 编码顺序即宽窄顺序：MIN(data_scope) 取最宽范围，CUSTOM=5 编码最窄，
	// 仅在用户全部启用角色均为 CUSTOM 时生效
	ALL(1, "全部数据"), DEPT(2, "本部门数据"), DEPT_AND_CHILD(3, "本部门及下级部门数据"), SELF(4, "仅本人数据"),
	CUSTOM(5, "自定义部门");

	private final Integer code;

	private final String desc;

	public static DataScopeEnum of(Integer code) {
		if (code == null) {
			return ALL;
		}
		for (DataScopeEnum value : values()) {
			if (value.getCode().equals(code)) {
				return value;
			}
		}
		return ALL;
	}

}
