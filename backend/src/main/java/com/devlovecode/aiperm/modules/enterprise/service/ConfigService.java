package com.devlovecode.aiperm.modules.enterprise.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.enterprise.dto.ConfigDTO;
import com.devlovecode.aiperm.modules.enterprise.entity.SysConfig;
import com.devlovecode.aiperm.modules.enterprise.repository.ConfigRepository;
import com.devlovecode.aiperm.modules.enterprise.vo.ConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final ConfigRepository configRepo;

    /**
     * 分页查询
     */
    public PageResult<ConfigVO> queryPage(ConfigDTO dto) {
        PageResult<SysConfig> result = configRepo.queryPage(
                dto.getConfigKey(), dto.getConfigType(),
                dto.getPage(), dto.getPageSize()
        );
        return result.map(this::toVO);
    }

    /**
     * 查询详情
     */
    public ConfigVO findById(Long id) {
        return configRepo.findById(id)
                .map(this::toVO)
                .orElseThrow(() -> new BusinessException("系统配置不存在"));
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

        configRepo.insert(entity);

        // 获取自增ID
        return configRepo.findByConfigKey(dto.getConfigKey())
                .map(SysConfig::getId)
                .orElse(null);
    }

    /**
     * 更新
     */
    @Transactional
    public void update(Long id, ConfigDTO dto) {
        SysConfig entity = configRepo.findById(id)
                .orElseThrow(() -> new BusinessException("系统配置不存在"));

        // 校验配置键是否重复
        if (configRepo.existsByConfigKeyExcludeId(dto.getConfigKey(), id)) {
            throw new BusinessException("配置键已存在");
        }

        entity.setConfigKey(dto.getConfigKey());
        entity.setConfigValue(dto.getConfigValue());
        entity.setConfigType(dto.getConfigType());
        entity.setRemark(dto.getRemark());
        entity.setUpdateBy(getCurrentUsername());

        configRepo.update(entity);
    }

    /**
     * 删除
     */
    @Transactional
    public void delete(Long id) {
        if (!configRepo.existsById(id)) {
            throw new BusinessException("系统配置不存在");
        }
        configRepo.deleteById(id);
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
        } catch (Exception e) {
            return "system";
        }
    }
}
