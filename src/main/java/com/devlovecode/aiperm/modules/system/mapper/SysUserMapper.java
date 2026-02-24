package com.devlovecode.aiperm.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devlovecode.aiperm.modules.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户Mapper接口
 *
 * @author devlovecode
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 根据部门ID查询用户列表
     *
     * @param deptId 部门ID
     * @return 用户列表
     */
    List<SysUser> selectByDeptId(@Param("deptId") Long deptId);

    /**
     * 根据角色ID查询用户列表
     *
     * @param roleId 角色ID
     * @return 用户列表
     */
    List<SysUser> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 分页查询用户列表（带部门信息）
     *
     * @param page     分页参数
     * @param username 用户名（可选）
     * @param phone    手机号（可选）
     * @param deptId   部门ID（可选）
     * @param status   状态（可选）
     * @return 用户分页列表
     */
    IPage<SysUser> selectUserPage(Page<SysUser> page,
                                   @Param("username") String username,
                                   @Param("phone") String phone,
                                   @Param("deptId") Long deptId,
                                   @Param("status") Integer status);
}
