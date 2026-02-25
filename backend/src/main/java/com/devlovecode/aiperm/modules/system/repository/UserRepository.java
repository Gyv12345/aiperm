package com.devlovecode.aiperm.modules.system.repository;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.common.repository.SqlBuilder;
import com.devlovecode.aiperm.modules.system.entity.SysUser;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository extends BaseRepository<SysUser> {

    public UserRepository(JdbcClient db) {
        super(db, "sys_user", SysUser.class);
    }

    /**
     * 插入用户
     */
    public void insert(SysUser entity) {
        String sql = """
            INSERT INTO sys_user (username, password, nickname, real_name, email, phone, gender, avatar,
                dept_id, post_id, is_admin, status, remark, deleted, version, create_time, create_by)
            VALUES (:username, :password, :nickname, :realName, :email, :phone, :gender, :avatar,
                :deptId, :postId, :isAdmin, :status, :remark, 0, 0, :createTime, :createBy)
            """;
        db.sql(sql)
                .param("username", entity.getUsername())
                .param("password", entity.getPassword())
                .param("nickname", entity.getNickname())
                .param("realName", entity.getRealName())
                .param("email", entity.getEmail())
                .param("phone", entity.getPhone())
                .param("gender", entity.getGender())
                .param("avatar", entity.getAvatar())
                .param("deptId", entity.getDeptId())
                .param("postId", entity.getPostId())
                .param("isAdmin", entity.getIsAdmin())
                .param("status", entity.getStatus())
                .param("remark", entity.getRemark())
                .param("createTime", LocalDateTime.now())
                .param("createBy", entity.getCreateBy())
                .update();
    }

    /**
     * 更新用户
     */
    public int update(SysUser entity) {
        String sql = """
            UPDATE sys_user
            SET nickname = :nickname, real_name = :realName, email = :email, phone = :phone,
                gender = :gender, avatar = :avatar, dept_id = :deptId, post_id = :postId,
                is_admin = :isAdmin, status = :status, remark = :remark,
                update_time = :updateTime, update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("nickname", entity.getNickname())
                .param("realName", entity.getRealName())
                .param("email", entity.getEmail())
                .param("phone", entity.getPhone())
                .param("gender", entity.getGender())
                .param("avatar", entity.getAvatar())
                .param("deptId", entity.getDeptId())
                .param("postId", entity.getPostId())
                .param("isAdmin", entity.getIsAdmin())
                .param("status", entity.getStatus())
                .param("remark", entity.getRemark())
                .param("updateTime", LocalDateTime.now())
                .param("updateBy", entity.getUpdateBy())
                .param("id", entity.getId())
                .update();
    }

    /**
     * 根据用户名查询
     */
    public Optional<SysUser> findByUsername(String username) {
        String sql = "SELECT * FROM sys_user WHERE username = :username AND deleted = 0";
        return db.sql(sql).param("username", username).query(SysUser.class).optional();
    }

    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM sys_user WHERE username = :username AND deleted = 0";
        Integer count = db.sql(sql).param("username", username).query(Integer.class).single();
        return count != null && count > 0;
    }

    /**
     * 检查用户名是否存在（排除指定ID）
     */
    public boolean existsByUsernameExcludeId(String username, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM sys_user WHERE username = :username AND id != :id AND deleted = 0";
        Integer count = db.sql(sql)
                .param("username", username)
                .param("id", excludeId)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    /**
     * 分页查询
     */
    public PageResult<SysUser> queryPage(String username, String phone, Long deptId, Integer status, int pageNum, int pageSize) {
        SqlBuilder sb = new SqlBuilder();
        sb.likeIf(username != null && !username.isBlank(), "username", username)
          .likeIf(phone != null && !phone.isBlank(), "phone", phone)
          .whereIf(deptId != null, "dept_id = ?", deptId)
          .whereIf(status != null, "status = ?", status);

        return queryPage(sb.getWhereClause(), sb.getParams(), pageNum, pageSize);
    }

    /**
     * 更新密码
     */
    public int updatePassword(Long id, String newPassword) {
        String sql = "UPDATE sys_user SET password = :password, update_time = :updateTime WHERE id = :id AND deleted = 0";
        return db.sql(sql)
                .param("password", newPassword)
                .param("updateTime", LocalDateTime.now())
                .param("id", id)
                .update();
    }

    /**
     * 更新状态
     */
    public int updateStatus(Long id, Integer status) {
        String sql = "UPDATE sys_user SET status = :status, update_time = :updateTime WHERE id = :id AND deleted = 0";
        return db.sql(sql)
                .param("status", status)
                .param("updateTime", LocalDateTime.now())
                .param("id", id)
                .update();
    }

    /**
     * 更新最后登录信息
     */
    public int updateLoginInfo(Long id, String loginIp) {
        String sql = "UPDATE sys_user SET last_login_ip = :loginIp, last_login_time = :loginTime WHERE id = :id AND deleted = 0";
        return db.sql(sql)
                .param("loginIp", loginIp)
                .param("loginTime", LocalDateTime.now())
                .param("id", id)
                .update();
    }

    /**
     * 检查是否为管理员
     */
    public boolean isAdmin(Long id) {
        String sql = "SELECT username FROM sys_user WHERE id = :id AND deleted = 0";
        String username = db.sql(sql).param("id", id).query(String.class).single();
        return "admin".equals(username);
    }
}
