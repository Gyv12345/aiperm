package com.devlovecode.aiperm.modules.notification.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.notification.dto.MessageTemplateDTO;
import com.devlovecode.aiperm.modules.notification.entity.SysMessageTemplate;
import com.devlovecode.aiperm.modules.notification.repository.MessageTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageTemplateService {

    private final MessageTemplateRepository templateRepo;

    public PageResult<SysMessageTemplate> queryPage(MessageTemplateDTO dto) {
        return templateRepo.queryPage(dto.getTemplateCode(), dto.getCategory(), dto.getPlatform(), dto.getPage(), dto.getPageSize());
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
        entity.setCreateBy(getCurrentUsername());
        templateRepo.insert(entity);
    }

    @Transactional
    public void update(Long id, MessageTemplateDTO dto) {
        SysMessageTemplate entity = templateRepo.findById(id).orElseThrow(() -> new BusinessException("模板不存在"));
        if (templateRepo.existsByTemplateCodeExcludeId(dto.getTemplateCode(), id)) {
            throw new BusinessException("模板编码已存在");
        }
        entity.setTemplateCode(dto.getTemplateCode());
        entity.setTemplateName(dto.getTemplateName());
        entity.setCategory(dto.getCategory());
        entity.setPlatform(dto.getPlatform());
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setUpdateBy(getCurrentUsername());
        templateRepo.update(entity);
    }

    @Transactional
    public void delete(Long id) {
        if (!templateRepo.existsById(id)) {
            throw new BusinessException("模板不存在");
        }
        templateRepo.deleteById(id);
    }

    private String getCurrentUsername() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            return "system";
        }
    }
}
