package com.devlovecode.aiperm.modules.web.controller;

import com.devlovecode.aiperm.modules.enterprise.controller.SysNoticeController;
import com.devlovecode.aiperm.modules.audit.controller.SysOperLogController;
import com.devlovecode.aiperm.modules.auth.oauth.controller.OAuthController;
import com.devlovecode.aiperm.modules.storage.controller.OssController;
import com.devlovecode.aiperm.modules.system.profile.controller.ProfileController;
import com.devlovecode.aiperm.modules.system.rbac.controller.SysUserController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("控制器 RequestParam 注解约束测试")
class ControllerRequestParamAnnotationTest {

	@Test
	@DisplayName("所有 RequestParam 都应显式声明参数名")
	void requestParamsShouldDeclareExplicitNames() {
		List<Class<?>> controllerClasses = List.of(SysNoticeController.class, SysOperLogController.class,
				OAuthController.class, OssController.class, ProfileController.class, SysUserController.class);

		for (Class<?> controllerClass : controllerClasses) {
			for (Method method : controllerClass.getDeclaredMethods()) {
				Parameter[] parameters = method.getParameters();
				for (int i = 0; i < parameters.length; i++) {
					RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
					if (requestParam == null) {
						continue;
					}

					boolean hasExplicitName = !requestParam.name().isBlank() || !requestParam.value().isBlank();
					int finalI = i;
					assertFalse(!hasExplicitName, () -> controllerClass.getSimpleName() + "#" + method.getName() + " 第 "
							+ (finalI + 1) + " 个参数缺少显式 RequestParam 名称");
				}
			}
		}
	}

}
