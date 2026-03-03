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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("待我审批聚合服务测试")
class ApprovalTodoOverviewServiceTest {

    @Mock
    private ImConfigRepository imConfigRepo;
    @Mock
    private UserOauthRepository userOauthRepo;
    @Mock
    private ApprovalSceneRepository approvalSceneRepo;
    @Mock
    private ApprovalInstanceRepository approvalInstanceRepo;
    @Mock
    private MessageLogRepository messageLogRepo;

    @InjectMocks
    private ApprovalTodoOverviewService overviewService;

    @Test
    @DisplayName("非管理员不应返回管理员诊断区")
    void shouldHideAdminDiagnosticsForNonAdmin() {
        SysImConfig feishu = new SysImConfig();
        feishu.setPlatform("FEISHU");
        feishu.setEnabled(1);

        when(imConfigRepo.findByPlatform("FEISHU")).thenReturn(Optional.of(feishu));
        when(userOauthRepo.findByUserId(1L)).thenReturn(List.of());
        when(approvalSceneRepo.countEnabledByPlatform("FEISHU")).thenReturn(2);

        ApprovalTodoOverviewVO result = overviewService.buildOverview(1L, false, "FEISHU");

        assertEquals("BIND_OAUTH", result.getUserGuide().getNextStep());
        assertNull(result.getAdminDiagnostics());
    }

    @Test
    @DisplayName("管理员应看到缺失字段与最近回调/推送摘要")
    void shouldIncludeAdminDiagnosticsForAdmin() {
        SysImConfig feishu = new SysImConfig();
        feishu.setPlatform("FEISHU");
        feishu.setEnabled(1);
        feishu.setAppId("");
        feishu.setAppSecret("");
        feishu.setCallbackToken("");

        SysUserOauth binding = new SysUserOauth();
        binding.setPlatform("FEISHU");

        SysApprovalInstance approval = new SysApprovalInstance();
        approval.setPlatform("FEISHU");
        approval.setSceneCode("ORDER_APPROVAL");
        approval.setPlatformInstanceId("FEISHU-001");
        approval.setStatus("APPROVED");
        approval.setResultTime(LocalDateTime.of(2026, 3, 3, 10, 20));

        SysMessageLog messageLog = new SysMessageLog();
        messageLog.setPlatform("FEISHU");
        messageLog.setTemplateCode("APPROVAL_SUBMIT");
        messageLog.setStatus("FAILED");
        messageLog.setErrorMsg("invalid token");
        messageLog.setSendTime(LocalDateTime.of(2026, 3, 3, 10, 19));

        when(imConfigRepo.findByPlatform("FEISHU")).thenReturn(Optional.of(feishu));
        when(imConfigRepo.findAll()).thenReturn(List.of(feishu));
        when(userOauthRepo.findByUserId(1L)).thenReturn(List.of(binding));
        when(approvalSceneRepo.countEnabledByPlatform(anyString())).thenReturn(0);
        when(approvalSceneRepo.countEnabledByPlatform("FEISHU")).thenReturn(1);
        when(approvalSceneRepo.findOneEnabledSceneCodeByPlatform(anyString())).thenReturn(Optional.empty());
        when(approvalSceneRepo.findOneEnabledSceneCodeByPlatform("FEISHU")).thenReturn(Optional.of("ORDER_APPROVAL"));
        when(approvalInstanceRepo.findLatestFinishedByPlatform("FEISHU")).thenReturn(Optional.of(approval));
        when(messageLogRepo.findLatestByPlatform("FEISHU")).thenReturn(Optional.of(messageLog));

        ApprovalTodoOverviewVO result = overviewService.buildOverview(1L, true, "FEISHU");
        ApprovalTodoOverviewVO.PlatformCheck feishuCheck = result.getAdminDiagnostics().getPlatformChecks().stream()
                .filter(item -> "FEISHU".equals(item.getPlatform()))
                .findFirst()
                .orElseThrow();

        assertNotNull(result.getAdminDiagnostics());
        assertEquals("OPEN_PLATFORM_TODO", result.getUserGuide().getNextStep());
        assertTrue(feishuCheck.getMissingFields().contains("appId"));
        assertEquals("FEISHU-001", result.getAdminDiagnostics().getLatestApprovalCallback().getPlatformInstanceId());
        assertEquals("FAILED", result.getAdminDiagnostics().getLatestMessagePush().getStatus());
    }
}
