package com.devlovecode.aiperm.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.devlovecode.aiperm.modules.system.entity.SysMenu;

import java.util.List;

/**
 * 菜单服务接口
 *
 * @author devlovecode
 */
public interface ISysMenuService extends IService<SysMenu> {

    /**
     * 查询所有菜单（树形结构）
     *
     * @return 菜单树
     */
    List<SysMenu> getMenuTree();

    /**
     * 根据用户ID查询菜单列表
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenu> listByUserId(Long userId);

    /**
     * 根据角色ID查询菜单列表
     *
     * @param roleId 角色ID
     * @return 菜单列表
     */
    List<SysMenu> listByRoleId(Long roleId);

    /**
     * 根据父菜单ID查询子菜单列表
     *
     * @param parentId 父菜单ID
     * @return 子菜单列表
     */
    List<SysMenu> listByParentId(Long parentId);

    /**
     * 创建菜单
     *
     * @param menu 菜单信息
     * @return 是否成功
     */
    boolean create(SysMenu menu);

    /**
     * 更新菜单
     *
     * @param menu 菜单信息
     * @return 是否成功
     */
    boolean update(SysMenu menu);

    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     * @return 是否成功
     */
    boolean delete(Long menuId);
}
