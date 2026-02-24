package com.devlovecode.aiperm.modules.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devlovecode.aiperm.common.enums.ErrorCode;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.system.entity.SysMenu;
import com.devlovecode.aiperm.modules.system.mapper.SysMenuMapper;
import com.devlovecode.aiperm.modules.system.service.ISysMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 *
 * @author devlovecode
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    private final SysMenuMapper sysMenuMapper;

    @Override
    public List<SysMenu> getMenuTree() {
        List<SysMenu> allMenus = sysMenuMapper.selectMenuTree();
        return buildMenuTree(allMenus, 0L);
    }

    @Override
    public List<SysMenu> listByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<SysMenu> menus = sysMenuMapper.selectByUserId(userId);
        return buildMenuTree(menus, 0L);
    }

    @Override
    public List<SysMenu> listByRoleId(Long roleId) {
        if (roleId == null) {
            return List.of();
        }
        return sysMenuMapper.selectByRoleId(roleId);
    }

    @Override
    public List<SysMenu> listByParentId(Long parentId) {
        return sysMenuMapper.selectByParentId(parentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(SysMenu menu) {
        return save(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(SysMenu menu) {
        SysMenu existMenu = getById(menu.getId());
        if (existMenu == null) {
            throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
        }

        // 检查是否将父菜单设置为自己或自己的子菜单
        if (menu.getId().equals(menu.getParentId())) {
            throw new BusinessException("父菜单不能是自己");
        }

        return updateById(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long menuId) {
        SysMenu menu = getById(menuId);
        if (menu == null) {
            throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
        }

        // 检查是否有子菜单
        Long childCount = sysMenuMapper.countByParentId(menuId);
        if (childCount > 0) {
            throw new BusinessException("存在子菜单，不允许删除");
        }

        return removeById(menuId);
    }

    /**
     * 构建菜单树
     *
     * @param menus    菜单列表
     * @param parentId 父菜单ID
     * @return 菜单树
     */
    private List<SysMenu> buildMenuTree(List<SysMenu> menus, Long parentId) {
        if (CollUtil.isEmpty(menus)) {
            return new ArrayList<>();
        }

        List<SysMenu> result = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (menu.getParentId().equals(parentId)) {
                List<SysMenu> children = buildMenuTree(menus, menu.getId());
                menu.setChildren(children);
                result.add(menu);
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
