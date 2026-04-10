package com.devlovecode.aiperm.modules.auth;

import com.devlovecode.aiperm.modules.auth.core.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ApplicationModuleTest(mode = ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
@ActiveProfiles("test")
class AuthModuleTest {

	@Autowired
	private AuthService authService;

	@Test
	void shouldBootstrapAuthModule() {
		assertNotNull(authService);
	}

}
