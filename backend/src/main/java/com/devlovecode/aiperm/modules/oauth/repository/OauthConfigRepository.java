package com.devlovecode.aiperm.modules.oauth.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.oauth.entity.SysOauthConfig;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * OAuth 平台配置 Repository
 */
@Repository
public interface OauthConfigRepository extends BaseJpaRepository<SysOauthConfig> {

    Optional<SysOauthConfig> findByPlatform(String platform);
}
