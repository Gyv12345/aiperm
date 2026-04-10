package com.devlovecode.aiperm.modules.auth.captcha.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.auth.captcha.dto.CaptchaConfigDTO;
import com.devlovecode.aiperm.modules.auth.captcha.entity.SysCaptchaConfig;
import com.devlovecode.aiperm.modules.auth.captcha.repository.CaptchaConfigRepository;
import com.devlovecode.aiperm.modules.auth.captcha.vo.CaptchaConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 验证码配置服务
 */
@Service
@RequiredArgsConstructor
public class CaptchaConfigService {

	private final CaptchaConfigRepository captchaConfigRepo;

	/** 获取配置（密钥脱敏） */
	public CaptchaConfigVO getConfig(String type) {
		SysCaptchaConfig config = captchaConfigRepo.findByType(type.toUpperCase())
			.orElseThrow(() -> new BusinessException("配置不存在：" + type));
		return toVO(config);
	}

	/** 更新配置 */
	@Transactional
	public void updateConfig(String type, CaptchaConfigDTO dto) {
		SysCaptchaConfig config = captchaConfigRepo.findByType(type.toUpperCase())
			.orElseThrow(() -> new BusinessException("配置不存在：" + type));

		config.setEnabled(dto.getEnabled());
		config.setSmsProvider(dto.getSmsProvider());

		// 密钥：空值代表不更新（前端脱敏显示，不传空值）
		if (dto.getSmsAccessKey() != null && !dto.getSmsAccessKey().contains("****")) {
			config.setSmsAccessKey(dto.getSmsAccessKey());
		}
		if (dto.getSmsSecretKey() != null && !dto.getSmsSecretKey().isBlank()) {
			config.setSmsSecretKey(dto.getSmsSecretKey());
		}
		config.setSmsSignName(dto.getSmsSignName());
		config.setSmsTemplateCode(dto.getSmsTemplateCode());
		config.setEmailHost(dto.getEmailHost());
		config.setEmailPort(dto.getEmailPort());
		config.setEmailUsername(dto.getEmailUsername());
		if (dto.getEmailPassword() != null && !dto.getEmailPassword().isBlank()) {
			config.setEmailPassword(dto.getEmailPassword());
		}
		config.setEmailFrom(dto.getEmailFrom());
		config.setEmailFromName(dto.getEmailFromName());
		config.setCodeLength(dto.getCodeLength());
		config.setExpireMinutes(dto.getExpireMinutes());
		config.setDailyLimit(dto.getDailyLimit());
		config.setUpdateBy(StpUtil.getLoginIdAsString());

		captchaConfigRepo.save(config);
	}

	private CaptchaConfigVO toVO(SysCaptchaConfig entity) {
		CaptchaConfigVO vo = new CaptchaConfigVO();
		vo.setId(entity.getId());
		vo.setType(entity.getType());
		vo.setEnabled(entity.getEnabled());
		vo.setSmsProvider(entity.getSmsProvider());
		// 密钥脱敏
		vo.setSmsAccessKey(desensitize(entity.getSmsAccessKey()));
		vo.setSmsSignName(entity.getSmsSignName());
		vo.setSmsTemplateCode(entity.getSmsTemplateCode());
		vo.setEmailHost(entity.getEmailHost());
		vo.setEmailPort(entity.getEmailPort());
		vo.setEmailUsername(entity.getEmailUsername());
		vo.setEmailFrom(entity.getEmailFrom());
		vo.setEmailFromName(entity.getEmailFromName());
		vo.setCodeLength(entity.getCodeLength());
		vo.setExpireMinutes(entity.getExpireMinutes());
		vo.setDailyLimit(entity.getDailyLimit());
		return vo;
	}

	private String desensitize(String value) {
		if (value == null || value.length() <= 4)
			return value;
		return value.substring(0, 4) + "****";
	}

}
