package com.devlovecode.aiperm.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.modules.system.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author devlovecode
 */
public interface ISysRoleService extends IService<SysRole> {

    /**
     * 根据角色编码查询角色
     *
     * @param roleCode 角色编码
     * @return 角色信息
     */
    SysRole getByRoleCode(String roleCode);

    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> listByUserId(Long userId);

    /**
     * 分页查询角色列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param roleName 角色名称（可选）
     * @param roleCode 角色编码（可选）
     * @param status   状态（可选）
     * @return 角色分页列表
     */
    PageResult<SysRole> page(Long pageNum, Long pageSize, String roleName, String roleCode, Integer status);

    /**
     * 创建角色
     *
     * @param role 角色信息
     * @return 是否成功
     */
    boolean create(SysRole role);

    /**
     * 更新角色
     *
     * @param role 角色信息
     * @return 是否成功
     */
    boolean update(SysRole role);

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean delete(Long roleId);

    /**
     * 分配角色菜单
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     * @return 是否成功
     */
    boolean assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 分配角色权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     * @return 是否成功
     */
    boolean assignPermissions(Long roleId, List<Long> permissionIds);
}
