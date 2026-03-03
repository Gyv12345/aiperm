package com.devlovecode.aiperm.modules.approval.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("待我审批下一步决策测试")
class ApprovalTodoDecisionTest {

    @Test
    @DisplayName("平台未启用时应提示联系管理员开通")
    void shouldRequireEnablePlatformWhenPlatformDisabled() {
        ApprovalTodoDecision.Output output = ApprovalTodoDecision.resolve(
                new ApprovalTodoDecision.Input(false, false, 0)
        );
        assertEquals("CONTACT_ADMIN_ENABLE_PLATFORM", output.nextStep());
    }

    @Test
    @DisplayName("平台已启用但用户未绑定时应引导绑定")
    void shouldRequireBindingWhenNotBound() {
        ApprovalTodoDecision.Output output = ApprovalTodoDecision.resolve(
                new ApprovalTodoDecision.Input(true, false, 2)
        );
        assertEquals("BIND_OAUTH", output.nextStep());
    }

    @Test
    @DisplayName("已绑定但无可用场景时应提示配置场景")
    void shouldRequireSceneConfigWhenNoEnabledScene() {
        ApprovalTodoDecision.Output output = ApprovalTodoDecision.resolve(
                new ApprovalTodoDecision.Input(true, true, 0)
        );
        assertEquals("CONTACT_ADMIN_CONFIG_SCENE", output.nextStep());
    }

    @Test
    @DisplayName("条件满足时应可进入平台待办")
    void shouldOpenPlatformTodoWhenReady() {
        ApprovalTodoDecision.Output output = ApprovalTodoDecision.resolve(
                new ApprovalTodoDecision.Input(true, true, 1)
        );
        assertEquals("OPEN_PLATFORM_TODO", output.nextStep());
    }
}

