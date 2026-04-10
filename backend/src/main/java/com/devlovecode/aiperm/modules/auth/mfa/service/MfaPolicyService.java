package com.devlovecode.aiperm.modules.auth.mfa.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.auth.mfa.dto.MfaPolicyDTO;
import com.devlovecode.aiperm.modules.auth.mfa.entity.SysMfaPolicy;
import com.devlovecode.aiperm.modules.auth.mfa.repository.MfaPolicyRepository;
import com.devlovecode.aiperm.modules.auth.mfa.vo.MfaPolicyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 2FA 策略配置服务
 */
@Service
@RequiredArgsConstructor
public class MfaPolicyService {

	private final MfaPolicyRepository mfaPolicyRepo;

	public List<MfaPolicyVO> listAll() {
		return mfaPolicyRepo.findByEnabled(1).stream().map(this::toVO).collect(Collectors.toList());
	}

	@Transactional
	public void create(MfaPolicyDTO dto) {
		SysMfaPolicy policy = new SysMfaPolicy();
		policy.setName(dto.getName());
		policy.setPermPattern(dto.getPermPattern());
		policy.setApiPattern(dto.getApiPattern());
		policy.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : 1);
		policy.setCreateTime(LocalDateTime.now());
		policy.setCreateBy(StpUtil.getLoginIdAsString());
		mfaPolicyRepo.save(policy);
	}

	@Transactional
	public void update(Long id, MfaPolicyDTO dto) {
		SysMfaPolicy policy = mfaPolicyRepo.findById(id).orElseThrow(() -> new BusinessException("策略不存在"));
		policy.setName(dto.getName());
		policy.setPermPattern(dto.getPermPattern());
		policy.setApiPattern(dto.getApiPattern());
		policy.setEnabled(dto.getEnabled());
		policy.setUpdateTime(LocalDateTime.now());
		policy.setUpdateBy(StpUtil.getLoginIdAsString());
		mfaPolicyRepo.save(policy);
	}

	@Transactional
	public void delete(Long id) {
		mfaPolicyRepo.softDelete(id, LocalDateTime.now());
	}

	private MfaPolicyVO toVO(SysMfaPolicy entity) {
		MfaPolicyVO vo = new MfaPolicyVO();
		vo.setId(entity.getId());
		vo.setName(entity.getName());
		vo.setPermPattern(entity.getPermPattern());
		vo.setApiPattern(entity.getApiPattern());
		vo.setEnabled(entity.getEnabled());
		vo.setCreateTime(entity.getCreateTime());
		return vo;
	}

}
