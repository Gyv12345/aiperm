package com.devlovecode.aiperm.modules.notification.service;

import com.devlovecode.aiperm.modules.im.adapter.ImPlatformAdapter;
import com.devlovecode.aiperm.modules.im.adapter.ImPlatformAdapterFactory;
import com.devlovecode.aiperm.modules.im.entity.SysImConfig;
import com.devlovecode.aiperm.modules.im.repository.ImConfigRepository;
import com.devlovecode.aiperm.modules.notification.entity.SysMessageLog;
import com.devlovecode.aiperm.modules.notification.entity.SysMessageTemplate;
import com.devlovecode.aiperm.modules.notification.repository.MessageLogRepository;
import com.devlovecode.aiperm.modules.notification.repository.MessageTemplateRepository;
import com.devlovecode.aiperm.modules.oauth.entity.SysUserOauth;
import com.devlovecode.aiperm.modules.oauth.repository.UserOauthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final List<String> PLATFORM_ORDER = List.of("WEWORK", "DINGTALK", "FEISHU");

    private final MessageTemplateRepository templateRepo;
    private final MessageLogRepository messageLogRepo;
    private final UserOauthRepository userOauthRepo;
    private final ImConfigRepository imConfigRepo;
    private final ImPlatformAdapterFactory adapterFactory;

    public void send(String templateCode, Map<String, Object> variables, List<Long> receiverIds) {
        if (receiverIds == null || receiverIds.isEmpty()) {
            return;
        }
        Optional<SysMessageTemplate> templateOpt = templateRepo.findByTemplateCode(templateCode);
        if (templateOpt.isEmpty()) {
            log.warn("消息模板不存在: {}", templateCode);
            return;
        }

        SysMessageTemplate template = templateOpt.get();
        String title = renderTemplate(template.getTitle(), variables);
        String content = renderTemplate(template.getContent(), variables);

        for (Long receiverId : receiverIds) {
            sendToUser(template, receiverId, title, content);
        }
    }

    private void sendToUser(SysMessageTemplate template, Long receiverId, String title, String content) {
        PlatformBinding binding = resolveBinding(receiverId, template.getPlatform());
        if (binding == null) {
            log.warn("用户未绑定可用IM平台, receiverId={}, templateCode={}", receiverId, template.getTemplateCode());
            return;
        }

        SysMessageLog logEntity = new SysMessageLog();
        logEntity.setTemplateCode(template.getTemplateCode());
        logEntity.setPlatform(binding.platform());
        logEntity.setReceiverId(receiverId);
        logEntity.setPlatformUserId(binding.platformUserId());
        logEntity.setTitle(title);
        logEntity.setContent(content);
        logEntity.setCreateBy("system");
        Long logId = messageLogRepo.insertPending(logEntity);

        try {
            ImPlatformAdapter adapter = adapterFactory.getAdapter(binding.platform());
            adapter.sendMessage(binding.platformUserId(), title, content);
            messageLogRepo.markSuccess(logId);
        } catch (Exception e) {
            messageLogRepo.markFailed(logId, e.getMessage());
            log.warn("发送IM消息失败, platform={}, receiverId={}, err={}", binding.platform(), receiverId, e.getMessage());
        }
    }

    private PlatformBinding resolveBinding(Long receiverId, String preferredPlatform) {
        if (preferredPlatform != null && !preferredPlatform.isBlank()) {
            String platform = preferredPlatform.toUpperCase();
            if (isPlatformEnabled(platform)) {
                Optional<SysUserOauth> preferred = userOauthRepo.findByUserIdAndPlatform(receiverId, platform);
                if (preferred.isPresent() && preferred.get().getStatus() != null && preferred.get().getStatus() == 1) {
                    return new PlatformBinding(platform, preferred.get().getOpenId());
                }
            }
        }

        for (String platform : PLATFORM_ORDER) {
            if (!isPlatformEnabled(platform)) continue;
            Optional<SysUserOauth> binding = userOauthRepo.findByUserIdAndPlatform(receiverId, platform);
            if (binding.isPresent() && binding.get().getStatus() != null && binding.get().getStatus() == 1) {
                return new PlatformBinding(platform, binding.get().getOpenId());
            }
        }
        return null;
    }

    private boolean isPlatformEnabled(String platform) {
        return imConfigRepo.findByPlatform(platform).map(SysImConfig::getEnabled).orElse(0) == 1;
    }

    private String renderTemplate(String template, Map<String, Object> variables) {
        if (template == null) return "";
        if (variables == null || variables.isEmpty()) return template;
        String rendered = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String key = "${" + entry.getKey() + "}";
            rendered = rendered.replace(key, entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
        }
        return rendered;
    }

    private record PlatformBinding(String platform, String platformUserId) {}
}
