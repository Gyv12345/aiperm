package com.devlovecode.aiperm.modules.im.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.im.dto.ImConfigDTO;
import com.devlovecode.aiperm.modules.im.entity.SysImConfig;
import com.devlovecode.aiperm.modules.im.repository.ImConfigRepository;
import com.devlovecode.aiperm.modules.im.vo.ImConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ImConfigService {

    private final ImConfigRepository imConfigRepo;

    public List<ImConfigVO> list() {
        return imConfigRepo.findAll().stream().map(this::toVO).toList();
    }

    public ImConfigVO getByPlatform(String platform) {
        SysImConfig config = imConfigRepo.findByPlatform(normPlatform(platform))
                .orElseThrow(() -> new BusinessException("平台配置不存在: " + platform));
        return toVO(config);
    }

    @Transactional
    public void update(String platform, ImConfigDTO dto) {
        SysImConfig config = imConfigRepo.findByPlatform(normPlatform(platform))
                .orElseThrow(() -> new BusinessException("平台配置不存在: " + platform));

        if (dto.getEnabled() != null) config.setEnabled(dto.getEnabled());
        if (dto.getAppId() != null) config.setAppId(dto.getAppId());
        if (dto.getCorpId() != null) config.setCorpId(dto.getCorpId());
        if (dto.getCallbackToken() != null) config.setCallbackToken(dto.getCallbackToken());
        if (dto.getCallbackAesKey() != null) config.setCallbackAesKey(dto.getCallbackAesKey());
        if (dto.getExtraConfig() != null) config.setExtraConfig(dto.getExtraConfig());
        if (dto.getAppSecret() != null && !dto.getAppSecret().isBlank() && !dto.getAppSecret().contains("****")) {
            config.setAppSecret(dto.getAppSecret());
        }
        config.setUpdateBy(getCurrentUsername());

        imConfigRepo.update(config);
    }

    private String normPlatform(String platform) {
        return platform == null ? "" : platform.trim().toUpperCase(Locale.ROOT);
    }

    private ImConfigVO toVO(SysImConfig entity) {
        ImConfigVO vo = new ImConfigVO();
        vo.setId(entity.getId());
        vo.setPlatform(entity.getPlatform());
        vo.setEnabled(entity.getEnabled());
        vo.setAppId(entity.getAppId());
        vo.setAppSecret(desensitize(entity.getAppSecret()));
        vo.setCorpId(entity.getCorpId());
        vo.setCallbackToken(desensitize(entity.getCallbackToken()));
        vo.setCallbackAesKey(desensitize(entity.getCallbackAesKey()));
        vo.setExtraConfig(entity.getExtraConfig());
        return vo;
    }

    private String desensitize(String value) {
        if (value == null || value.length() <= 6) return value;
        return value.substring(0, 3) + "****" + value.substring(value.length() - 3);
    }

    private String getCurrentUsername() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            return "system";
        }
    }
}
