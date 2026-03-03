package com.devlovecode.aiperm.modules.approval.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApprovalTodoOverviewVO {
    private Viewer viewer;
    private UserGuide userGuide;
    private List<QuickAction> quickActions;
    private AdminDiagnostics adminDiagnostics;

    @Data
    public static class Viewer {
        private Long userId;
        private Boolean isAdmin;
        private String focusPlatform;
    }

    @Data
    public static class UserGuide {
        private Boolean platformEnabled;
        private Boolean oauthBound;
        private Integer enabledSceneCount;
        private String nextStep;
        private String nextStepReason;
    }

    @Data
    public static class QuickAction {
        private String code;
        private Boolean enabled;
        private String reason;
        private String url;
    }

    @Data
    public static class AdminDiagnostics {
        private List<PlatformCheck> platformChecks;
        private List<SceneCheck> sceneChecks;
        private LatestApprovalCallback latestApprovalCallback;
        private LatestMessagePush latestMessagePush;
    }

    @Data
    public static class PlatformCheck {
        private String platform;
        private Boolean enabled;
        private Boolean configReady;
        private List<String> missingFields;
    }

    @Data
    public static class SceneCheck {
        private String platform;
        private Integer enabledSceneCount;
        private String sampleSceneCode;
    }

    @Data
    public static class LatestApprovalCallback {
        private String platform;
        private String sceneCode;
        private String platformInstanceId;
        private String status;
        private LocalDateTime resultTime;
    }

    @Data
    public static class LatestMessagePush {
        private String platform;
        private String templateCode;
        private String status;
        private String errorMsg;
        private LocalDateTime sendTime;
    }
}

