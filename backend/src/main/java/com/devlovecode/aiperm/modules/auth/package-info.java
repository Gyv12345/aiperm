@ApplicationModule(
        id = "auth",
        displayName = "Authentication",
        allowedDependencies = {"system::api", "audit::api", "monitor::api"})
package com.devlovecode.aiperm.modules.auth;

import org.springframework.modulith.ApplicationModule;
