# Testing Patterns

**Analysis Date:** 2025-03-25

## Test Framework

### Backend

**Runner:**
- JUnit 5 (JUnit Jupiter) - Spring Boot default
- Config: Implicit in Spring Boot test dependencies
- Platform: `JUnitPlatform` via Gradle

**Assertion Library:**
- JUnit 5 `Assertions` class: `assertNotNull`, `assertTrue`, `assertThrows`

**Run Commands:**
```bash
cd backend
./gradlew test              # Run all tests
./gradlew test --tests "*AuthServiceTest"  # Run specific test class
./gradlew check             # Run tests + checks
```

### Frontend

**Runner:**
- Not detected (no test framework configured)
- No test files present in codebase

**Config:**
- None detected

## Test File Organization

### Backend

**Location:**
- Co-located with source: `src/test/java/com/devlovecode/aiperm/`
- Mirrors main package structure

**Naming:**
- `{ClassName}Test.java` for unit tests
- Example: `AuthServiceTest.java`, `RoleServiceTest.java`, `BaseRepositoryTest.java`

**Structure:**
```
backend/src/test/java/com/devlovecode/aiperm/
├── AipermApplicationTests.java          # Main application test
├── common/
│   └── repository/
│       └── BaseRepositoryTest.java      # Base test class
├── modules/
│   ├── auth/
│   │   └── service/
│   │       └── AuthServiceTest.java     # Auth service tests
│   ├── system/
│   │   ├── repository/
│   │   │   ├── UserRepositoryTest.java  # User repository tests
│   │   │   └── UserRepositoryTransactionalTest.java
│   │   └── service/
│   │       └── RoleServiceTest.java     # Role service tests
│   └── approval/
│       └── service/
│           ├── ApprovalTodoDecisionTest.java
│           └── ApprovalTodoOverviewServiceTest.java
```

### Frontend

**Location:**
- Not detected (no test directory)

**Naming:**
- N/A

## Test Structure

### Backend

**Suite Organization:**
```java
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("认证服务测试")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

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
}
```

**Patterns:**
- **Setup**: `@SpringBootTest` loads full application context
- **Test profile**: `@ActiveProfiles("test")` for test configuration
- **Transactional**: `@Transactional` auto-rollback after each test
- **DI**: `@Autowired` for dependency injection
- **Display names**: `@DisplayName` for Chinese test descriptions
- **Assertions**: JUnit 5 assertions with descriptive messages
- **Test isolation**: Each test is independent due to rollback

**Teardown:**
- Automatic via `@Transactional` (rollback after each test)
- No explicit `@AfterEach` needed

### Frontend

**Not detected** - No testing framework present

## Mocking

### Backend

**Framework:**
- Mockito (included in Spring Boot Test)
- `@MockBean` for Spring beans
- `@Mock` for plain Mockito mocks

**Patterns:**
```java
// Test with real dependencies (common pattern observed)
@SpringBootTest
class AuthServiceTest {
    @Autowired
    private AuthService authService;  // Real service

    @Autowired
    private StringRedisTemplate redisTemplate;  // Real Redis
}
```

**What to Mock:**
- External services (SMS, email, OSS)
- Third-party APIs
- Infrastructure dependencies (Redis in unit tests)

**What NOT to Mock:**
- Repositories (use `@Transactional` with rollback)
- Services under test
- DTOs/VOs

**Example mocking pattern (not observed but standard):**
```java
@MockBean
private SmsService smsService;

@BeforeEach
void setUp() {
    when(smsService.send(any())).thenReturn(true);
}
```

### Frontend

**Not detected**

## Fixtures and Factories

### Backend

**Test Data:**
- Created inline in test methods
- Builder patterns via `new DTO()` and setters

**Example:**
```java
@Test
void testLoginUserNotFound() {
    LoginRequest request = new LoginRequest();
    request.setUsername("nonexistent_user");
    request.setPassword("any_password");
    request.setCaptchaKey("test_key");
    request.setCaptcha("test");

    assertThrows(BusinessException.class, () -> authService.login(request));
}
```

**Location:**
- No dedicated fixtures directory
- Data created within test methods

### Frontend

**Not detected**

## Coverage

### Backend

**Requirements:**
- No explicit coverage target detected
- Standard Spring Boot test approach (integration-heavy)

**View Coverage:**
```bash
./gradlew test jacocoTestReport    # Generate JaCoCo report
# Report location: build/reports/jacoco/test/html/index.html
```

### Frontend

**Not detected**

## Test Types

### Backend

**Unit Tests:**
- Service layer tests with `@SpringBootTest`
- Repository tests with `@Transactional` rollback
- Example: `RoleServiceTest`, `UserRepositoryTest`

**Integration Tests:**
- Full context tests with real database
- `@SpringBootTest` loads entire application
- Example: `AuthServiceTest`, `AipermApplicationTests`

**E2E Tests:**
- Not detected (no dedicated E2E framework)

**Transactional Tests:**
- All tests use `@Transactional` for auto-rollback
- Ensures test isolation

### Frontend

**Not detected**

## Common Patterns

### Backend

**Async Testing:**
```java
// Not explicitly observed, but standard pattern:
@Test
void testAsyncOperation() throws Exception {
    Future<Result> future = asyncService.operation();
    Result result = future.get(5, TimeUnit.SECONDS);
    assertNotNull(result);
}
```

**Error Testing:**
```java
@Test
@DisplayName("登录失败 - 用户不存在")
void testLoginUserNotFound() {
    assertThrows(BusinessException.class, () -> {
        authService.login(request);
    });
}
```

**Pagination Testing:**
```java
@Test
@DisplayName("分页查询")
void testQueryPage() {
    PageResult<SysRole> result = roleService.queryPage(dto);
    assertNotNull(result.getList());
    assertTrue(result.getTotal() > 0);
}
```

### Frontend

**Not detected**

---

*Testing analysis: 2025-03-25*
