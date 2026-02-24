package com.devlovecode.aiperm.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devlovecode.aiperm.common.enums.ErrorCode;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.system.entity.SysDictData;
import com.devlovecode.aiperm.modules.system.mapper.SysDictDataMapper;
import com.devlovecode.aiperm.modules.system.service.ISysDictDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData>
        implements ISysDictDataService {

    @Override
    public List<SysDictData> listByDictType(String dictType) {
        return list(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, 1)
                .orderByAsc(SysDictData::getSort));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(SysDictData dictData) {
        save(dictData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysDictData dictData) {
        if (getById(dictData.getId()) == null) {
            throw new BusinessException(ErrorCode.FAIL.getCode(), "字典数据不存在");
        }
        updateById(dictData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (getById(id) == null) {
            throw new BusinessException(ErrorCode.FAIL.getCode(), "字典数据不存在");
        }
        removeById(id);
    }
}
