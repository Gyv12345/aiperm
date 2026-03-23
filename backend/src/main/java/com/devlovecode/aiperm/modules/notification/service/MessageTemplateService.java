package com.devlovecode.aiperm.modules.notification.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.modules.notification.dto.MessageTemplateDTO;
import com.devlovecode.aiperm.modules.notification.entity.SysMessageTemplate;
import com.devlovecode.aiperm.modules.notification.repository.MessageTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageTemplateService {

    private final MessageTemplateRepository templateRepo;

    public PageResult<SysMessageTemplate> queryPage(MessageTemplateDTO dto) {
        Specification<SysMessageTemplate> spec = SpecificationUtils.and(
                SpecificationUtils.like("templateCode", dto.getTemplateCode()),
                SpecificationUtils.eq("category", dto.getCategory()),
                SpecificationUtils.eq("platform", dto.getPlatform())
        );
        PageRequest pageRequest = PageRequest.of(dto.getPage() - 1, dto.getPageSize());
        Page<SysMessageTemplate> page = templateRepo.findAll(spec, pageRequest);
        return PageResult.fromJpaPage(page);
    }

    public SysMessageTemplate findById(Long id) {
        return templateRepo.findById(id).orElseThrow(() -> new BusinessException("模板不存在"));
    }

    @Transactional
    public void create(MessageTemplateDTO dto) {
        if (templateRepo.existsByTemplateCode(dto.getTemplateCode())) {
            throw new BusinessException("模板编码已存在");
        }
        SysMessageTemplate entity = new SysMessageTemplate();
        entity.setTemplateCode(dto.getTemplateCode());
        entity.setTemplateName(dto.getTemplateName());
        entity.setCategory(dto.getCategory());
        entity.setPlatform(dto.getPlatform());
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setCreateTime(LocalDateTime.now());
        entity.setCreateBy(getCurrentUsername());
        templateRepo.save(entity);
    }

    @Transactional
    public void update(Long id, MessageTemplateDTO dto) {
        SysMessageTemplate entity = templateRepo.findById(id).orElseThrow(() -> new BusinessException("模板不存在"));
        if (templateRepo.existsByTemplateCodeAndIdNot(dto.getTemplateCode(), id)) {
            throw new BusinessException("模板编码已存在");
        }
        entity.setTemplateCode(dto.getTemplateCode());
        entity.setTemplateName(dto.getTemplateName());
        entity.setCategory(dto.getCategory());
        entity.setPlatform(dto.getPlatform());
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setUpdateBy(getCurrentUsername());
        entity.setUpdateTime(LocalDateTime.now());
        templateRepo.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        if (!templateRepo.existsById(id)) {
            throw new BusinessException("模板不存在");
        }
        templateRepo.softDelete(id, LocalDateTime.now());
    }

    private String getCurrentUsername() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            return "system";
        }
    }
}
