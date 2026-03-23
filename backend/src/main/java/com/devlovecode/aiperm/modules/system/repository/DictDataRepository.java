package com.devlovecode.aiperm.modules.system.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.system.entity.SysDictData;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DictDataRepository extends BaseJpaRepository<SysDictData> {

    /**
     * 根据字典类型查询（启用状态，按排序）
     */
    List<SysDictData> findByDictTypeAndStatusAndDeletedOrderBySortAsc(String dictType, Integer status, Integer deleted);
}
