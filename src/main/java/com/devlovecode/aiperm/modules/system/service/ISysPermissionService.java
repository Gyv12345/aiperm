package com.devlovecode.aiperm.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.modules.system.entity.SysPermission;

import java.util.List;

/**
 * 权限服务接口
 *
 * @author devlovecode
 */
public interface ISysPermissionService extends IService<SysPermission> {

    /**
     * 根据权限编码查询权限
     *
     * @param permissionCode 权限编码
     * @return 权限信息
     */
    SysPermission getByPermissionCode(String permissionCode);

    /**
     * 根据角色ID查询权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<SysPermission> listByRoleId(Long roleId);

    /**
     * 根据用户ID查询权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<SysPermission> listByUserId(Long userId);

    /**
     * 根据菜单ID查询权限列表
     *
     * @param menuId 菜单ID
     * @return 权限列表
     */
    List<SysPermission> listByMenuId(Long menuId);

    /**
     * 分页查询权限列表
     *
     * @param pageNum           页码
     * @param pageSize          每页条数
     * @param permissionName    权限名称（可选）
     * @param permissionCode    权限编码（可选）
     * @param permissionType    权限类型（可选）
     * @param menuId            菜单ID（可选）
     * @param status            状态（可选）
     * @return 权限分页列表
     */
    PageResult<SysPermission> page(Long pageNum, Long pageSize, String permissionName, String permissionCode,
                                    Integer permissionType, Long menuId, Integer status);

    /**
     * 创建权限
     *
     * @param permission 权限信息
     * @return 是否成功
     */
    boolean create(SysPermission permission);

    /**
     * 更新权限
     *
     * @param permission 权限信息
     * @return 是否成功
     */
    boolean update(SysPermission permission);

    /**
     * 删除权限
     *
     * @param permissionId 权限ID
     * @return 是否成功
     */
    boolean delete(Long permissionId);
}
