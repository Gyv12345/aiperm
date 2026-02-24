package com.devlovecode.aiperm.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.enums.ErrorCode;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.system.entity.SysDictType;
import com.devlovecode.aiperm.modules.system.mapper.SysDictTypeMapper;
import com.devlovecode.aiperm.modules.system.service.ISysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType>
        implements ISysDictTypeService {

    @Override
    public PageResult<SysDictType> page(Integer pageNum, Integer pageSize, String dictName, String dictType) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<SysDictType>()
                .like(dictName != null, SysDictType::getDictName, dictName)
                .like(dictType != null, SysDictType::getDictType, dictType)
                .orderByDesc(SysDictType::getCreateTime);
        Page<SysDictType> result = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(SysDictType dictType) {
        long count = count(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, dictType.getDictType()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.FAIL.getCode(), "字典类型已存在");
        }
        save(dictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysDictType dictType) {
        if (getById(dictType.getId()) == null) {
            throw new BusinessException(ErrorCode.FAIL.getCode(), "字典类型不存在");
        }
        updateById(dictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (getById(id) == null) {
            throw new BusinessException(ErrorCode.FAIL.getCode(), "字典类型不存在");
        }
        removeById(id);
    }
}
