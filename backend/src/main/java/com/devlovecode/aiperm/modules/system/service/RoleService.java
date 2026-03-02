package com.devlovecode.aiperm.modules.system.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.system.dto.RoleDTO;
import com.devlovecode.aiperm.modules.system.entity.SysRole;
import com.devlovecode.aiperm.modules.system.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepo;

    /**
     * 分页查询
     */
    public PageResult<SysRole> queryPage(RoleDTO dto) {
        return roleRepo.queryPage(dto.getRoleName(), dto.getRoleCode(), dto.getStatus(), dto.getPage(), dto.getPageSize());
    }

    /**
     * 查询所有
     */
    public List<SysRole> listAll() {
        return roleRepo.findAll();
    }

    /**
     * 查询详情
     */
    public SysRole findById(Long id) {
        return roleRepo.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));
    }

    /**
     * 获取角色的菜单ID列表
     */
    public List<Long> getMenuIds(Long roleId) {
        return roleRepo.getMenuIdsByRoleId(roleId);
    }

    /**
     * 创建
     */
    @Transactional
    public void create(RoleDTO dto) {
        if (roleRepo.existsByRoleCode(dto.getRoleCode())) {
            throw new BusinessException("角色编码已存在");
        }

        SysRole entity = new SysRole();
        entity.setRoleName(dto.getRoleName());
        entity.setRoleCode(dto.getRoleCode());
        entity.setSort(dto.getSort() != null ? dto.getSort() : 0);
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        entity.setDataScope(dto.getDataScope() != null ? dto.getDataScope() : 1);
        entity.setRemark(dto.getRemark());
        entity.setCreateBy(getCurrentUsername());

        roleRepo.insert(entity);
    }

    /**
     * 更新
     */
    @Transactional
    public void update(Long id, RoleDTO dto) {
        SysRole entity = roleRepo.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));

        if (roleRepo.isBuiltin(id)) {
            throw new BusinessException("内置角色不能修改");
        }

        if (roleRepo.existsByRoleCodeExcludeId(dto.getRoleCode(), id)) {
            throw new BusinessException("角色编码已存在");
        }

        entity.setRoleName(dto.getRoleName());
        entity.setRoleCode(dto.getRoleCode());
        entity.setSort(dto.getSort());
        entity.setStatus(dto.getStatus());
        if (dto.getDataScope() != null) {
            entity.setDataScope(dto.getDataScope());
        }
        entity.setRemark(dto.getRemark());
        entity.setUpdateBy(getCurrentUsername());

        roleRepo.update(entity);
    }

    /**
     * 删除
     */
    @Transactional
    public void delete(Long id) {
        if (!roleRepo.existsById(id)) {
            throw new BusinessException("角色不存在");
        }
        if (roleRepo.isBuiltin(id)) {
            throw new BusinessException("内置角色不能删除");
        }
        if (roleRepo.isUsedByUser(id)) {
            throw new BusinessException("角色已分配给用户，不能删除");
        }
        // 删除角色菜单关联
        roleRepo.deleteRoleMenus(id);
        // 删除角色
        roleRepo.deleteById(id);
    }

    /**
     * 分配菜单
     */
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        if (!roleRepo.existsById(roleId)) {
            throw new BusinessException("角色不存在");
        }

        // 先删除旧的关联
        roleRepo.deleteRoleMenus(roleId);

        // 添加新的关联
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                roleRepo.insertRoleMenu(roleId, menuId);
            }
        }
    }

    private String getCurrentUsername() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            return "system";
        }
    }
}
