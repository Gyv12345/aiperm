package com.devlovecode.aiperm.modules.system;

import com.devlovecode.aiperm.modules.system.config.service.ConfigService;
import com.devlovecode.aiperm.modules.system.profile.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ApplicationModuleTest(mode = ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
@ActiveProfiles("test")
class SystemModuleTest {

	@Autowired
	private ProfileService profileService;

	@Autowired
	private ConfigService configService;

	@Test
	void shouldBootstrapSystemModule() {
		assertNotNull(profileService);
		assertNotNull(configService);
	}

}
