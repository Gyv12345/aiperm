# aiperm 项目改进实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 根据 2026-02-26 项目评估报告，修复安全漏洞、建立质量保障体系、提升项目健壮性

**Architecture:** 分三个阶段执行：Phase 1 安全加固（立即）→ Phase 2 质量保障（短期）→ Phase 3 功能完善（长期）

**Tech Stack:** Spring Boot 3.5 + Java 21 + Sa-Token + Vue 3 + GitHub Actions

---

## Phase 1: 安全加固（立即修复）

### Task 1: 启用验证码功能

**Files:**
- Modify: `backend/src/main/java/com/devlovecode/aiperm/modules/auth/service/AuthService.java:75-76`

**Step 1: 取消验证码校验注释**

```java
// 修改前（第 75-76 行）
// 验证码校验（暂时跳过，方便开发调试）
// validateCaptcha(request.getCaptchaKey(), request.getCaptcha());

// 修改后
// 验证码校验
validateCaptcha(request.getCaptchaKey(), request.getCaptcha());
```

**Step 2: 验证修改**

Run: `cd backend && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

**Step 3: 提交**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/auth/service/AuthService.java
git commit -m "fix(security): enable captcha validation for login security"
```

---

### Task 2: 修复 SQL 注入风险

**Files:**
- Modify: `backend/src/main/java/com/devlovecode/aiperm/common/repository/BaseRepository.java:61-70`

**Step 1: 修复 deleteByIds 方法的 SQL 注入风险**

```java
// 修改前（第 61-70 行）
public int deleteByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
        return 0;
    }
    String placeholders = String.join(",", ids.stream().map(String::valueOf).toList());
    String sql = "UPDATE " + tableName + " SET deleted = 1, update_time = :updateTime WHERE id IN (" + placeholders + ")";
    return db.sql(sql)
            .param("updateTime", LocalDateTime.now())
            .update();
}

// 修改后
public int deleteByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
        return 0;
    }
    // 使用 NamedParameterJdbcClient 的参数化查询防止 SQL 注入
    String placeholders = ids.stream().map(id -> "?").collect(java.util.stream.Collectors.joining(","));
    String sql = "UPDATE " + tableName + " SET deleted = 1, update_time = ? WHERE id IN (" + placeholders + ")";

    // 构建参数列表：先添加 updateTime，再添加所有 id
    List<Object> params = new java.util.ArrayList<>();
    params.add(LocalDateTime.now());
    params.addAll(ids);

    return db.sql(sql)
            .params(params)
            .update();
}
```

**Step 2: 验证修改**

Run: `cd backend && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

**Step 3: 提交**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/common/repository/BaseRepository.java
git commit -m "fix(security): prevent SQL injection in deleteByIds using parameterized query"
```

---

### Task 3: 敏感配置环境变量化

**Files:**
- Modify: `backend/src/main/resources/application.yaml`
- Create: `backend/src/main/resources/application-dev.yaml`
- Create: `backend/src/main/resources/application-prod.yaml`

**Step 1: 修改 application.yaml 使用环境变量**

```yaml
# 数据源配置 - 使用环境变量
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: ${DB_URL:jdbc:mysql://localhost:3306/aiperm?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}

  # Redis配置
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: ${REDIS_DATABASE:0}
      timeout: 10s

# Sa-Token配置
sa-token:
  jwt-secret-key: ${SA_TOKEN_JWT_SECRET:change-this-to-random-string}
```

**Step 2: 创建 .env.example 文件**

```bash
# 数据库配置
DB_URL=jdbc:mysql://localhost:3306/aiperm?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=your_password_here

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_DATABASE=0

# Sa-Token配置
SA_TOKEN_JWT_SECRET=your_random_secret_key_here
```

**Step 3: 更新 .gitignore**

```bash
# 在 .gitignore 中添加
.env
.env.local
application-prod.yaml
```

**Step 4: 验证修改**

Run: `cd backend && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

**Step 5: 提交**

```bash
git add backend/src/main/resources/application.yaml
git add backend/.env.example
git add .gitignore
git commit -m "fix(security): use environment variables for sensitive configuration"
```

---

### Task 4: 建立 CI/CD 流程

**Files:**
- Create: `.github/workflows/ci.yml`

**Step 1: 创建 GitHub Actions CI 配置**

```yaml
name: CI

on:
  push:
    branches: [ main, master, develop ]
  pull_request:
    branches: [ main, master, develop ]

jobs:
  backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Build Backend
        working-directory: backend
        run: ./gradlew build -x test

      - name: Run Backend Tests
        working-directory: backend
        run: ./gradlew test

  frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'

      - name: Setup pnpm
        uses: pnpm/action-setup@v4
        with:
          version: 9

      - name: Install Frontend Dependencies
        working-directory: frontend
        run: pnpm install

      - name: Type Check
        working-directory: frontend
        run: pnpm run type-check

      - name: Lint
        working-directory: frontend
        run: pnpm run lint

      - name: Build Frontend
        working-directory: frontend
        run: pnpm run build
```

**Step 2: 提交**

```bash
git add .github/workflows/ci.yml
git commit -m "feat(ci): add GitHub Actions CI workflow for backend and frontend"
```

---

### Task 5: 添加前端 type-check 和 lint 脚本

**Files:**
- Modify: `frontend/package.json`

**Step 1: 检查并添加缺失的脚本**

首先检查 package.json 是否有 type-check 和 lint 脚本，如果没有则添加：

```json
{
  "scripts": {
    "type-check": "vue-tsc --noEmit",
    "lint": "eslint . --ext .vue,.js,.jsx,.cjs,.mjs,.ts,.tsx,.cts,.mts --fix"
  }
}
```

**Step 2: 验证修改**

Run: `cd frontend && pnpm run type-check`
Expected: 无类型错误

**Step 3: 提交**

```bash
git add frontend/package.json
git commit -m "feat(frontend): add type-check and lint scripts"
```

---

## Phase 2: 质量保障（短期优化）

### Task 6: 添加后端单元测试基础框架

**Files:**
- Create: `backend/src/test/java/com/devlovecode/aiperm/common/repository/BaseRepositoryTest.java`

**Step 1: 创建测试基类**

```java
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
```

**Step 2: 验证修改**

Run: `cd backend && ./gradlew test`
Expected: Tests run (可能需要配置 test profile)

**Step 3: 提交**

```bash
git add backend/src/test/java/com/devlovecode/aiperm/common/repository/BaseRepositoryTest.java
git commit -m "test(backend): add base test class for repository tests"
```

---

### Task 7: 添加核心 Repository 测试

**Files:**
- Create: `backend/src/test/java/com/devlovecode/aiperm/modules/system/repository/UserRepositoryTest.java`

**Step 1: 创建用户 Repository 测试**

```java
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
```

**Step 2: 创建测试配置文件**

创建 `backend/src/test/resources/application-test.yaml`:

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/aiperm_test?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
  flyway:
    enabled: true
    locations: classpath:db/migration
    clean-disabled: false
```

**Step 3: 验证修改**

Run: `cd backend && ./gradlew test --tests "UserRepositoryTest"`
Expected: Tests pass

**Step 4: 提交**

```bash
git add backend/src/test/java/com/devlovecode/aiperm/modules/system/repository/UserRepositoryTest.java
git add backend/src/test/resources/application-test.yaml
git commit -m "test(backend): add UserRepository unit tests"
```

---

### Task 8: 添加 Service 层测试

**Files:**
- Create: `backend/src/test/java/com/devlovecode/aiperm/modules/auth/service/AuthServiceTest.java`

**Step 1: 创建认证服务测试**

```java
package com.devlovecode.aiperm.modules.auth.service;

import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.auth.dto.request.LoginRequest;
import com.devlovecode.aiperm.modules.auth.vo.CaptchaVO;
import com.devlovecode.aiperm.modules.auth.vo.LoginVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("认证服务测试")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    @DisplayName("生成验证码")
    void testGenerateCaptcha() {
        CaptchaVO captcha = authService.generateCaptcha();
        assertNotNull(captcha.getCaptchaKey(), "验证码 Key 不应为空");
        assertNotNull(captcha.getCaptchaImage(), "验证码图片不应为空");
        assertTrue(captcha.getCaptchaImage().startsWith("data:image"), "应该是 Base64 图片");
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void testLoginUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent_user");
        request.setPassword("any_password");

        assertThrows(BusinessException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void testLoginWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong_password");

        assertThrows(BusinessException.class, () -> authService.login(request));
    }
}
```

**Step 2: 验证修改**

Run: `cd backend && ./gradlew test --tests "AuthServiceTest"`
Expected: Tests pass

**Step 3: 提交**

```bash
git add backend/src/test/java/com/devlovecode/aiperm/modules/auth/service/AuthServiceTest.java
git commit -m "test(backend): add AuthService unit tests"
```

---

### Task 9: 添加前端组合式函数 - useTable

**Files:**
- Create: `frontend/src/composables/useTable.ts`

**Step 1: 创建通用表格组合式函数**

```typescript
import { ref, reactive, computed } from 'vue'
import type { PageResult } from '@/types'

export interface TableOptions<T> {
  fetchData: (params: { page: number; pageSize: number }) => Promise<PageResult<T>>
  defaultPageSize?: number
}

export function useTable<T>(options: TableOptions<T>) {
  const { fetchData, defaultPageSize = 10 } = options

  const loading = ref(false)
  const tableData = ref<T[]>([]) as any
  const pagination = reactive({
    page: 1,
    pageSize: defaultPageSize,
    total: 0
  })

  const fetchDataList = async () => {
    loading.value = true
    try {
      const result = await fetchData({
        page: pagination.page,
        pageSize: pagination.pageSize
      })
      tableData.value = result.list || []
      pagination.total = result.total || 0
    } catch (error) {
      console.error('Failed to fetch data:', error)
      tableData.value = []
    } finally {
      loading.value = false
    }
  }

  const handlePageChange = (page: number) => {
    pagination.page = page
    fetchDataList()
  }

  const handleSizeChange = (size: number) => {
    pagination.pageSize = size
    pagination.page = 1
    fetchDataList()
  }

  const refresh = () => {
    pagination.page = 1
    fetchDataList()
  }

  return {
    loading,
    tableData,
    pagination,
    fetchDataList,
    handlePageChange,
    handleSizeChange,
    refresh
  }
}
```

**Step 2: 验证修改**

Run: `cd frontend && pnpm run type-check`
Expected: 无类型错误

**Step 3: 提交**

```bash
git add frontend/src/composables/useTable.ts
git commit -m "feat(frontend): add useTable composable for table data management"
```

---

### Task 10: 添加前端组合式函数 - useForm

**Files:**
- Create: `frontend/src/composables/useForm.ts`

**Step 1: 创建通用表单组合式函数**

```typescript
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

export interface FormOptions<T> {
  defaultValues?: Partial<T>
  onSubmit?: (values: T) => Promise<void>
  rules?: FormRules
}

export function useForm<T extends Record<string, any>>(options: FormOptions<T> = {}) {
  const { defaultValues = {}, onSubmit, rules = {} } = options

  const formRef = ref<FormInstance>()
  const loading = ref(false)
  const formData = reactive<T>({ ...defaultValues } as T)

  const validate = async (): Promise<boolean> => {
    if (!formRef.value) return true
    try {
      await formRef.value.validate()
      return true
    } catch {
      return false
    }
  }

  const handleSubmit = async () => {
    const isValid = await validate()
    if (!isValid) return false

    if (!onSubmit) return true

    loading.value = true
    try {
      await onSubmit({ ...formData } as T)
      return true
    } catch (error) {
      console.error('Form submit error:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  const resetForm = () => {
    formRef.value?.resetFields()
    Object.assign(formData, defaultValues)
  }

  const setFormData = (values: Partial<T>) => {
    Object.assign(formData, values)
  }

  return {
    formRef,
    formData,
    loading,
    rules,
    validate,
    handleSubmit,
    resetForm,
    setFormData
  }
}
```

**Step 2: 验证修改**

Run: `cd frontend && pnpm run type-check`
Expected: 无类型错误

**Step 3: 提交**

```bash
git add frontend/src/composables/useForm.ts
git commit -m "feat(frontend): add useForm composable for form management"
```

---

### Task 11: 添加后端缓存配置

**Files:**
- Modify: `backend/src/main/java/com/devlovecode/aiperm/config/CacheConfig.java`

**Step 1: 创建缓存配置类**

```java
package com.devlovecode.aiperm.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 默认缓存 30 分钟
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
```

**Step 2: 验证修改**

Run: `cd backend && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

**Step 3: 提交**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/config/CacheConfig.java
git commit -m "feat(backend): add Redis cache configuration"
```

---

### Task 12: 为字典数据添加缓存

**Files:**
- Modify: `backend/src/main/java/com/devlovecode/aiperm/modules/system/service/DictService.java`

**Step 1: 添加缓存注解**

在字典查询方法上添加 `@Cacheable` 注解：

```java
// 在类上添加
@CacheConfig(cacheNames = "dict")

// 在查询方法上添加
@Cacheable(key = "#dictType")
public List<SysDictData> getDictDataByType(String dictType) {
    // 原有逻辑
}

// 在更新方法上添加
@CacheEvict(allEntries = true)
public void updateDictData(SysDictData data) {
    // 原有逻辑
}
```

**Step 2: 验证修改**

Run: `cd backend && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

**Step 3: 提交**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/system/service/DictService.java
git commit -m "feat(backend): add cache for dict data to improve performance"
```

---

## Phase 3: 功能完善（长期规划）

### Task 13: 创建在线用户实体

**Files:**
- Create: `backend/src/main/resources/db/migration/V1.0.10__online_user.sql`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/system/entity/SysOnlineUser.java`

**Step 1: 创建数据库迁移脚本**

```sql
-- 在线用户表
CREATE TABLE sys_online_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    token VARCHAR(100) NOT NULL COMMENT 'Token',
    ip VARCHAR(50) COMMENT '登录IP',
    browser VARCHAR(100) COMMENT '浏览器',
    os VARCHAR(100) COMMENT '操作系统',
    login_time DATETIME NOT NULL COMMENT '登录时间',
    last_access_time DATETIME COMMENT '最后访问时间',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_token (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='在线用户表';
```

**Step 2: 创建实体类**

```java
package com.devlovecode.aiperm.modules.system.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysOnlineUser extends BaseEntity {
    private Long userId;
    private String username;
    private String token;
    private String ip;
    private String browser;
    private String os;
    private LocalDateTime loginTime;
    private LocalDateTime lastAccessTime;
}
```

**Step 3: 提交**

```bash
git add backend/src/main/resources/db/migration/V1.0.10__online_user.sql
git add backend/src/main/java/com/devlovecode/aiperm/modules/system/entity/SysOnlineUser.java
git commit -m "feat(system): add online user entity and migration"
```

---

### Task 14: 创建登录日志实体

**Files:**
- Create: `backend/src/main/resources/db/migration/V1.0.11__login_log.sql`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/log/entity/SysLoginLog.java`

**Step 1: 创建数据库迁移脚本**

```sql
-- 登录日志表
CREATE TABLE sys_login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    ip VARCHAR(50) COMMENT '登录IP',
    location VARCHAR(100) COMMENT '登录地点',
    browser VARCHAR(100) COMMENT '浏览器',
    os VARCHAR(100) COMMENT '操作系统',
    status TINYINT DEFAULT 1 COMMENT '登录状态（1成功 0失败）',
    msg VARCHAR(255) COMMENT '提示消息',
    login_time DATETIME NOT NULL COMMENT '登录时间',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';
```

**Step 2: 创建实体类**

```java
package com.devlovecode.aiperm.modules.log.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysLoginLog extends BaseEntity {
    private Long userId;
    private String username;
    private String ip;
    private String location;
    private String browser;
    private String os;
    private Integer status;
    private String msg;
    private LocalDateTime loginTime;
}
```

**Step 3: 提交**

```bash
git add backend/src/main/resources/db/migration/V1.0.11__login_log.sql
git add backend/src/main/java/com/devlovecode/aiperm/modules/log/entity/SysLoginLog.java
git commit -m "feat(log): add login log entity and migration"
```

---

## 总结

### 优先级

| 阶段 | 任务数 | 预计时间 | 关键收益 |
|------|--------|----------|----------|
| Phase 1 | 5 | 1 周 | 安全性提升、CI/CD 基础 |
| Phase 2 | 7 | 1 个月 | 质量保障、性能优化 |
| Phase 3 | 2 | 3 个月 | 功能完善 |

### 执行建议

1. **Phase 1 必须立即执行**：安全漏洞是高危问题
2. **Phase 2 按优先级执行**：测试优先，缓存次之
3. **Phase 3 可根据需求调整**：在线用户、登录日志是基础功能

### 验证清单

- [ ] 验证码登录校验正常
- [ ] SQL 注入测试通过
- [ ] CI/CD 流水线绿色
- [ ] 单元测试覆盖率 > 50%
- [ ] 字典缓存生效

---

*计划创建时间：2026-02-26*
*基于评估报告：2026-02-26-project-evaluation-report.md*
