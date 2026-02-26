package com.devlovecode.aiperm.modules.system.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.system.dto.DictDataDTO;
import com.devlovecode.aiperm.modules.system.entity.SysDictData;
import com.devlovecode.aiperm.modules.system.repository.DictDataRepository;
import com.devlovecode.aiperm.modules.system.vo.DictDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DictDataService {

    private final DictDataRepository dictDataRepo;

    /**
     * 根据字典类型查询
     */
    public List<DictDataVO> listByDictType(String dictType) {
        return dictDataRepo.findByDictType(dictType).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 创建
     */
    @Transactional
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

        dictDataRepo.insert(entity);
    }

    /**
     * 更新
     */
    @Transactional
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

        dictDataRepo.update(entity);
    }

    /**
     * 删除
     */
    @Transactional
    public void delete(Long id) {
        if (!dictDataRepo.existsById(id)) {
            throw new BusinessException("字典数据不存在");
        }
        dictDataRepo.deleteById(id);
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
