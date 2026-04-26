package com.devlovecode.aiperm.modules.approval.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.modules.approval.dto.ImConfigDTO;
import com.devlovecode.aiperm.modules.approval.entity.SysImConfig;
import com.devlovecode.aiperm.modules.approval.repository.ImConfigRepository;
import com.devlovecode.aiperm.modules.approval.vo.ImConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImConfigService {

	private static final List<String> DEFAULT_PLATFORMS = List.of("FEISHU", "WEWORK", "DINGTALK");

	private final ImConfigRepository imConfigRepo;

	@Transactional
	public List<ImConfigVO> listAll() {
		ensureDefaults();
		return imConfigRepo.findAllByOrderByIdAsc().stream().map(this::toVO).toList();
	}

	@Transactional
	public ImConfigVO getConfig(String platform) {
		return toVO(getOrCreateEntity(platform));
	}

	@Transactional
	public void updateConfig(String platform, ImConfigDTO dto) {
		SysImConfig entity = getOrCreateEntity(platform);
		entity.setEnabled(dto.getEnabled());
		entity.setAppId(dto.getAppId());
		if (dto.getAppSecret() != null && !dto.getAppSecret().isBlank() && !dto.getAppSecret().contains("****")) {
			entity.setAppSecret(dto.getAppSecret());
		}
		entity.setCorpId(dto.getCorpId());
		if (dto.getCallbackToken() != null && !dto.getCallbackToken().isBlank() && !dto.getCallbackToken().contains("****")) {
			entity.setCallbackToken(dto.getCallbackToken());
		}
		if (dto.getCallbackAesKey() != null && !dto.getCallbackAesKey().isBlank()
				&& !dto.getCallbackAesKey().contains("****")) {
			entity.setCallbackAesKey(dto.getCallbackAesKey());
		}
		entity.setExtraConfig(dto.getExtraConfig());
		entity.setRemark(dto.getRemark());
		entity.setUpdateBy(getCurrentUsername());
		entity.setUpdateTime(LocalDateTime.now());
		imConfigRepo.save(entity);
	}

	@Transactional
	public SysImConfig getOrCreateEntity(String platform) {
		String normalizedPlatform = normalizePlatform(platform);
		return imConfigRepo.findByPlatform(normalizedPlatform).orElseGet(() -> {
			SysImConfig entity = new SysImConfig();
			entity.setPlatform(normalizedPlatform);
			entity.setEnabled(0);
			entity.setCreateBy(getCurrentUsername());
			entity.setCreateTime(LocalDateTime.now());
			return imConfigRepo.save(entity);
		});
	}

	public ConfigAssessment assessConfig(SysImConfig entity) {
		List<String> missingFields = new ArrayList<>();
		if (isBlank(entity.getAppId())) {
			missingFields.add("appId");
		}
		if (isBlank(entity.getAppSecret())) {
			missingFields.add("appSecret");
		}
		if (isBlank(entity.getCallbackToken())) {
			missingFields.add("callbackToken");
		}
		if ("WEWORK".equals(entity.getPlatform()) && isBlank(entity.getCorpId())) {
			missingFields.add("corpId");
		}
		return new ConfigAssessment(missingFields.isEmpty(), missingFields);
	}

	private void ensureDefaults() {
		for (String platform : DEFAULT_PLATFORMS) {
			getOrCreateEntity(platform);
		}
	}

	private ImConfigVO toVO(SysImConfig entity) {
		ConfigAssessment assessment = assessConfig(entity);
		ImConfigVO vo = new ImConfigVO();
		vo.setId(entity.getId());
		vo.setPlatform(entity.getPlatform());
		vo.setEnabled(entity.getEnabled());
		vo.setAppId(entity.getAppId());
		vo.setAppSecret(mask(entity.getAppSecret()));
		vo.setCorpId(entity.getCorpId());
		vo.setCallbackToken(mask(entity.getCallbackToken()));
		vo.setCallbackAesKey(mask(entity.getCallbackAesKey()));
		vo.setExtraConfig(entity.getExtraConfig());
		vo.setRemark(entity.getRemark());
		vo.setConfigReady(assessment.ready());
		vo.setMissingFields(assessment.missingFields());
		return vo;
	}

	private String normalizePlatform(String platform) {
		return platform == null ? "" : platform.trim().toUpperCase();
	}

	private String getCurrentUsername() {
		try {
			return StpUtil.getLoginIdAsString();
		}
		catch (Exception e) {
			return "system";
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private String mask(String value) {
		if (isBlank(value)) {
			return value;
		}
		if (value.length() <= 4) {
			return "****";
		}
		return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
	}

	public record ConfigAssessment(boolean ready, List<String> missingFields) {
	}

}
