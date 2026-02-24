package com.devlovecode.aiperm.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.modules.system.entity.SysDictType;

public interface ISysDictTypeService extends IService<SysDictType> {
    PageResult<SysDictType> page(Integer pageNum, Integer pageSize, String dictName, String dictType);
    void create(SysDictType dictType);
    void update(SysDictType dictType);
    void delete(Long id);
}
