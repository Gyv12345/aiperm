package com.devlovecode.aiperm.modules.agent.service;

import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.agent.dto.LlmProviderDTO;
import com.devlovecode.aiperm.modules.agent.entity.SysLlmProvider;
import com.devlovecode.aiperm.modules.agent.repository.LlmProviderRepository;
import com.devlovecode.aiperm.modules.agent.vo.LlmProviderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmProviderService {

    private final LlmProviderRepository providerRepo;

    public List<LlmProviderVO> listAll() {
        return providerRepo.findAll().stream()
            .map(this::toVO)
            .collect(Collectors.toList());
    }

    public LlmProviderVO findById(Long id) {
        return providerRepo.findById(id)
            .map(this::toVO)
            .orElseThrow(() -> new BusinessException("提供商不存在"));
    }

    @Transactional
    public Long create(LlmProviderDTO dto) {
        // 检查名称是否已存在
        if (providerRepo.findByName(dto.getName()).isPresent()) {
            throw new BusinessException("提供商名称已存在");
        }

        SysLlmProvider entity = new SysLlmProvider();
        entity.setName(dto.getName());
        entity.setDisplayName(dto.getDisplayName());
        String protocol = normalizeProtocol(dto.getProtocol());
        entity.setProtocol(protocol);
        entity.setApiKey(dto.getApiKey());
        entity.setBaseUrl(dto.getBaseUrl() != null ? dto.getBaseUrl() : getDefaultBaseUrl(dto.getName(), protocol));
        entity.setModel(dto.getModel());
        entity.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        entity.setSort(dto.getSort() != null ? dto.getSort() : 0);
        entity.setRemark(dto.getRemark());

        providerRepo.insert(entity);

        // 如果设为默认，清除其他默认
        if (Boolean.TRUE.equals(entity.getIsDefault())) {
            providerRepo.clearDefault();
            providerRepo.setDefault(entity.getId());
        }

        return entity.getId();
    }

    @Transactional
    public void update(Long id, LlmProviderDTO dto) {
        SysLlmProvider entity = providerRepo.findById(id)
            .orElseThrow(() -> new BusinessException("提供商不存在"));

        entity.setDisplayName(dto.getDisplayName());
        entity.setProtocol(normalizeProtocol(dto.getProtocol()));
        if (dto.getApiKey() != null) {
            entity.setApiKey(dto.getApiKey());
        }
        entity.setBaseUrl(dto.getBaseUrl());
        entity.setModel(dto.getModel());
        entity.setStatus(dto.getStatus());
        entity.setSort(dto.getSort());
        entity.setRemark(dto.getRemark());

        providerRepo.update(entity);

        // 处理默认设置
        if (Boolean.TRUE.equals(dto.getIsDefault()) && !Boolean.TRUE.equals(entity.getIsDefault())) {
            providerRepo.clearDefault();
            providerRepo.setDefault(id);
        }
    }

    @Transactional
    public void delete(Long id) {
        SysLlmProvider provider = providerRepo.findById(id)
            .orElseThrow(() -> new BusinessException("提供商不存在"));

        if (Boolean.TRUE.equals(provider.getIsDefault())) {
            throw new BusinessException("不能删除默认提供商");
        }

        providerRepo.deleteById(id);
    }

    @Transactional
    public void setDefault(Long id) {
        if (providerRepo.findById(id).isEmpty()) {
            throw new BusinessException("提供商不存在");
        }

        providerRepo.clearDefault();
        providerRepo.setDefault(id);
    }

    private String getDefaultBaseUrl(String name, String protocol) {
        if ("anthropic".equals(protocol)) {
            return "https://api.anthropic.com";
        }
        return switch (name) {
            case "deepseek" -> "https://api.deepseek.com";
            case "qwen" -> "https://dashscope.aliyuncs.com/compatible-mode/v1";
            case "openai" -> "https://api.openai.com";
            default -> null;
        };
    }

    private String normalizeProtocol(String protocol) {
        if (protocol == null) {
            throw new BusinessException("协议不能为空");
        }
        String p = protocol.toLowerCase(Locale.ROOT).trim();
        if (!"openai".equals(p) && !"anthropic".equals(p)) {
            throw new BusinessException("协议仅支持 openai 或 anthropic");
        }
        return p;
    }

    private LlmProviderVO toVO(SysLlmProvider entity) {
        LlmProviderVO vo = new LlmProviderVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setDisplayName(entity.getDisplayName());
        vo.setProtocol(entity.getProtocol());
        vo.setBaseUrl(entity.getBaseUrl());
        vo.setModel(entity.getModel());
        vo.setIsDefault(entity.getIsDefault());
        vo.setStatus(entity.getStatus());
        vo.setSort(entity.getSort());
        vo.setRemark(entity.getRemark());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (entity.getCreateTime() != null) {
            vo.setCreateTime(entity.getCreateTime().format(formatter));
        }
        if (entity.getUpdateTime() != null) {
            vo.setUpdateTime(entity.getUpdateTime().format(formatter));
        }

        return vo;
    }
}
