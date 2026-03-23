package com.devlovecode.aiperm.modules.captcha.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.modules.captcha.entity.SysCaptchaConfig;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CaptchaConfigRepository extends BaseJpaRepository<SysCaptchaConfig> {

    Optional<SysCaptchaConfig> findByType(String type);
}
