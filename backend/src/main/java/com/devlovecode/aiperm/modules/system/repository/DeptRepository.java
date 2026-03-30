package com.devlovecode.aiperm.modules.system.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.system.entity.SysDept;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeptRepository extends BaseJpaRepository<SysDept> {

	/**
	 * 根据父ID查询子部门
	 */
	List<SysDept> findByParentIdOrderBySortAsc(Long parentId);

	/**
	 * 查询所有部门（按父ID和排序）
	 */
	List<SysDept> findAllByDeletedOrderByParentIdAscSortAsc(Integer deleted);

	/**
	 * 检查是否有子部门
	 */
	@Query("SELECT COUNT(d) > 0 FROM SysDept d WHERE d.parentId = :parentId AND d.deleted = 0")
	boolean hasChildren(@Param("parentId") Long parentId);

	/**
	 * 检查部门名称是否重复（同父级下）
	 */
	@Query("SELECT COUNT(d) > 0 FROM SysDept d WHERE d.deptName = :deptName AND d.parentId = :parentId AND d.deleted = 0")
	boolean existsByDeptNameAndParentId(@Param("deptName") String deptName, @Param("parentId") Long parentId);

	/**
	 * 检查部门名称是否重复（同父级下，排除指定ID）
	 */
	@Query("SELECT COUNT(d) > 0 FROM SysDept d WHERE d.deptName = :deptName AND d.parentId = :parentId AND d.id != :id AND d.deleted = 0")
	boolean existsByDeptNameAndParentIdExcludeId(@Param("deptName") String deptName, @Param("parentId") Long parentId,
			@Param("id") Long excludeId);

}
