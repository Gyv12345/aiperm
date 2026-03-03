package com.devlovecode.aiperm.modules.approval.service;

import com.devlovecode.aiperm.modules.approval.entity.SysApprovalInstance;
import com.devlovecode.aiperm.modules.approval.repository.ApprovalInstanceRepository;
import com.devlovecode.aiperm.modules.approval.repository.ApprovalSceneRepository;
import com.devlovecode.aiperm.modules.approval.vo.ApprovalTodoOverviewVO;
import com.devlovecode.aiperm.modules.im.entity.SysImConfig;
import com.devlovecode.aiperm.modules.im.repository.ImConfigRepository;
import com.devlovecode.aiperm.modules.notification.entity.SysMessageLog;
import com.devlovecode.aiperm.modules.notification.repository.MessageLogRepository;
import com.devlovecode.aiperm.modules.oauth.entity.SysUserOauth;
import com.devlovecode.aiperm.modules.oauth.repository.UserOauthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApprovalTodoOverviewService {

    private static final List<String> PLATFORM_ORDER = List.of("WEWORK", "DINGTALK", "FEISHU");

    private final ImConfigRepository imConfigRepo;
    private final UserOauthRepository userOauthRepo;
    private final ApprovalSceneRepository approvalSceneRepo;
    private final ApprovalInstanceRepository approvalInstanceRepo;
    private final MessageLogRepository messageLogRepo;

    public ApprovalTodoOverviewVO buildOverview(Long userId, boolean isAdmin, String platform) {
        String focusPlatform = normalizePlatform(platform);
        SysImConfig config = imConfigRepo.findByPlatform(focusPlatform).orElse(null);

        boolean platformEnabled = config != null && config.getEnabled() != null && config.getEnabled() == 1;
        boolean oauthBound = userOauthRepo.findByUserId(userId).stream()
                .map(SysUserOauth::getPlatform)
                .anyMatch(p -> focusPlatform.equalsIgnoreCase(p));
        int enabledSceneCount = approvalSceneRepo.countEnabledByPlatform(focusPlatform);

        ApprovalTodoDecision.Output decision = ApprovalTodoDecision.resolve(
                new ApprovalTodoDecision.Input(platformEnabled, oauthBound, enabledSceneCount)
        );

        ApprovalTodoOverviewVO vo = new ApprovalTodoOverviewVO();
        vo.setViewer(buildViewer(userId, isAdmin, focusPlatform));
        vo.setUserGuide(buildUserGuide(platformEnabled, oauthBound, enabledSceneCount, decision));
        vo.setQuickActions(buildQuickActions(focusPlatform, decision));
        if (isAdmin) {
            vo.setAdminDiagnostics(buildAdminDiagnostics(focusPlatform));
        }
        return vo;
    }

    private ApprovalTodoOverviewVO.Viewer buildViewer(Long userId, boolean isAdmin, String focusPlatform) {
        ApprovalTodoOverviewVO.Viewer viewer = new ApprovalTodoOverviewVO.Viewer();
        viewer.setUserId(userId);
        viewer.setIsAdmin(isAdmin);
        viewer.setFocusPlatform(focusPlatform);
        return viewer;
    }

    private ApprovalTodoOverviewVO.UserGuide buildUserGuide(boolean platformEnabled,
                                                            boolean oauthBound,
                                                            int enabledSceneCount,
                                                            ApprovalTodoDecision.Output decision) {
        ApprovalTodoOverviewVO.UserGuide guide = new ApprovalTodoOverviewVO.UserGuide();
        guide.setPlatformEnabled(platformEnabled);
        guide.setOauthBound(oauthBound);
        guide.setEnabledSceneCount(enabledSceneCount);
        guide.setNextStep(decision.nextStep());
        guide.setNextStepReason(decision.nextStepReason());
        return guide;
    }

    private List<ApprovalTodoOverviewVO.QuickAction> buildQuickActions(String platform,
                                                                       ApprovalTodoDecision.Output decision) {
        ApprovalTodoOverviewVO.QuickAction openTodo = new ApprovalTodoOverviewVO.QuickAction();
        openTodo.setCode("OPEN_PLATFORM_TODO");
        openTodo.setEnabled("OPEN_PLATFORM_TODO".equals(decision.nextStep()));
        openTodo.setReason(openTodo.getEnabled() ? "" : decision.nextStepReason());
        openTodo.setUrl(getPlatformTodoUrl(platform));

        ApprovalTodoOverviewVO.QuickAction bindOauth = new ApprovalTodoOverviewVO.QuickAction();
        bindOauth.setCode("BIND_OAUTH");
        bindOauth.setEnabled(true);
        bindOauth.setReason("");
        bindOauth.setUrl("/api/oauth/bind/" + platform);

        ApprovalTodoOverviewVO.QuickAction myApproval = new ApprovalTodoOverviewVO.QuickAction();
        myApproval.setCode("VIEW_MY_APPROVAL");
        myApproval.setEnabled(true);
        myApproval.setReason("");
        myApproval.setUrl("/approval/my");

        return List.of(openTodo, bindOauth, myApproval);
    }

    private ApprovalTodoOverviewVO.AdminDiagnostics buildAdminDiagnostics(String focusPlatform) {
        ApprovalTodoOverviewVO.AdminDiagnostics diagnostics = new ApprovalTodoOverviewVO.AdminDiagnostics();
        diagnostics.setPlatformChecks(buildPlatformChecks());
        diagnostics.setSceneChecks(buildSceneChecks());
        diagnostics.setLatestApprovalCallback(buildLatestApprovalCallback(focusPlatform));
        diagnostics.setLatestMessagePush(buildLatestMessagePush(focusPlatform));
        return diagnostics;
    }

    private List<ApprovalTodoOverviewVO.PlatformCheck> buildPlatformChecks() {
        List<SysImConfig> configs = imConfigRepo.findAll();
        List<ApprovalTodoOverviewVO.PlatformCheck> checks = new ArrayList<>();
        for (String platform : PLATFORM_ORDER) {
            SysImConfig config = configs.stream()
                    .filter(item -> platform.equalsIgnoreCase(item.getPlatform()))
                    .findFirst()
                    .orElse(null);
            checks.add(buildPlatformCheck(platform, config));
        }
        return checks;
    }

    private ApprovalTodoOverviewVO.PlatformCheck buildPlatformCheck(String platform, SysImConfig config) {
        ApprovalTodoOverviewVO.PlatformCheck check = new ApprovalTodoOverviewVO.PlatformCheck();
        check.setPlatform(platform);
        check.setEnabled(config != null && config.getEnabled() != null && config.getEnabled() == 1);
        List<String> missing = collectMissingFields(platform, config);
        check.setMissingFields(missing);
        check.setConfigReady(check.getEnabled() && missing.isEmpty());
        return check;
    }

    private List<ApprovalTodoOverviewVO.SceneCheck> buildSceneChecks() {
        List<ApprovalTodoOverviewVO.SceneCheck> checks = new ArrayList<>();
        for (String platform : PLATFORM_ORDER) {
            ApprovalTodoOverviewVO.SceneCheck check = new ApprovalTodoOverviewVO.SceneCheck();
            check.setPlatform(platform);
            int count = approvalSceneRepo.countEnabledByPlatform(platform);
            check.setEnabledSceneCount(count);
            check.setSampleSceneCode(approvalSceneRepo.findOneEnabledSceneCodeByPlatform(platform).orElse(null));
            checks.add(check);
        }
        return checks;
    }

    private ApprovalTodoOverviewVO.LatestApprovalCallback buildLatestApprovalCallback(String platform) {
        Optional<SysApprovalInstance> optional = approvalInstanceRepo.findLatestFinishedByPlatform(platform);
        if (optional.isEmpty()) {
            return null;
        }
        SysApprovalInstance instance = optional.get();
        ApprovalTodoOverviewVO.LatestApprovalCallback callback = new ApprovalTodoOverviewVO.LatestApprovalCallback();
        callback.setPlatform(instance.getPlatform());
        callback.setSceneCode(instance.getSceneCode());
        callback.setPlatformInstanceId(instance.getPlatformInstanceId());
        callback.setStatus(instance.getStatus());
        callback.setResultTime(instance.getResultTime());
        return callback;
    }

    private ApprovalTodoOverviewVO.LatestMessagePush buildLatestMessagePush(String platform) {
        Optional<SysMessageLog> optional = messageLogRepo.findLatestByPlatform(platform);
        if (optional.isEmpty()) {
            return null;
        }
        SysMessageLog log = optional.get();
        ApprovalTodoOverviewVO.LatestMessagePush latest = new ApprovalTodoOverviewVO.LatestMessagePush();
        latest.setPlatform(log.getPlatform());
        latest.setTemplateCode(log.getTemplateCode());
        latest.setStatus(log.getStatus());
        latest.setErrorMsg(log.getErrorMsg());
        latest.setSendTime(log.getSendTime());
        return latest;
    }

    private List<String> collectMissingFields(String platform, SysImConfig config) {
        List<String> missing = new ArrayList<>();
        if (config == null) {
            missing.add("platformConfig");
            return missing;
        }
        if (isBlank(config.getAppId())) {
            missing.add("appId");
        }
        if (isBlank(config.getAppSecret())) {
            missing.add("appSecret");
        }
        if (isBlank(config.getCallbackToken())) {
            missing.add("callbackToken");
        }
        if (isBlank(config.getCallbackAesKey())) {
            missing.add("callbackAesKey");
        }
        if ("WEWORK".equals(platform) && isBlank(config.getCorpId())) {
            missing.add("corpId");
        }
        return missing;
    }

    private String normalizePlatform(String platform) {
        if (platform == null || platform.isBlank()) {
            return "FEISHU";
        }
        String normalized = platform.trim().toUpperCase(Locale.ROOT);
        return PLATFORM_ORDER.contains(normalized) ? normalized : "FEISHU";
    }

    private String getPlatformTodoUrl(String platform) {
        return switch (platform) {
            case "WEWORK" -> "https://work.weixin.qq.com/";
            case "DINGTALK" -> "https://www.dingtalk.com/";
            default -> "https://applink.feishu.cn/client/todo";
        };
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty() || value.contains("****");
    }
}

