package com.devlovecode.aiperm.modules.system.config.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.modules.system.config.dto.ConfigDTO;
import com.devlovecode.aiperm.modules.system.config.entity.SysConfig;
import com.devlovecode.aiperm.modules.system.config.repository.ConfigRepository;
import com.devlovecode.aiperm.modules.system.config.vo.ConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConfigService {

	private final ConfigRepository configRepo;

	/**
	 * 分页查询
	 */
	public PageResult<ConfigVO> queryPage(ConfigDTO dto) {
		Specification<SysConfig> spec = SpecificationUtils.and(SpecificationUtils.like("configKey", dto.getConfigKey()),
				SpecificationUtils.eq("configType", dto.getConfigType()));
		PageRequest pageRequest = PageRequest.of(dto.getPage() - 1, dto.getPageSize());
		Page<SysConfig> page = configRepo.findAll(spec, pageRequest);
		PageResult<SysConfig> result = PageResult.fromJpaPage(page);
		return result.map(this::toVO);
	}

	/**
	 * 查询详情
	 */
	public ConfigVO findById(Long id) {
		return configRepo.findById(id).map(this::toVO).orElseThrow(() -> new BusinessException("系统配置不存在"));
	}

	/**
	 * 根据配置键查询
	 */
	public ConfigVO findByConfigKey(String configKey) {
		return configRepo.findByConfigKey(configKey)
			.map(this::toVO)
			.orElseThrow(() -> new BusinessException("系统配置不存在"));
	}

	/**
	 * 创建
	 */
	@Transactional
	public Long create(ConfigDTO dto) {
		// 校验配置键是否重复
		if (configRepo.existsByConfigKey(dto.getConfigKey())) {
			throw new BusinessException("配置键已存在");
		}

		SysConfig entity = new SysConfig();
		entity.setConfigKey(dto.getConfigKey());
		entity.setConfigValue(dto.getConfigValue());
		entity.setConfigType(dto.getConfigType());
		entity.setRemark(dto.getRemark());
		entity.setCreateBy(getCurrentUsername());
		entity.setCreateTime(LocalDateTime.now());

		configRepo.save(entity);

		return entity.getId();
	}

	/**
	 * 更新
	 */
	@Transactional
	public void update(Long id, ConfigDTO dto) {
		SysConfig entity = configRepo.findById(id).orElseThrow(() -> new BusinessException("系统配置不存在"));

		// 校验配置键是否重复
		if (configRepo.existsByConfigKeyExcludeId(dto.getConfigKey(), id)) {
			throw new BusinessException("配置键已存在");
		}

		entity.setConfigKey(dto.getConfigKey());
		entity.setConfigValue(dto.getConfigValue());
		entity.setConfigType(dto.getConfigType());
		entity.setRemark(dto.getRemark());
		entity.setUpdateBy(getCurrentUsername());
		entity.setUpdateTime(LocalDateTime.now());

		configRepo.save(entity);
	}

	/**
	 * 删除
	 */
	@Transactional
	public void delete(Long id) {
		if (!configRepo.existsById(id)) {
			throw new BusinessException("系统配置不存在");
		}
		configRepo.softDelete(id, LocalDateTime.now());
	}

	// ========== 私有方法 ==========

	private ConfigVO toVO(SysConfig entity) {
		ConfigVO vo = new ConfigVO();
		vo.setId(entity.getId());
		vo.setConfigKey(entity.getConfigKey());
		vo.setConfigValue(entity.getConfigValue());
		vo.setConfigType(entity.getConfigType());
		vo.setRemark(entity.getRemark());
		vo.setCreateTime(entity.getCreateTime());
		return vo;
	}

	private String getCurrentUsername() {
		try {
			return StpUtil.getLoginIdAsString();
		}
		catch (Exception e) {
			return "system";
		}
	}

}
