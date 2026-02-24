package com.devlovecode.aiperm.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.devlovecode.aiperm.modules.system.entity.SysDept;

import java.util.List;

/**
 * 部门服务接口
 *
 * @author devlovecode
 */
public interface ISysDeptService extends IService<SysDept> {

    /**
     * 查询所有部门（树形结构）
     *
     * @return 部门树
     */
    List<SysDept> getDeptTree();

    /**
     * 根据父部门ID查询子部门列表
     *
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    List<SysDept> listByParentId(Long parentId);

    /**
     * 创建部门
     *
     * @param dept 部门信息
     * @return 是否成功
     */
    boolean create(SysDept dept);

    /**
     * 更新部门
     *
     * @param dept 部门信息
     * @return 是否成功
     */
    boolean update(SysDept dept);

    /**
     * 删除部门
     *
     * @param deptId 部门ID
     * @return 是否成功
     */
    boolean delete(Long deptId);
}
