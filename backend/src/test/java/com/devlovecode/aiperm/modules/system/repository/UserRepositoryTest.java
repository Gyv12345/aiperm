package com.devlovecode.aiperm.modules.system.repository;

import com.devlovecode.aiperm.common.repository.BaseRepositoryTest;
import com.devlovecode.aiperm.modules.system.entity.SysUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("用户 Repository 测试")
class UserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("根据用户名查询用户")
    void testFindByUsername() {
        // 测试查询存在的用户
        Optional<SysUser> user = userRepository.findByUsername("admin");
        assertTrue(user.isPresent(), "应该找到 admin 用户");
        assertEquals("admin", user.get().getUsername());
    }

    @Test
    @DisplayName("根据用户名查询不存在的用户")
    void testFindByUsernameNotFound() {
        Optional<SysUser> user = userRepository.findByUsername("nonexistent_user");
        assertFalse(user.isPresent(), "不应该找到不存在的用户");
    }

    @Test
    @DisplayName("根据 ID 查询用户")
    void testFindById() {
        Optional<SysUser> user = userRepository.findById(1L);
        assertTrue(user.isPresent(), "应该找到 ID=1 的用户");
    }
}
