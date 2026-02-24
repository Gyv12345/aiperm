package com.devlovecode.aiperm.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devlovecode.aiperm.modules.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色Mapper接口
 *
 * @author devlovecode
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据角色编码查询角色
     *
     * @param roleCode 角色编码
     * @return 角色信息
     */
    SysRole selectByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> selectByUserId(@Param("userId") Long userId);

    /**
     * 分页查询角色列表
     *
     * @param page     分页参数
     * @param roleName 角色名称（可选）
     * @param roleCode 角色编码（可选）
     * @param status   状态（可选）
     * @return 角色分页列表
     */
    IPage<SysRole> selectRolePage(Page<SysRole> page,
                                   @Param("roleName") String roleName,
                                   @Param("roleCode") String roleCode,
                                   @Param("status") Integer status);
}
