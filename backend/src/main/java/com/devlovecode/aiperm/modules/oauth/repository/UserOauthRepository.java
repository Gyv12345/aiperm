package com.devlovecode.aiperm.modules.oauth.repository;

import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.modules.oauth.entity.SysUserOauth;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户第三方账号绑定 Repository
 */
@Repository
public class UserOauthRepository extends BaseRepository<SysUserOauth> {

    public UserOauthRepository(JdbcClient db) {
        super(db, "sys_user_oauth", SysUserOauth.class);
    }

    /** 根据平台和openId查找绑定关系 */
    public Optional<SysUserOauth> findByPlatformAndOpenId(String platform, String openId) {
        String sql = "SELECT * FROM sys_user_oauth WHERE platform = :platform AND open_id = :openId AND deleted = 0";
        return db.sql(sql)
                .param("platform", platform)
                .param("openId", openId)
                .query(SysUserOauth.class)
                .optional();
    }

    /** 查询用户已绑定的所有第三方账号 */
    public List<SysUserOauth> findByUserId(Long userId) {
        String sql = "SELECT * FROM sys_user_oauth WHERE user_id = :userId AND status = 1 AND deleted = 0";
        return db.sql(sql).param("userId", userId).query(SysUserOauth.class).list();
    }

    /** 查询用户对某平台的绑定 */
    public Optional<SysUserOauth> findByUserIdAndPlatform(Long userId, String platform) {
        String sql = "SELECT * FROM sys_user_oauth WHERE user_id = :userId AND platform = :platform AND deleted = 0";
        return db.sql(sql).param("userId", userId).param("platform", platform).query(SysUserOauth.class).optional();
    }

    public void insert(SysUserOauth oauth) {
        String sql = """
            INSERT INTO sys_user_oauth (user_id, platform, open_id, union_id, nickname, avatar,
                last_login_time, status, deleted, version, create_time, create_by)
            VALUES (:userId, :platform, :openId, :unionId, :nickname, :avatar,
                :lastLoginTime, 1, 0, 0, NOW(), :createBy)
            """;
        db.sql(sql)
                .param("userId", oauth.getUserId())
                .param("platform", oauth.getPlatform())
                .param("openId", oauth.getOpenId())
                .param("unionId", oauth.getUnionId())
                .param("nickname", oauth.getNickname())
                .param("avatar", oauth.getAvatar())
                .param("lastLoginTime", oauth.getLastLoginTime())
                .param("createBy", oauth.getCreateBy())
                .update();
    }

    public int updateLastLoginTime(Long id) {
        String sql = "UPDATE sys_user_oauth SET last_login_time = NOW() WHERE id = :id";
        return db.sql(sql).param("id", id).update();
    }

    public int unbind(Long userId, String platform) {
        String sql = "UPDATE sys_user_oauth SET status = 0, update_time = NOW() WHERE user_id = :userId AND platform = :platform";
        return db.sql(sql).param("userId", userId).param("platform", platform).update();
    }
}
