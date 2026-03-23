package com.devlovecode.aiperm.modules.mfa.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.mfa.entity.SysUserMfa;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMfaRepository extends BaseJpaRepository<SysUserMfa> {

    Optional<SysUserMfa> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE SysUserMfa m SET m.status = :status, m.updateTime = :updateTime WHERE m.userId = :userId")
    int updateStatus(@Param("userId") Long userId, @Param("status") Integer status, @Param("updateTime") java.time.LocalDateTime updateTime);
}
