package com.devlovecode.aiperm.modules.system.api;

public record SystemMenuDescriptor(Long id, String menuName, Long parentId, String menuType, Integer sort,
		String path, String component, String perms, String icon, Integer visible, Integer status) {
}
