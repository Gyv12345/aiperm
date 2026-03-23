package com.devlovecode.aiperm.modules.approval.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.modules.approval.dto.ApprovalSceneDTO;
import com.devlovecode.aiperm.modules.approval.entity.SysApprovalScene;
import com.devlovecode.aiperm.modules.approval.repository.ApprovalSceneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalSceneService {

    private final ApprovalSceneRepository sceneRepo;

    public PageResult<SysApprovalScene> queryPage(ApprovalSceneDTO dto) {
        Specification<SysApprovalScene> spec = SpecificationUtils.and(
                SpecificationUtils.like("sceneCode", dto.getSceneCode()),
                SpecificationUtils.like("sceneName", dto.getSceneName()),
                SpecificationUtils.eq("platform", dto.getPlatform()),
                SpecificationUtils.eq("enabled", dto.getEnabled())
        );
        PageRequest pageRequest = PageRequest.of(dto.getPage() - 1, dto.getPageSize());
        Page<SysApprovalScene> page = sceneRepo.findAll(spec, pageRequest);
        return PageResult.fromJpaPage(page);
    }

    public SysApprovalScene findById(Long id) {
        return sceneRepo.findById(id).orElseThrow(() -> new BusinessException("审批场景不存在"));
    }

    @Transactional
    public void create(ApprovalSceneDTO dto) {
        if (sceneRepo.existsBySceneCode(dto.getSceneCode())) {
            throw new BusinessException("场景编码已存在");
        }
        SysApprovalScene entity = new SysApprovalScene();
        entity.setSceneCode(dto.getSceneCode());
        entity.setSceneName(dto.getSceneName());
        entity.setPlatform(dto.getPlatform());
        entity.setTemplateId(dto.getTemplateId());
        entity.setEnabled(dto.getEnabled() == null ? 1 : dto.getEnabled());
        entity.setHandlerClass(dto.getHandlerClass());
        entity.setTimeoutHours(dto.getTimeoutHours() == null ? 72 : dto.getTimeoutHours());
        entity.setTimeoutAction(dto.getTimeoutAction() == null ? "NOTIFY" : dto.getTimeoutAction());
        entity.setCreateTime(LocalDateTime.now());
        entity.setCreateBy(getCurrentUsername());
        sceneRepo.save(entity);
    }

    @Transactional
    public void update(Long id, ApprovalSceneDTO dto) {
        SysApprovalScene entity = sceneRepo.findById(id).orElseThrow(() -> new BusinessException("审批场景不存在"));
        if (sceneRepo.existsBySceneCodeAndIdNot(dto.getSceneCode(), id)) {
            throw new BusinessException("场景编码已存在");
        }
        entity.setSceneCode(dto.getSceneCode());
        entity.setSceneName(dto.getSceneName());
        entity.setPlatform(dto.getPlatform());
        entity.setTemplateId(dto.getTemplateId());
        entity.setEnabled(dto.getEnabled());
        entity.setHandlerClass(dto.getHandlerClass());
        entity.setTimeoutHours(dto.getTimeoutHours());
        entity.setTimeoutAction(dto.getTimeoutAction());
        entity.setUpdateBy(getCurrentUsername());
        entity.setUpdateTime(LocalDateTime.now());
        sceneRepo.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        if (!sceneRepo.existsById(id)) {
            throw new BusinessException("审批场景不存在");
        }
        sceneRepo.softDelete(id, LocalDateTime.now());
    }

    private String getCurrentUsername() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            return "system";
        }
    }
}
