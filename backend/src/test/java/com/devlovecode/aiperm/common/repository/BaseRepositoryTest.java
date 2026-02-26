package com.devlovecode.aiperm.common.repository;

import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Repository 测试基类")
public abstract class BaseRepositoryTest {
    // 测试基类，提供通用测试配置
}
