package com.devlovecode.aiperm.modules.system.api;

import java.time.LocalDateTime;

public record SystemUserAccount(Long id, String username, String password, Integer status, String nickname,
		String avatar, String email, String phone, LocalDateTime pwdUpdateTime) {
}
