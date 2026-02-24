package com.devlovecode.aiperm.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devlovecode.aiperm.modules.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单Mapper接口
 *
 * @author devlovecode
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 查询所有菜单（树形结构）
     *
     * @return 菜单列表
     */
    List<SysMenu> selectMenuTree();

    /**
     * 根据用户ID查询菜单列表
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenu> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询菜单列表
     *
     * @param roleId 角色ID
     * @return 菜单列表
     */
    List<SysMenu> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据父菜单ID查询子菜单列表
     *
     * @param parentId 父菜单ID
     * @return 子菜单列表
     */
    List<SysMenu> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询子菜单数量
     *
     * @param parentId 父菜单ID
     * @return 子菜单数量
     */
    Long countByParentId(@Param("parentId") Long parentId);
}
