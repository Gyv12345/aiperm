package com.devlovecode.aiperm.modules.approval.service;

import com.devlovecode.aiperm.modules.approval.entity.SysApprovalInstance;
import com.devlovecode.aiperm.modules.approval.entity.SysApprovalScene;
import com.devlovecode.aiperm.modules.approval.handler.ApprovalHandler;
import com.devlovecode.aiperm.modules.approval.repository.ApprovalInstanceRepository;
import com.devlovecode.aiperm.modules.approval.repository.ApprovalSceneRepository;
import com.devlovecode.aiperm.modules.im.adapter.CallbackData;
import com.devlovecode.aiperm.modules.im.adapter.ImPlatformAdapter;
import com.devlovecode.aiperm.modules.im.adapter.ImPlatformAdapterFactory;
import com.devlovecode.aiperm.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalCallbackService {

    private final ImPlatformAdapterFactory adapterFactory;
    private final ApprovalInstanceRepository instanceRepo;
    private final ApprovalSceneRepository sceneRepo;
    private final NotificationService notificationService;
    private final ApplicationContext applicationContext;

    @Transactional
    public String handle(String platform, String body, Map<String, String> headers) {
        ImPlatformAdapter adapter = adapterFactory.getAdapter(platform);
        if (!adapter.verifySignature(body, headers)) {
            log.warn("approval callback signature invalid, platform={}", platform);
            return "fail";
        }

        CallbackData data = adapter.parseCallback(body, headers);
        if (data.getInstanceId() == null || data.getInstanceId().isBlank()) {
            return "success";
        }

        Optional<SysApprovalInstance> instanceOpt = instanceRepo.findByPlatformInstanceId(data.getInstanceId());
        if (instanceOpt.isEmpty()) {
            return "success";
        }

        SysApprovalInstance instance = instanceOpt.get();
        String status = normalizeStatus(data.getStatus());
        if ("PENDING".equals(status)) {
            return "success";
        }

        instanceRepo.updateStatus(instance.getId(), status, LocalDateTime.now(), "callback:" + platform);

        SysApprovalScene scene = sceneRepo.findBySceneCode(instance.getSceneCode()).orElse(null);
        ApprovalHandler handler = resolveHandler(scene);
        if ("APPROVED".equals(status)) {
            handler.onApproved(instance);
        } else {
            handler.onRejected(instance);
        }

        Map<String, Object> vars = new HashMap<>();
        vars.put("businessType", instance.getBusinessType());
        vars.put("businessId", instance.getBusinessId());
        String templateCode = "APPROVED".equals(status) ? "APPROVAL_PASSED" : "APPROVAL_REJECTED";
        notificationService.send(templateCode, vars, java.util.List.of(instance.getInitiatorId()));

        return "success";
    }

    private ApprovalHandler resolveHandler(SysApprovalScene scene) {
        String beanName = scene == null ? null : scene.getHandlerClass();
        if (beanName != null && !beanName.isBlank() && applicationContext.containsBean(beanName)) {
            Object bean = applicationContext.getBean(beanName);
            if (bean instanceof ApprovalHandler handler) {
                return handler;
            }
        }
        return applicationContext.getBean("defaultApprovalHandler", ApprovalHandler.class);
    }

    private String normalizeStatus(String raw) {
        if (raw == null) return "PENDING";
        String value = raw.trim().toUpperCase();
        return switch (value) {
            case "APPROVED", "PASS", "PASSED" -> "APPROVED";
            case "REJECTED", "REJECT", "DENIED" -> "REJECTED";
            case "CANCELED", "CANCELLED" -> "CANCELED";
            default -> "PENDING";
        };
    }
}
