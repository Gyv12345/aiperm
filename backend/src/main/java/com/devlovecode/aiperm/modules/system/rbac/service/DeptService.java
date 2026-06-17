package com.devlovecode.aiperm.modules.system.rbac.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.system.rbac.dto.DeptDTO;
import com.devlovecode.aiperm.modules.system.rbac.entity.SysDept;
import com.devlovecode.aiperm.modules.system.rbac.repository.DeptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeptService {

	private final DeptRepository deptRepo;

	/**
	 * 查询部门树
	 */
	public List<SysDept> getDeptTree() {
		List<SysDept> allDepts = deptRepo.findAllByDeletedOrderByParentIdAscSortAsc(0);
		return buildTree(allDepts, 0L);
	}

	/**
	 * 查询所有部门
	 */
	public List<SysDept> listAll() {
		return deptRepo.findAllByDeletedOrderByParentIdAscSortAsc(0);
	}

	/**
	 * 查询详情
	 */
	public SysDept findById(Long id) {
		return deptRepo.findById(id).orElseThrow(() -> new BusinessException("部门不存在"));
	}

	/**
	 * 查询子部门
	 */
	public List<SysDept> listByParentId(Long parentId) {
		return deptRepo.findByParentIdOrderBySortAsc(parentId);
	}

	/**
	 * 创建
	 */
	@Transactional
	public void create(DeptDTO dto) {
		// 校验部门名称是否重复
		if (deptRepo.existsByDeptNameAndParentId(dto.getDeptName(), dto.getParentId())) {
			throw new BusinessException("同级部门名称已存在");
		}

		SysDept entity = new SysDept();
		entity.setDeptName(dto.getDeptName());
		entity.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
		entity.setSort(dto.getSort() != null ? dto.getSort() : 0);
		entity.setLeader(dto.getLeader());
		entity.setPhone(dto.getPhone());
		entity.setEmail(dto.getEmail());
		entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
		entity.setRemark(dto.getRemark());
		entity.setDeptCode(dto.getDeptCode());
		entity.setCreateBy(getCurrentUsername());
		entity.setCreateTime(LocalDateTime.now());
		// 计算祖级路径：根部门 "0"；子部门 = 父级 ancestors + "," + 父级 id
		entity.setAncestors(buildAncestors(entity.getParentId()));

		deptRepo.save(entity);
	}

	/**
	 * 更新
	 */
	@Transactional
	public void update(Long id, DeptDTO dto) {
		SysDept entity = deptRepo.findById(id).orElseThrow(() -> new BusinessException("部门不存在"));

		if (isRootDept(entity) && dto.getStatus() != null && !dto.getStatus().equals(entity.getStatus())) {
			throw new BusinessException("顶级部门状态不能修改");
		}

		// 校验部门名称是否重复
		if (deptRepo.existsByDeptNameAndParentIdExcludeId(dto.getDeptName(), dto.getParentId(), id)) {
			throw new BusinessException("同级部门名称已存在");
		}

		// 校验父部门不能是自己或自己的子部门
		if (isChildOf(id, dto.getParentId())) {
			throw new BusinessException("父部门不能是自己或自己的子部门");
		}

		// 检测父部门是否变更：变更时需级联重算本部门及所有子孙的 ancestors
		boolean parentChanged = !Objects.equals(entity.getParentId(), dto.getParentId());
		String oldAncestors = entity.getAncestors();

		entity.setDeptName(dto.getDeptName());
		entity.setParentId(dto.getParentId());
		entity.setSort(dto.getSort());
		entity.setLeader(dto.getLeader());
		entity.setPhone(dto.getPhone());
		entity.setEmail(dto.getEmail());
		entity.setStatus(dto.getStatus());
		entity.setRemark(dto.getRemark());
		entity.setDeptCode(dto.getDeptCode());
		entity.setUpdateBy(getCurrentUsername());
		entity.setUpdateTime(LocalDateTime.now());
		// 重新计算本部门 ancestors
		entity.setAncestors(buildAncestors(entity.getParentId()));

		deptRepo.save(entity);

		// 父部门变更：级联重算所有子孙的 ancestors（旧前缀 → 新前缀替换）
		if (parentChanged && oldAncestors != null) {
			String newAncestors = entity.getAncestors();
			updateDescendantsAncestors(id, oldAncestors, newAncestors);
		}
	}

	/**
	 * 删除
	 */
	@Transactional
	public void delete(Long id) {
		SysDept entity = deptRepo.findById(id).orElseThrow(() -> new BusinessException("部门不存在"));
		if (isRootDept(entity)) {
			throw new BusinessException("顶级部门不能删除");
		}
		if (deptRepo.hasChildren(id)) {
			throw new BusinessException("存在子部门，不能删除");
		}
		deptRepo.softDelete(id, LocalDateTime.now());
	}

	// ========== 私有方法 ==========

	/**
	 * 根据父部门ID构建祖级路径。
	 * 根部门（parentId 为空或 0）返回 "0"；否则返回 "父级ancestors + , + 父级id"。
	 */
	private String buildAncestors(Long parentId) {
		if (parentId == null || parentId == 0L) {
			return "0";
		}
		SysDept parent = deptRepo.findById(parentId)
			.orElseThrow(() -> new BusinessException("父级部门不存在"));
		String parentAncestors = parent.getAncestors() != null ? parent.getAncestors() : "0";
		return parentAncestors + "," + parentId;
	}

	/**
	 * 移动部门后级联重算所有子孙的 ancestors。
	 * 策略：找出所有 ancestors 以 "oldAncestors,部门id" 开头的部门，把该前缀替换为
	 * "newAncestors,部门id"。这样子孙链一次性修正，无需递归。
	 */
	private void updateDescendantsAncestors(Long deptId, String oldAncestors, String newAncestors) {
		List<SysDept> allDepts = deptRepo.findAllByDeletedOrderByParentIdAscSortAsc(0);
		// 本部门作为子孙前缀的锚点
		String oldPrefix = oldAncestors + "," + deptId;
		String newPrefix = newAncestors + "," + deptId;
		for (SysDept dept : allDepts) {
			String ancestors = dept.getAncestors();
			if (ancestors != null && ancestors.startsWith(oldPrefix)) {
				dept.setAncestors(newPrefix + ancestors.substring(oldPrefix.length()));
				dept.setUpdateTime(LocalDateTime.now());
				deptRepo.save(dept);
			}
		}
	}

	/**
	 * 构建部门树
	 */
	private List<SysDept> buildTree(List<SysDept> allDepts, Long parentId) {
		Map<Long, List<SysDept>> groupedByParent = allDepts.stream()
			.collect(Collectors.groupingBy(SysDept::getParentId));

		List<SysDept> roots = groupedByParent.getOrDefault(parentId, new ArrayList<>());
		for (SysDept root : roots) {
			root.setChildren(buildTreeRecursive(groupedByParent, root.getId()));
		}
		return roots;
	}

	private List<SysDept> buildTreeRecursive(Map<Long, List<SysDept>> groupedByParent, Long parentId) {
		List<SysDept> children = groupedByParent.getOrDefault(parentId, new ArrayList<>());
		for (SysDept child : children) {
			child.setChildren(buildTreeRecursive(groupedByParent, child.getId()));
		}
		return children;
	}

	/**
	 * 检查 targetId 是否是 id 的子部门（包括自己）
	 */
	private boolean isChildOf(Long id, Long targetParentId) {
		if (id.equals(targetParentId)) {
			return true;
		}
		// 收集所有子部门ID
		List<SysDept> allDepts = deptRepo.findAllByDeletedOrderByParentIdAscSortAsc(0);
		return isChildRecursive(allDepts, id, targetParentId);
	}

	private boolean isChildRecursive(List<SysDept> allDepts, Long parentId, Long targetId) {
		for (SysDept dept : allDepts) {
			if (dept.getParentId().equals(parentId)) {
				if (dept.getId().equals(targetId)) {
					return true;
				}
				if (isChildRecursive(allDepts, dept.getId(), targetId)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isRootDept(SysDept dept) {
		return dept.getParentId() != null && dept.getParentId() == 0L;
	}

	private String getCurrentUsername() {
		try {
			return StpUtil.getLoginIdAsString();
		}
		catch (Exception e) {
			return "system";
		}
	}

}
