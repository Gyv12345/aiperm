package com.devlovecode.aiperm;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModulithArchitectureTest {

	@Test
	void shouldVerifyModuleDependenciesAndWriteDocumentation() {
		ApplicationModules modules = ApplicationModules.of(AipermApplication.class);
		modules.verify();

		Documenter documenter = new Documenter(modules);
		documenter.writeModulesAsPlantUml();
		documenter.writeIndividualModulesAsPlantUml();
	}

}
