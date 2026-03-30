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

	ALL(1, "全部数据"), DEPT(2, "本部门数据"), DEPT_AND_CHILD(3, "本部门及下级部门数据"), SELF(4, "仅本人数据");

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
