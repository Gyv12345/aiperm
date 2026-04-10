package com.devlovecode.aiperm.modules.system.rbac.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.system.rbac.entity.SysDictData;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DictDataRepository extends BaseJpaRepository<SysDictData> {

	/**
	 * 根据字典类型查询（启用状态，按排序）
	 */
	List<SysDictData> findByDictTypeAndStatusAndDeletedOrderBySortAsc(String dictType, Integer status, Integer deleted);

	List<SysDictData> findByDictTypeAndDeletedOrderBySortAsc(String dictType, Integer deleted);

	List<SysDictData> findByDeletedOrderByDictTypeAscSortAsc(Integer deleted);

	Optional<SysDictData> findByDictTypeAndDictValueAndDeleted(String dictType, String dictValue, Integer deleted);

}
