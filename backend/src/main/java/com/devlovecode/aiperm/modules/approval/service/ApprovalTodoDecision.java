package com.devlovecode.aiperm.modules.approval.service;

/**
 * 待我审批页面的下一步决策器。
 */
public final class ApprovalTodoDecision {

    private ApprovalTodoDecision() {
    }

    public static Output resolve(Input input) {
        if (!input.platformEnabled()) {
            return new Output("CONTACT_ADMIN_ENABLE_PLATFORM", "管理员尚未启用该平台审批通道");
        }
        if (!input.oauthBound()) {
            return new Output("BIND_OAUTH", "当前账号未绑定平台账号");
        }
        if (input.enabledSceneCount() <= 0) {
            return new Output("CONTACT_ADMIN_CONFIG_SCENE", "平台已启用，但未检测到可用审批场景");
        }
        return new Output("OPEN_PLATFORM_TODO", "可以前往企业IM处理待办");
    }

    public record Input(boolean platformEnabled, boolean oauthBound, int enabledSceneCount) {
    }

    public record Output(String nextStep, String nextStepReason) {
    }
}

