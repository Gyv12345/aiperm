package com.devlovecode.aiperm.modules.approval.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.modules.approval.api.ApprovalHandler;
import com.devlovecode.aiperm.modules.approval.dto.ApprovalSceneDTO;
import com.devlovecode.aiperm.modules.approval.entity.SysApprovalScene;
import com.devlovecode.aiperm.modules.approval.repository.ApprovalSceneRepository;
import com.devlovecode.aiperm.modules.approval.vo.ApprovalHandlerVO;
import com.devlovecode.aiperm.modules.approval.vo.ApprovalSceneVO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalSceneService {

	private final ApprovalSceneRepository approvalSceneRepo;

	private final ApplicationContext applicationContext;

	public PageResult<ApprovalSceneVO> queryPage(ApprovalSceneDTO dto) {
		Specification<SysApprovalScene> spec = SpecificationUtils.and(
				SpecificationUtils.like("sceneCode", dto.getSceneCode()),
				SpecificationUtils.like("sceneName", dto.getSceneName()),
				SpecificationUtils.like("businessType", dto.getBusinessType()),
				SpecificationUtils.eq("platform", normalizePlatform(dto.getPlatform())),
				SpecificationUtils.eq("enabled", dto.getEnabled()));
		PageRequest pageRequest = PageRequest.of(dto.getPage() - 1, dto.getPageSize(),
				Sort.by(Sort.Direction.DESC, "createTime"));
		Page<SysApprovalScene> page = approvalSceneRepo.findAll(spec, pageRequest);
		return PageResult.fromJpaPage(page).map(this::toVO);
	}

	public ApprovalSceneVO findById(Long id) {
		return approvalSceneRepo.findByIdAndDeleted(id, 0).map(this::toVO)
			.orElseThrow(() -> new BusinessException("审批场景不存在"));
	}

	public List<ApprovalHandlerVO> listHandlers() {
		return applicationContext.getBeansOfType(ApprovalHandler.class).entrySet().stream()
			.sorted(Comparator.comparing(entry -> entry.getKey().toLowerCase()))
			.map(entry -> {
				ApprovalHandlerVO vo = new ApprovalHandlerVO();
				vo.setBeanName(entry.getKey());
				vo.setDisplayName(entry.getValue().getClass().getSimpleName());
				return vo;
			})
			.toList();
	}

	@Transactional
	public Long create(ApprovalSceneDTO dto) {
		if (approvalSceneRepo.existsBySceneCode(dto.getSceneCode())) {
			throw new BusinessException("场景编码已存在");
		}
		validateHandlerBean(dto.getHandlerBeanName());

		SysApprovalScene entity = new SysApprovalScene();
		applyChanges(entity, dto);
		entity.setCreateBy(getCurrentUsername());
		entity.setCreateTime(LocalDateTime.now());
		approvalSceneRepo.save(entity);
		return entity.getId();
	}

	@Transactional
	public void update(Long id, ApprovalSceneDTO dto) {
		SysApprovalScene entity = approvalSceneRepo.findByIdAndDeleted(id, 0)
			.orElseThrow(() -> new BusinessException("审批场景不存在"));
		if (approvalSceneRepo.existsBySceneCodeExcludeId(dto.getSceneCode(), id)) {
			throw new BusinessException("场景编码已存在");
		}
		validateHandlerBean(dto.getHandlerBeanName());

		applyChanges(entity, dto);
		entity.setUpdateBy(getCurrentUsername());
		entity.setUpdateTime(LocalDateTime.now());
		approvalSceneRepo.save(entity);
	}

	@Transactional
	public void delete(Long id) {
		approvalSceneRepo.findByIdAndDeleted(id, 0).orElseThrow(() -> new BusinessException("审批场景不存在"));
		approvalSceneRepo.softDelete(id, LocalDateTime.now());
	}

	private void applyChanges(SysApprovalScene entity, ApprovalSceneDTO dto) {
		entity.setSceneCode(dto.getSceneCode().trim());
		entity.setSceneName(dto.getSceneName().trim());
		entity.setBusinessType(dto.getBusinessType().trim());
		entity.setPlatform(normalizePlatform(dto.getPlatform()));
		entity.setTemplateId(dto.getTemplateId());
		entity.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : 1);
		entity.setHandlerBeanName(dto.getHandlerBeanName().trim());
		entity.setAutoSubmitEnabled(dto.getAutoSubmitEnabled() != null ? dto.getAutoSubmitEnabled() : 1);
		entity.setAllowDuplicatePending(dto.getAllowDuplicatePending() != null ? dto.getAllowDuplicatePending() : 0);
		entity.setTimeoutHours(dto.getTimeoutHours() != null ? dto.getTimeoutHours() : 72);
		entity.setTimeoutAction(dto.getTimeoutAction());
		entity.setNotifyTemplateCode(dto.getNotifyTemplateCode());
		entity.setRemark(dto.getRemark());
	}

	private void validateHandlerBean(String beanName) {
		if (!applicationContext.containsBean(beanName)) {
			throw new BusinessException("审批处理器不存在：" + beanName);
		}
		Object bean = applicationContext.getBean(beanName);
		if (!(bean instanceof ApprovalHandler)) {
			throw new BusinessException("审批处理器不是 ApprovalHandler 类型：" + beanName);
		}
	}

	private ApprovalSceneVO toVO(SysApprovalScene entity) {
		ApprovalSceneVO vo = new ApprovalSceneVO();
		vo.setId(entity.getId());
		vo.setSceneCode(entity.getSceneCode());
		vo.setSceneName(entity.getSceneName());
		vo.setBusinessType(entity.getBusinessType());
		vo.setPlatform(entity.getPlatform());
		vo.setTemplateId(entity.getTemplateId());
		vo.setEnabled(entity.getEnabled());
		vo.setHandlerBeanName(entity.getHandlerBeanName());
		vo.setAutoSubmitEnabled(entity.getAutoSubmitEnabled());
		vo.setAllowDuplicatePending(entity.getAllowDuplicatePending());
		vo.setTimeoutHours(entity.getTimeoutHours());
		vo.setTimeoutAction(entity.getTimeoutAction());
		vo.setNotifyTemplateCode(entity.getNotifyTemplateCode());
		vo.setRemark(entity.getRemark());
		vo.setCreateTime(entity.getCreateTime());
		return vo;
	}

	private String normalizePlatform(String platform) {
		return platform == null ? null : platform.trim().toUpperCase();
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
