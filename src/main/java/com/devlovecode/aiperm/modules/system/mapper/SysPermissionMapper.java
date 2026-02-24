package com.devlovecode.aiperm.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devlovecode.aiperm.modules.system.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限Mapper接口
 *
 * @author devlovecode
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 根据权限编码查询权限
     *
     * @param permissionCode 权限编码
     * @return 权限信息
     */
    SysPermission selectByPermissionCode(@Param("permissionCode") String permissionCode);

    /**
     * 根据角色ID查询权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<SysPermission> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查询权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<SysPermission> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据菜单ID查询权限列表
     *
     * @param menuId 菜单ID
     * @return 权限列表
     */
    List<SysPermission> selectByMenuId(@Param("menuId") Long menuId);

    /**
     * 分页查询权限列表
     *
     * @param page           分页参数
     * @param permissionName 权限名称（可选）
     * @param permissionCode 权限编码（可选）
     * @param permissionType 权限类型（可选）
     * @param menuId         菜单ID（可选）
     * @param status         状态（可选）
     * @return 权限分页列表
     */
    IPage<SysPermission> selectPermissionPage(Page<SysPermission> page,
                                               @Param("permissionName") String permissionName,
                                               @Param("permissionCode") String permissionCode,
                                               @Param("permissionType") Integer permissionType,
                                               @Param("menuId") Long menuId,
                                               @Param("status") Integer status);
}
