package com.devlovecode.aiperm.modules.system.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.system.dto.MenuDTO;
import com.devlovecode.aiperm.modules.system.entity.SysMenu;
import com.devlovecode.aiperm.modules.system.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepo;

    /**
     * 查询菜单树
     */
    public List<SysMenu> getMenuTree() {
        List<SysMenu> allMenus = menuRepo.findAllByDeletedOrderByParentIdAscSortAsc(0);
        return buildTree(allMenus, 0L);
    }

    /**
     * 查询所有菜单
     */
    public List<SysMenu> listAll() {
        return menuRepo.findAllByDeletedOrderByParentIdAscSortAsc(0);
    }

    /**
     * 查询详情
     */
    public SysMenu findById(Long id) {
        return menuRepo.findById(id)
                .orElseThrow(() -> new BusinessException("菜单不存在"));
    }

    /**
     * 查询子菜单
     */
    public List<SysMenu> listByParentId(Long parentId) {
        return menuRepo.findByParentIdOrderBySortAsc(parentId);
    }

    /**
     * 创建
     */
    @Transactional
    public void create(MenuDTO dto) {
        // 校验菜单名称是否重复
        if (menuRepo.existsByMenuNameAndParentId(dto.getMenuName(), dto.getParentId())) {
            throw new BusinessException("同级菜单名称已存在");
        }

        SysMenu entity = new SysMenu();
        entity.setMenuName(dto.getMenuName());
        entity.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        entity.setMenuType(dto.getMenuType());
        entity.setSort(dto.getSort() != null ? dto.getSort() : 0);
        entity.setPath(dto.getPath());
        entity.setComponent(dto.getComponent());
        entity.setPerms(dto.getPerms());
        entity.setIcon(dto.getIcon());
        entity.setIsExternal(dto.getIsExternal() != null ? dto.getIsExternal() : 0);
        entity.setIsCache(dto.getIsCache() != null ? dto.getIsCache() : 0);
        entity.setVisible(dto.getVisible() != null ? dto.getVisible() : 1);
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        entity.setPermission(dto.getPermission());
        entity.setRemark(dto.getRemark());
        entity.setCreateBy(getCurrentUsername());
        entity.setCreateTime(LocalDateTime.now());

        menuRepo.save(entity);
    }

    /**
     * 更新
     */
    @Transactional
    public void update(Long id, MenuDTO dto) {
        SysMenu entity = menuRepo.findById(id)
                .orElseThrow(() -> new BusinessException("菜单不存在"));

        // 校验菜单名称是否重复
        if (menuRepo.existsByMenuNameAndParentIdExcludeId(dto.getMenuName(), dto.getParentId(), id)) {
            throw new BusinessException("同级菜单名称已存在");
        }

        // 校验父菜单不能是自己或自己的子菜单
        if (isChildOf(id, dto.getParentId())) {
            throw new BusinessException("父菜单不能是自己或自己的子菜单");
        }

        entity.setMenuName(dto.getMenuName());
        entity.setParentId(dto.getParentId());
        entity.setMenuType(dto.getMenuType());
        entity.setSort(dto.getSort());
        entity.setPath(dto.getPath());
        entity.setComponent(dto.getComponent());
        entity.setPerms(dto.getPerms());
        entity.setIcon(dto.getIcon());
        entity.setIsExternal(dto.getIsExternal());
        entity.setIsCache(dto.getIsCache());
        entity.setVisible(dto.getVisible());
        entity.setStatus(dto.getStatus());
        entity.setPermission(dto.getPermission());
        entity.setRemark(dto.getRemark());
        entity.setUpdateBy(getCurrentUsername());
        entity.setUpdateTime(LocalDateTime.now());

        menuRepo.save(entity);
    }

    /**
     * 删除
     */
    @Transactional
    public void delete(Long id) {
        if (!menuRepo.existsById(id)) {
            throw new BusinessException("菜单不存在");
        }
        if (menuRepo.hasChildren(id)) {
            throw new BusinessException("存在子菜单，不能删除");
        }
        menuRepo.softDelete(id, LocalDateTime.now());
    }

    // ========== 私有方法 ==========

    /**
     * 构建菜单树
     */
    private List<SysMenu> buildTree(List<SysMenu> allMenus, Long parentId) {
        Map<Long, List<SysMenu>> groupedByParent = allMenus.stream()
                .collect(Collectors.groupingBy(SysMenu::getParentId));

        List<SysMenu> roots = groupedByParent.getOrDefault(parentId, new ArrayList<>());
        for (SysMenu root : roots) {
            root.setChildren(buildTreeRecursive(groupedByParent, root.getId()));
        }
        return roots;
    }

    private List<SysMenu> buildTreeRecursive(Map<Long, List<SysMenu>> groupedByParent, Long parentId) {
        List<SysMenu> children = groupedByParent.getOrDefault(parentId, new ArrayList<>());
        for (SysMenu child : children) {
            child.setChildren(buildTreeRecursive(groupedByParent, child.getId()));
        }
        return children;
    }

    /**
     * 检查 targetId 是否是 id 的子菜单（包括自己）
     */
    private boolean isChildOf(Long id, Long targetParentId) {
        if (id.equals(targetParentId)) {
            return true;
        }
        List<SysMenu> allMenus = menuRepo.findAllByDeletedOrderByParentIdAscSortAsc(0);
        return isChildRecursive(allMenus, id, targetParentId);
    }

    private boolean isChildRecursive(List<SysMenu> allMenus, Long parentId, Long targetId) {
        for (SysMenu menu : allMenus) {
            if (menu.getParentId().equals(parentId)) {
                if (menu.getId().equals(targetId)) {
                    return true;
                }
                if (isChildRecursive(allMenus, menu.getId(), targetId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getCurrentUsername() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            return "system";
        }
    }
}
