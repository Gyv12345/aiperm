package com.devlovecode.aiperm.modules.system.api;

public record SystemUserAccount(Long id, String username, String password, Integer status, String nickname,
		String avatar, String email, String phone) {
}
