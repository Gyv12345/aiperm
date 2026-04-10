package com.devlovecode.aiperm.modules.auth.oauth.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.auth.oauth.dto.OauthConfigDTO;
import com.devlovecode.aiperm.modules.auth.oauth.entity.SysOauthConfig;
import com.devlovecode.aiperm.modules.auth.oauth.repository.OauthConfigRepository;
import com.devlovecode.aiperm.modules.auth.oauth.vo.OauthConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * OAuth 配置服务
 */
@Service
@RequiredArgsConstructor
public class OauthConfigService {

	private final OauthConfigRepository oauthConfigRepo;

	public OauthConfigVO getConfig(String platform) {
		SysOauthConfig config = oauthConfigRepo.findByPlatform(platform.toUpperCase())
			.orElseThrow(() -> new BusinessException("平台配置不存在：" + platform));
		return toVO(config);
	}

	@Transactional
	public void updateConfig(String platform, OauthConfigDTO dto) {
		SysOauthConfig config = oauthConfigRepo.findByPlatform(platform.toUpperCase())
			.orElseThrow(() -> new BusinessException("平台配置不存在：" + platform));

		config.setEnabled(dto.getEnabled());
		config.setCorpId(dto.getCorpId());
		config.setAgentId(dto.getAgentId());

		if (dto.getAppKey() != null && !dto.getAppKey().contains("****")) {
			config.setAppKey(dto.getAppKey());
		}
		if (dto.getAppSecret() != null && !dto.getAppSecret().isBlank()) {
			config.setAppSecret(dto.getAppSecret());
		}
		config.setCallbackUrl(dto.getCallbackUrl());
		config.setRemark(dto.getRemark());
		config.setUpdateBy(StpUtil.getLoginIdAsString());
		config.setUpdateTime(LocalDateTime.now());

		oauthConfigRepo.save(config);
	}

	private OauthConfigVO toVO(SysOauthConfig entity) {
		OauthConfigVO vo = new OauthConfigVO();
		vo.setId(entity.getId());
		vo.setPlatform(entity.getPlatform());
		vo.setEnabled(entity.getEnabled());
		vo.setCorpId(entity.getCorpId());
		vo.setAgentId(entity.getAgentId());
		vo.setAppKey(desensitize(entity.getAppKey()));
		vo.setCallbackUrl(entity.getCallbackUrl());
		vo.setRemark(entity.getRemark());
		return vo;
	}

	private String desensitize(String value) {
		if (value == null || value.length() <= 4)
			return value;
		return value.substring(0, 4) + "****";
	}

}
