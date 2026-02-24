package com.devlovecode.aiperm.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.modules.system.entity.SysUser;

/**
 * 用户服务接口
 *
 * @author devlovecode
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getByUsername(String username);

    /**
     * 分页查询用户列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param username 用户名（可选）
     * @param phone    手机号（可选）
     * @param deptId   部门ID（可选）
     * @param status   状态（可选）
     * @return 用户分页列表
     */
    PageResult<SysUser> page(Long pageNum, Long pageSize, String username, String phone, Long deptId, Integer status);

    /**
     * 创建用户
     *
     * @param user 用户信息
     * @return 是否成功
     */
    boolean create(SysUser user);

    /**
     * 更新用户
     *
     * @param user 用户信息
     * @return 是否成功
     */
    boolean update(SysUser user);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean delete(Long userId);

    /**
     * 重置用户密码
     *
     * @param userId          用户ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetPassword(Long userId, String newPassword);

    /**
     * 修改用户状态
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 是否成功
     */
    boolean changeStatus(Long userId, Integer status);

    /**
     * 更新最后登录信息
     *
     * @param userId 用户ID
     * @param ip     登录IP
     */
    void updateLastLoginInfo(Long userId, String ip);
}
