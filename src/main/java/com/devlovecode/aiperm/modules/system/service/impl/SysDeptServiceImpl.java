package com.devlovecode.aiperm.modules.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devlovecode.aiperm.common.enums.ErrorCode;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.system.entity.SysDept;
import com.devlovecode.aiperm.modules.system.mapper.SysDeptMapper;
import com.devlovecode.aiperm.modules.system.service.ISysDeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门服务实现类
 *
 * @author devlovecode
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    private final SysDeptMapper sysDeptMapper;

    @Override
    public List<SysDept> getDeptTree() {
        List<SysDept> allDepts = sysDeptMapper.selectDeptTree();
        return buildDeptTree(allDepts, 0L);
    }

    @Override
    public List<SysDept> listByParentId(Long parentId) {
        return sysDeptMapper.selectByParentId(parentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(SysDept dept) {
        return save(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(SysDept dept) {
        SysDept existDept = getById(dept.getId());
        if (existDept == null) {
            throw new BusinessException(ErrorCode.DEPT_NOT_EXISTS);
        }

        // 检查是否将父部门设置为自己或自己的子部门
        if (dept.getId().equals(dept.getParentId())) {
            throw new BusinessException("父部门不能是自己");
        }

        return updateById(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long deptId) {
        SysDept dept = getById(deptId);
        if (dept == null) {
            throw new BusinessException(ErrorCode.DEPT_NOT_EXISTS);
        }

        // 检查是否有子部门
        Long childCount = sysDeptMapper.countByParentId(deptId);
        if (childCount > 0) {
            throw new BusinessException("存在子部门，不允许删除");
        }

        return removeById(deptId);
    }

    /**
     * 构建部门树
     *
     * @param depts    部门列表
     * @param parentId 父部门ID
     * @return 部门树
     */
    private List<SysDept> buildDeptTree(List<SysDept> depts, Long parentId) {
        if (CollUtil.isEmpty(depts)) {
            return new ArrayList<>();
        }

        List<SysDept> result = new ArrayList<>();
        for (SysDept dept : depts) {
            if (dept.getParentId().equals(parentId)) {
                List<SysDept> children = buildDeptTree(depts, dept.getId());
                dept.setChildren(children);
                result.add(dept);
            }
        }

        return result.stream()
                .sorted((a, b) -> {
                    if (a.getSort() == null) return 1;
                    if (b.getSort() == null) return -1;
                    return a.getSort().compareTo(b.getSort());
                })
                .collect(Collectors.toList());
    }
}
