package com.devlovecode.aiperm.modules.system.repository;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryTransactionalTest {

	@Test
	void modifyingMethodsShouldDeclareTransactional() throws NoSuchMethodException {
		assertTrue(UserRepository.class.getMethod("updateLoginInfo", Long.class, String.class, LocalDateTime.class)
			.isAnnotationPresent(Transactional.class), "updateLoginInfo 必须声明 @Transactional");
	}

}
