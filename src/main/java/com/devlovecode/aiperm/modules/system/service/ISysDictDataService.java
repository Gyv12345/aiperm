package com.devlovecode.aiperm.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.devlovecode.aiperm.modules.system.entity.SysDictData;

import java.util.List;

public interface ISysDictDataService extends IService<SysDictData> {
    List<SysDictData> listByDictType(String dictType);
    void create(SysDictData dictData);
    void update(SysDictData dictData);
    void delete(Long id);
}
