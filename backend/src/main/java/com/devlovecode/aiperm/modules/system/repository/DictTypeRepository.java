package com.devlovecode.aiperm.modules.system.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.modules.system.entity.SysDictType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DictTypeRepository extends BaseJpaRepository<SysDictType> {

    /**
     * 根据字典类型查询
     */
    Optional<SysDictType> findByDictType(String dictType);

    /**
     * 检查字典类型是否存在
     */
    boolean existsByDictType(String dictType);

    /**
     * 检查字典类型是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(dt) > 0 FROM SysDictType dt WHERE dt.dictType = :dictType AND dt.id != :id AND dt.deleted = 0")
    boolean existsByDictTypeExcludeId(@Param("dictType") String dictType, @Param("id") Long excludeId);

    /**
     * 查询启用的字典类型列表
     */
    List<SysDictType> findByStatusAndDeletedOrderByCreateTimeDesc(Integer status, Integer deleted);

    /**
     * 分页查询
     */
    default Page<SysDictType> queryPage(String dictName, String dictType, Integer status, int pageNum, int pageSize) {
        return findAll(SpecificationUtils.and(
                SpecificationUtils.like("dictName", dictName),
                SpecificationUtils.like("dictType", dictType),
                SpecificationUtils.eq("status", status)
        ), PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime")));
    }
}
