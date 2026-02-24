package com.devlovecode.aiperm.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devlovecode.aiperm.modules.system.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门Mapper接口
 *
 * @author devlovecode
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    /**
     * 查询所有部门（树形结构）
     *
     * @return 部门列表
     */
    List<SysDept> selectDeptTree();

    /**
     * 根据父部门ID查询子部门列表
     *
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    List<SysDept> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询子部门数量
     *
     * @param parentId 父部门ID
     * @return 子部门数量
     */
    Long countByParentId(@Param("parentId") Long parentId);

    /**
     * 根据用户ID查询部门
     *
     * @param userId 用户ID
     * @return 部门信息
     */
    SysDept selectByUserId(@Param("userId") Long userId);
}
