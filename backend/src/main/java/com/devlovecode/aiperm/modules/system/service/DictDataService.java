package com.devlovecode.aiperm.modules.system.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.system.dto.DictDataDTO;
import com.devlovecode.aiperm.modules.system.entity.SysDictData;
import com.devlovecode.aiperm.modules.system.repository.DictDataRepository;
import com.devlovecode.aiperm.modules.system.vo.DictDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "dict")
public class DictDataService {

    private final DictDataRepository dictDataRepo;

    /**
     * 根据字典类型查询
     */
    @Cacheable(key = "#dictType")
    public List<DictDataVO> listByDictType(String dictType) {
        return dictDataRepo.findByDictTypeAndStatusAndDeletedOrderBySortAsc(dictType, 1, 0).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 创建
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public void create(DictDataDTO dto) {
        SysDictData entity = new SysDictData();
        entity.setDictType(dto.getDictType());
        entity.setDictLabel(dto.getDictLabel());
        entity.setDictValue(dto.getDictValue());
        entity.setSort(dto.getSort() != null ? dto.getSort() : 0);
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        entity.setListClass(dto.getListClass() != null ? dto.getListClass() : "");
        entity.setRemark(dto.getRemark());
        entity.setCreateBy(getCurrentUsername());
        entity.setCreateTime(LocalDateTime.now());

        dictDataRepo.save(entity);
    }

    /**
     * 更新
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public void update(Long id, DictDataDTO dto) {
        SysDictData entity = dictDataRepo.findById(id)
                .orElseThrow(() -> new BusinessException("字典数据不存在"));

        entity.setDictLabel(dto.getDictLabel());
        entity.setDictValue(dto.getDictValue());
        entity.setSort(dto.getSort());
        entity.setStatus(dto.getStatus());
        entity.setListClass(dto.getListClass() != null ? dto.getListClass() : "");
        entity.setRemark(dto.getRemark());
        entity.setUpdateBy(getCurrentUsername());
        entity.setUpdateTime(LocalDateTime.now());

        dictDataRepo.save(entity);
    }

    /**
     * 删除
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public void delete(Long id) {
        if (!dictDataRepo.existsById(id)) {
            throw new BusinessException("字典数据不存在");
        }
        dictDataRepo.softDelete(id, LocalDateTime.now());
    }

    // ========== 私有方法 ==========

    private DictDataVO toVO(SysDictData entity) {
        DictDataVO vo = new DictDataVO();
        vo.setId(entity.getId());
        vo.setDictType(entity.getDictType());
        vo.setDictLabel(entity.getDictLabel());
        vo.setDictValue(entity.getDictValue());
        vo.setSort(entity.getSort());
        vo.setStatus(entity.getStatus());
        vo.setListClass(entity.getListClass());
        vo.setRemark(entity.getRemark());
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
