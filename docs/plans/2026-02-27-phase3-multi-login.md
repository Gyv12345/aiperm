# Phase 3: 多方式登录策略 + 前端改造

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 使用策略模式实现手机号/邮箱登录，修改 AuthController 支持多登录类型，前端登录页面改造为 Tab 切换形式。

**前置条件:** Phase 1-2 已完成（验证码服务已可用）

---

## Task 9: 登录策略接口和统一登录 DTO

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/auth/strategy/LoginStrategy.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/auth/strategy/LoginType.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/auth/dto/request/UnifiedLoginRequest.java`

**Step 1: 创建 LoginType 枚举**

```java
package com.devlovecode.aiperm.modules.auth.strategy;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoginType {
    PASSWORD("password", "账号密码登录"),
    SMS("sms", "手机号验证码登录"),
    EMAIL("email", "邮箱验证码登录"),
    OAUTH("oauth", "第三方OAuth登录");

    private final String code;
    private final String desc;
}
```

**Step 2: 创建 LoginStrategy 接口**

```java
package com.devlovecode.aiperm.modules.auth.strategy;

import com.devlovecode.aiperm.modules.auth.dto.request.UnifiedLoginRequest;
import com.devlovecode.aiperm.modules.auth.vo.LoginVO;

public interface LoginStrategy {
    /** 执行登录 */
    LoginVO login(UnifiedLoginRequest request);

    /** 获取策略类型 */
    LoginType getType();
}
```

**Step 3: 创建 UnifiedLoginRequest DTO**

```java
package com.devlovecode.aiperm.modules.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "统一登录请求")
public class UnifiedLoginRequest {

    @Schema(description = "登录类型：password/sms/email", example = "password")
    @NotBlank(message = "登录类型不能为空")
    private String loginType;

    // ===== 账号密码登录 =====
    @Schema(description = "用户名（账号密码登录）")
    private String username;

    @Schema(description = "密码（账号密码登录）")
    private String password;

    @Schema(description = "图形验证码（账号密码登录）")
    private String captcha;

    @Schema(description = "图形验证码Key（账号密码登录）")
    private String captchaKey;

    // ===== 手机号/邮箱登录 =====
    @Schema(description = "手机号或邮箱（短信/邮件登录）")
    private String target;

    @Schema(description = "短信/邮件验证码")
    private String code;
}
```

**Step 4: 编译验证**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm/backend && ./gradlew compileJava 2>&1 | tail -5
```

期望输出：`BUILD SUCCESSFUL`

**Step 5: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add backend/src/main/java/com/devlovecode/aiperm/modules/auth/strategy/
git add backend/src/main/java/com/devlovecode/aiperm/modules/auth/dto/request/UnifiedLoginRequest.java
git commit -m "feat(auth): add LoginStrategy interface and UnifiedLoginRequest DTO"
```

---

## Task 10: 密码登录策略（重构现有逻辑）

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/auth/strategy/PasswordLoginStrategy.java`

**Step 1: 创建 PasswordLoginStrategy**

将 `AuthService.login()` 中的核心逻辑提取到策略中：

```java
package com.devlovecode.aiperm.modules.auth.strategy;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.auth.dto.request.UnifiedLoginRequest;
import com.devlovecode.aiperm.modules.auth.vo.LoginVO;
import com.devlovecode.aiperm.modules.system.entity.SysUser;
import com.devlovecode.aiperm.modules.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordLoginStrategy implements LoginStrategy {

    private final UserRepository userRepo;
    private final StringRedisTemplate redisTemplate;

    private static final String CAPTCHA_PREFIX = "captcha:";

    @Override
    public LoginVO login(UnifiedLoginRequest request) {
        // 1. 验证图形验证码
        validateCaptcha(request.getCaptchaKey(), request.getCaptcha());

        // 2. 查询用户
        SysUser user = userRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        // 3. 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 4. 密码校验
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 5. 执行 Sa-Token 登录
        StpUtil.login(user.getId());

        // 6. 更新最后登录信息
        userRepo.updateLoginInfo(user.getId(), "127.0.0.1");

        return buildLoginVO(user);
    }

    @Override
    public LoginType getType() {
        return LoginType.PASSWORD;
    }

    private void validateCaptcha(String captchaKey, String captcha) {
        if (captchaKey == null || captchaKey.isBlank()) {
            throw new BusinessException("验证码Key不能为空");
        }
        String key = CAPTCHA_PREFIX + captchaKey;
        String storedCode = redisTemplate.opsForValue().get(key);
        if (storedCode == null) {
            throw new BusinessException("验证码已过期");
        }
        if (!storedCode.equals(captcha != null ? captcha.toLowerCase() : "")) {
            throw new BusinessException("验证码错误");
        }
        redisTemplate.delete(key);
    }

    private LoginVO buildLoginVO(SysUser user) {
        LoginVO.UserInfo userInfo = LoginVO.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
        return LoginVO.builder()
                .token(StpUtil.getTokenValue())
                .userInfo(userInfo)
                .build();
    }
}
```

**Step 2: 编译验证**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm/backend && ./gradlew compileJava 2>&1 | tail -5
```

期望：`BUILD SUCCESSFUL`

**Step 3: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add backend/src/main/java/com/devlovecode/aiperm/modules/auth/strategy/PasswordLoginStrategy.java
git commit -m "feat(auth): implement PasswordLoginStrategy extracting existing login logic"
```

---

## Task 11: 短信/邮箱登录策略

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/auth/strategy/SmsLoginStrategy.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/auth/strategy/EmailLoginStrategy.java`

**Step 1: 创建 SmsLoginStrategy**

```java
package com.devlovecode.aiperm.modules.auth.strategy;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.auth.dto.request.UnifiedLoginRequest;
import com.devlovecode.aiperm.modules.auth.vo.LoginVO;
import com.devlovecode.aiperm.modules.captcha.enums.CaptchaScene;
import com.devlovecode.aiperm.modules.captcha.service.CaptchaService;
import com.devlovecode.aiperm.modules.system.entity.SysUser;
import com.devlovecode.aiperm.modules.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmsLoginStrategy implements LoginStrategy {

    private final UserRepository userRepo;
    @Qualifier("smsCaptchaService")
    private final CaptchaService smsCaptchaService;

    @Override
    public LoginVO login(UnifiedLoginRequest request) {
        String phone = request.getTarget();
        String code = request.getCode();

        if (phone == null || phone.isBlank()) {
            throw new BusinessException("手机号不能为空");
        }
        if (code == null || code.isBlank()) {
            throw new BusinessException("验证码不能为空");
        }

        // 验证短信验证码
        if (!smsCaptchaService.verify(phone, code, CaptchaScene.LOGIN)) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 根据手机号查询用户
        SysUser user = userRepo.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("该手机号未注册"));

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        StpUtil.login(user.getId());
        userRepo.updateLoginInfo(user.getId(), "127.0.0.1");

        LoginVO.UserInfo userInfo = LoginVO.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
        return LoginVO.builder()
                .token(StpUtil.getTokenValue())
                .userInfo(userInfo)
                .build();
    }

    @Override
    public LoginType getType() {
        return LoginType.SMS;
    }
}
```

**Step 2: 创建 EmailLoginStrategy**

```java
package com.devlovecode.aiperm.modules.auth.strategy;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.auth.dto.request.UnifiedLoginRequest;
import com.devlovecode.aiperm.modules.auth.vo.LoginVO;
import com.devlovecode.aiperm.modules.captcha.enums.CaptchaScene;
import com.devlovecode.aiperm.modules.captcha.service.CaptchaService;
import com.devlovecode.aiperm.modules.system.entity.SysUser;
import com.devlovecode.aiperm.modules.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailLoginStrategy implements LoginStrategy {

    private final UserRepository userRepo;
    @Qualifier("emailCaptchaService")
    private final CaptchaService emailCaptchaService;

    @Override
    public LoginVO login(UnifiedLoginRequest request) {
        String email = request.getTarget();
        String code = request.getCode();

        if (email == null || email.isBlank()) {
            throw new BusinessException("邮箱不能为空");
        }
        if (code == null || code.isBlank()) {
            throw new BusinessException("验证码不能为空");
        }

        if (!emailCaptchaService.verify(email, code, CaptchaScene.LOGIN)) {
            throw new BusinessException("验证码错误或已过期");
        }

        SysUser user = userRepo.findByEmail(email)
                .orElseThrow(() -> new BusinessException("该邮箱未注册"));

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        StpUtil.login(user.getId());
        userRepo.updateLoginInfo(user.getId(), "127.0.0.1");

        LoginVO.UserInfo userInfo = LoginVO.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
        return LoginVO.builder()
                .token(StpUtil.getTokenValue())
                .userInfo(userInfo)
                .build();
    }

    @Override
    public LoginType getType() {
        return LoginType.EMAIL;
    }
}
```

**Step 3: 在 UserRepository 中添加 findByPhone/findByEmail 方法**

> 先检查 UserRepository 是否已有这些方法。
> 如果没有，在 `backend/src/main/java/com/devlovecode/aiperm/modules/system/repository/UserRepository.java` 中添加：

```java
public Optional<SysUser> findByPhone(String phone) {
    String sql = "SELECT * FROM sys_user WHERE phone = :phone AND deleted = 0";
    return db.sql(sql).param("phone", phone).query(SysUser.class).optional();
}

public Optional<SysUser> findByEmail(String email) {
    String sql = "SELECT * FROM sys_user WHERE email = :email AND deleted = 0";
    return db.sql(sql).param("email", email).query(SysUser.class).optional();
}
```

**Step 4: 编译验证**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm/backend && ./gradlew compileJava 2>&1 | tail -5
```

期望：`BUILD SUCCESSFUL`

**Step 5: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add backend/src/main/java/com/devlovecode/aiperm/modules/auth/strategy/
git add backend/src/main/java/com/devlovecode/aiperm/modules/system/repository/UserRepository.java
git commit -m "feat(auth): implement SMS and Email login strategies"
```

---

## Task 12: 登录策略工厂 + 登录配置 VO

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/auth/strategy/LoginStrategyFactory.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/auth/vo/LoginConfigVO.java`

**Step 1: 创建 LoginStrategyFactory**

```java
package com.devlovecode.aiperm.modules.auth.strategy;

import com.devlovecode.aiperm.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LoginStrategyFactory {

    private final List<LoginStrategy> strategies;

    private Map<LoginType, LoginStrategy> strategyMap;

    @jakarta.annotation.PostConstruct
    public void init() {
        strategyMap = strategies.stream()
                .collect(Collectors.toMap(LoginStrategy::getType, Function.identity()));
    }

    public LoginStrategy getStrategy(String loginType) {
        try {
            LoginType type = LoginType.valueOf(loginType.toUpperCase());
            LoginStrategy strategy = strategyMap.get(type);
            if (strategy == null) {
                throw new BusinessException("不支持的登录类型：" + loginType);
            }
            return strategy;
        } catch (IllegalArgumentException e) {
            throw new BusinessException("未知的登录类型：" + loginType);
        }
    }
}
```

**Step 2: 创建 LoginConfigVO**

```java
package com.devlovecode.aiperm.modules.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "登录配置（前端根据此配置显示登录Tab）")
public class LoginConfigVO {

    @Schema(description = "是否启用短信登录")
    private boolean smsEnabled;

    @Schema(description = "是否启用邮箱登录")
    private boolean emailEnabled;

    @Schema(description = "是否启用企业微信登录")
    private boolean weworkEnabled;

    @Schema(description = "是否启用钉钉登录")
    private boolean dingtalkEnabled;

    @Schema(description = "是否启用飞书登录")
    private boolean feishuEnabled;
}
```

**Step 3: 编译验证**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm/backend && ./gradlew compileJava 2>&1 | tail -5
```

期望：`BUILD SUCCESSFUL`

**Step 4: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add backend/src/main/java/com/devlovecode/aiperm/modules/auth/strategy/LoginStrategyFactory.java
git add backend/src/main/java/com/devlovecode/aiperm/modules/auth/vo/LoginConfigVO.java
git commit -m "feat(auth): add LoginStrategyFactory and LoginConfigVO"
```

---

## Task 13: 修改 AuthController 和 AuthService 支持多登录

**Files:**
- Modify: `backend/src/main/java/com/devlovecode/aiperm/modules/auth/controller/AuthController.java`
- Modify: `backend/src/main/java/com/devlovecode/aiperm/modules/auth/service/AuthService.java`

**Step 1: 在 AuthController 中新增接口**

在 `AuthController.java` 中，保留原有的 `/auth/captcha`、`/auth/login`（原账号密码登录，向后兼容）、`/auth/logout`、`/auth/info`、`/auth/menus` 接口。

**新增**以下接口到 AuthController：

```java
// 在类中注入新依赖
private final LoginStrategyFactory loginStrategyFactory;
private final com.devlovecode.aiperm.modules.enterprise.repository.ConfigRepository configRepo;

// 新增：统一登录接口（多种登录方式）
@Operation(summary = "统一登录（支持多种方式）")
@PostMapping("/login/unified")
public R<LoginVO> unifiedLogin(@RequestBody @Valid UnifiedLoginRequest request) {
    return R.ok(loginStrategyFactory.getStrategy(request.getLoginType()).login(request));
}

// 新增：获取登录配置
@Operation(summary = "获取登录配置（前端用于显示/隐藏登录方式）")
@GetMapping("/config")
public R<LoginConfigVO> loginConfig() {
    return R.ok(authService.getLoginConfig());
}
```

**Step 2: 在 AuthService 中新增 getLoginConfig 方法**

在 `AuthService.java` 中注入 `ConfigRepository` 并添加：

```java
// 在顶部注入（使用 @RequiredArgsConstructor，在字段声明处添加）
private final com.devlovecode.aiperm.modules.enterprise.repository.ConfigRepository configRepo;

// 新增方法
public LoginConfigVO getLoginConfig() {
    return LoginConfigVO.builder()
            .smsEnabled(isConfigEnabled("login.sms.enabled"))
            .emailEnabled(isConfigEnabled("login.email.enabled"))
            .weworkEnabled(isConfigEnabled("oauth.wework.enabled"))
            .dingtalkEnabled(isConfigEnabled("oauth.dingtalk.enabled"))
            .feishuEnabled(isConfigEnabled("oauth.feishu.enabled"))
            .build();
}

private boolean isConfigEnabled(String key) {
    return configRepo.findByKey(key)
            .map(c -> "1".equals(c.getConfigValue()))
            .orElse(false);
}
```

> 注意：`ConfigRepository.findByKey()` 方法可能需要先检查是否存在。如不存在，需要添加该方法：
> ```java
> public Optional<SysConfig> findByKey(String configKey) {
>     String sql = "SELECT * FROM sys_config WHERE config_key = :configKey AND deleted = 0";
>     return db.sql(sql).param("configKey", configKey).query(SysConfig.class).optional();
> }
> ```

**Step 3: 编译验证**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm/backend && ./gradlew compileJava 2>&1 | tail -10
```

期望：`BUILD SUCCESSFUL`

**Step 4: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add backend/src/main/java/com/devlovecode/aiperm/modules/auth/
git add backend/src/main/java/com/devlovecode/aiperm/modules/enterprise/repository/ConfigRepository.java
git commit -m "feat(auth): add unified login endpoint and login config API"
```

---

## Task 14: 前端 auth API 更新

**Files:**
- Modify: `frontend/src/api/auth.ts`

**Step 1: 在 auth.ts 中追加新类型和接口**

在现有 auth.ts 末尾，**追加**（不删除原有内容）：

```typescript
// ==================== 多方式登录相关类型 ====================

/** 统一登录请求（多种方式）*/
export interface UnifiedLoginRequest {
  loginType: 'password' | 'sms' | 'email'
  // 密码登录
  username?: string
  password?: string
  captcha?: string
  captchaKey?: string
  // 短信/邮箱登录
  target?: string
  code?: string
}

/** 登录配置（控制前端显示哪些登录Tab） */
export interface LoginConfigVO {
  smsEnabled: boolean
  emailEnabled: boolean
  weworkEnabled: boolean
  dingtalkEnabled: boolean
  feishuEnabled: boolean
}
```

在 `authApi` 对象中追加：

```typescript
  /** 统一登录（支持多种方式） */
  unifiedLogin: (data: UnifiedLoginRequest) =>
    request.post<LoginVO>('/auth/login/unified', data),

  /** 获取登录配置 */
  loginConfig: () =>
    request.get<LoginConfigVO>('/auth/config'),
```

**Step 2: 编译检查（TypeScript）**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm/frontend && npx tsc --noEmit 2>&1 | head -20
```

期望：无错误输出

**Step 3: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend/src/api/auth.ts
git commit -m "feat(auth): extend auth.ts with unified login and login config APIs"
```

---

## Task 15: 前端登录页面改造

**Files:**
- Modify: `frontend/src/views/login/index.vue`

**Step 1: 重写登录页面脚本部分**

将 `<script setup lang="ts">` 部分替换为：

```typescript
<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { User, Lock, Picture, Iphone, Message } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  authApi,
  type LoginRequest,
  type CaptchaVO,
  type LoginConfigVO,
  type UnifiedLoginRequest,
} from '@/api/auth'
import { captchaApi } from '@/api/captcha'

const router = useRouter()
const userStore = useUserStore()

// 登录方式 Tab
type LoginTab = 'password' | 'sms' | 'email'
const activeTab = ref<LoginTab>('password')

// 登录配置（控制显示哪些 Tab）
const loginConfig = ref<LoginConfigVO>({
  smsEnabled: false,
  emailEnabled: false,
  weworkEnabled: false,
  dingtalkEnabled: false,
  feishuEnabled: false,
})

// 表单引用
const passwordFormRef = ref<FormInstance>()
const smsFormRef = ref<FormInstance>()
const emailFormRef = ref<FormInstance>()

// 密码登录表单
const passwordForm = reactive({
  username: '',
  password: '',
  captcha: '',
  captchaKey: '',
})

// 短信登录表单
const smsForm = reactive({
  phone: '',
  code: '',
})

// 邮箱登录表单
const emailForm = reactive({
  email: '',
  code: '',
})

// 验证码图片
const captchaData = ref<CaptchaVO>({ captchaKey: '', captchaImage: '' })

// 发送验证码倒计时
const smsCooldown = ref(0)
const emailCooldown = ref(0)

// 加载状态
const loading = ref(false)

// 密码登录表单规则
const passwordRules = computed(() => ({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码为4位', trigger: 'blur' },
  ],
}))

// 短信登录表单规则
const smsRules = computed(() => ({
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}))

// 邮箱登录表单规则
const emailRules = computed(() => ({
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱', trigger: 'blur' },
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}))

// 获取图形验证码
async function fetchCaptcha() {
  try {
    const data = await authApi.captcha()
    if (data) {
      captchaData.value = {
        captchaKey: data.captchaKey || '',
        captchaImage: data.captchaImage || '',
      }
      passwordForm.captchaKey = data.captchaKey || ''
    }
  }
  catch (e) {
    ElMessage.error('获取验证码失败')
  }
}

// 发送短信验证码
async function sendSmsCode() {
  if (!smsForm.phone || !/^1[3-9]\d{9}$/.test(smsForm.phone)) {
    ElMessage.warning('请先输入正确的手机号')
    return
  }
  try {
    await captchaApi.send({ target: smsForm.phone, type: 'SMS', scene: 'LOGIN' })
    ElMessage.success('验证码已发送')
    startCooldown('sms')
  }
  catch (e: any) {
    ElMessage.error(e?.message || '发送失败')
  }
}

// 发送邮件验证码
async function sendEmailCode() {
  if (!emailForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  try {
    await captchaApi.send({ target: emailForm.email, type: 'EMAIL', scene: 'LOGIN' })
    ElMessage.success('验证码已发送')
    startCooldown('email')
  }
  catch (e: any) {
    ElMessage.error(e?.message || '发送失败')
  }
}

// 倒计时
function startCooldown(type: 'sms' | 'email') {
  const counter = type === 'sms' ? smsCooldown : emailCooldown
  counter.value = 60
  const timer = setInterval(() => {
    counter.value--
    if (counter.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

// 执行登录
async function handleLogin() {
  loading.value = true
  try {
    let loginData: any

    if (activeTab.value === 'password') {
      await passwordFormRef.value?.validate()
      const req: UnifiedLoginRequest = {
        loginType: 'password',
        username: passwordForm.username,
        password: passwordForm.password,
        captcha: passwordForm.captcha,
        captchaKey: passwordForm.captchaKey,
      }
      loginData = await authApi.unifiedLogin(req)
    }
    else if (activeTab.value === 'sms') {
      await smsFormRef.value?.validate()
      const req: UnifiedLoginRequest = {
        loginType: 'sms',
        target: smsForm.phone,
        code: smsForm.code,
      }
      loginData = await authApi.unifiedLogin(req)
    }
    else if (activeTab.value === 'email') {
      await emailFormRef.value?.validate()
      const req: UnifiedLoginRequest = {
        loginType: 'email',
        target: emailForm.email,
        code: emailForm.code,
      }
      loginData = await authApi.unifiedLogin(req)
    }

    if (loginData) {
      userStore.setToken(loginData.token || '')
      if (loginData.nickname) {
        userStore.setUserInfo({
          id: 0,
          username: loginData.username || '',
          nickname: loginData.nickname || '',
          roles: ['admin'],
          permissions: ['*'],
        })
      }
      ElMessage.success('登录成功')
      router.push('/')
    }
  }
  catch (e: any) {
    fetchCaptcha()
    passwordForm.captcha = ''
  }
  finally {
    loading.value = false
  }
}

onMounted(async () => {
  fetchCaptcha()
  try {
    const config = await authApi.loginConfig()
    if (config) loginConfig.value = config
  }
  catch (e) {
    // 忽略配置获取失败
  }
})
</script>
```

**Step 2: 重写 template 部分**

将 `<template>` 内的登录表单区域（`.login-form-container` 内部）替换，增加 Tab 切换：

```html
<template>
  <div class="login-container">
    <!-- 左侧品牌区域保持不变 -->
    <div class="login-brand">
      <!-- 原有品牌内容不变 -->
      <div class="brand-content">
        <div class="brand-logo">
          <img src="/logo.png" alt="爱编程" class="logo-img">
        </div>
        <h1 class="brand-title">AIPerm 权限管理系统</h1>
        <p class="brand-subtitle">专业的企业级 RBAC 权限管理解决方案</p>
        <div class="brand-features">
          <div class="feature-item">
            <svg class="feature-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
            </svg>
            <span>安全可靠</span>
          </div>
          <div class="feature-item">
            <svg class="feature-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" />
            </svg>
            <span>高效便捷</span>
          </div>
          <div class="feature-item">
            <svg class="feature-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10" />
              <path d="M12 6v6l4 2" />
            </svg>
            <span>实时响应</span>
          </div>
        </div>
      </div>
      <div class="brand-decoration">
        <div class="decoration-circle decoration-circle-1" />
        <div class="decoration-circle decoration-circle-2" />
        <div class="decoration-circle decoration-circle-3" />
      </div>
    </div>

    <!-- 右侧登录区域 -->
    <div class="login-form-wrapper">
      <div class="login-form-container">
        <div class="mobile-logo">
          <img src="/logo.png" alt="爱编程" class="logo-img-mobile">
        </div>

        <div class="login-header">
          <h2 class="login-title">欢迎登录</h2>
          <p class="login-subtitle">请选择登录方式</p>
        </div>

        <!-- 登录方式 Tabs -->
        <el-tabs v-model="activeTab" class="login-tabs">
          <!-- 账号密码 Tab -->
          <el-tab-pane label="账号密码" name="password">
            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="0"
              size="large"
              @keyup.enter="handleLogin"
            >
              <el-form-item prop="username">
                <el-input v-model="passwordForm.username" placeholder="请输入用户名" :prefix-icon="User" clearable />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                  v-model="passwordForm.password"
                  type="password"
                  placeholder="请输入密码"
                  :prefix-icon="Lock"
                  show-password
                />
              </el-form-item>
              <el-form-item prop="captcha">
                <div class="captcha-row">
                  <el-input
                    v-model="passwordForm.captcha"
                    placeholder="请输入验证码"
                    :prefix-icon="Picture"
                    clearable
                    class="captcha-input"
                  />
                  <div class="captcha-box" title="点击刷新验证码" @click="fetchCaptcha">
                    <img v-if="captchaData.captchaImage" :src="captchaData.captchaImage" alt="验证码" class="captcha-img">
                    <div v-else class="captcha-loading">点击刷新</div>
                  </div>
                </div>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- 手机号 Tab（仅配置启用时显示） -->
          <el-tab-pane v-if="loginConfig.smsEnabled" label="手机号" name="sms">
            <el-form
              ref="smsFormRef"
              :model="smsForm"
              :rules="smsRules"
              label-width="0"
              size="large"
              @keyup.enter="handleLogin"
            >
              <el-form-item prop="phone">
                <el-input v-model="smsForm.phone" placeholder="请输入手机号" :prefix-icon="Iphone" clearable />
              </el-form-item>
              <el-form-item prop="code">
                <div class="captcha-row">
                  <el-input
                    v-model="smsForm.code"
                    placeholder="请输入短信验证码"
                    clearable
                    class="captcha-input"
                  />
                  <el-button
                    :disabled="smsCooldown > 0"
                    class="send-code-btn"
                    @click="sendSmsCode"
                  >
                    {{ smsCooldown > 0 ? `${smsCooldown}s` : '获取验证码' }}
                  </el-button>
                </div>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- 邮箱 Tab（仅配置启用时显示） -->
          <el-tab-pane v-if="loginConfig.emailEnabled" label="邮箱" name="email">
            <el-form
              ref="emailFormRef"
              :model="emailForm"
              :rules="emailRules"
              label-width="0"
              size="large"
              @keyup.enter="handleLogin"
            >
              <el-form-item prop="email">
                <el-input v-model="emailForm.email" placeholder="请输入邮箱" :prefix-icon="Message" clearable />
              </el-form-item>
              <el-form-item prop="code">
                <div class="captcha-row">
                  <el-input
                    v-model="emailForm.code"
                    placeholder="请输入邮件验证码"
                    clearable
                    class="captcha-input"
                  />
                  <el-button
                    :disabled="emailCooldown > 0"
                    class="send-code-btn"
                    @click="sendEmailCode"
                  >
                    {{ emailCooldown > 0 ? `${emailCooldown}s` : '获取验证码' }}
                  </el-button>
                </div>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>

        <!-- 登录按钮 -->
        <el-button
          type="primary"
          :loading="loading"
          class="login-btn"
          @click="handleLogin"
        >
          {{ loading ? '登录中...' : '登 录' }}
        </el-button>

        <!-- 第三方登录（根据配置显示） -->
        <div
          v-if="loginConfig.weworkEnabled || loginConfig.dingtalkEnabled || loginConfig.feishuEnabled"
          class="oauth-section"
        >
          <el-divider>第三方登录</el-divider>
          <div class="oauth-buttons">
            <el-button v-if="loginConfig.weworkEnabled" class="oauth-btn" @click="() => {}">
              企业微信
            </el-button>
            <el-button v-if="loginConfig.dingtalkEnabled" class="oauth-btn" @click="() => {}">
              钉钉
            </el-button>
            <el-button v-if="loginConfig.feishuEnabled" class="oauth-btn" @click="() => {}">
              飞书
            </el-button>
          </div>
        </div>

        <div class="login-tips">
          <span class="tips-label">默认账号：</span>
          <span class="tips-value">admin / admin123</span>
        </div>
      </div>

      <div class="login-footer">
        <p class="company-name">河南爱编程网络科技有限公司</p>
        <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener noreferrer" class="icp-link">
          豫ICP备2024074107号-2
        </a>
      </div>
    </div>
  </div>
</template>
```

**Step 3: 在 `<style scoped>` 中追加新样式**

在现有样式末尾追加：

```css
/* 登录 Tabs */
.login-tabs {
  margin-bottom: 1rem;
}

:deep(.el-tabs__header) {
  margin-bottom: 1.5rem;
}

/* 发送验证码按钮 */
.send-code-btn {
  width: 110px;
  flex-shrink: 0;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 44px;
  font-size: 1rem;
  font-weight: 500;
  margin-bottom: 1rem;
}

/* 第三方登录 */
.oauth-section {
  margin-top: 1rem;
}

.oauth-buttons {
  display: flex;
  gap: 0.75rem;
  justify-content: center;
}

.oauth-btn {
  flex: 1;
}
```

**Step 4: TypeScript 编译检查**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm/frontend && npx tsc --noEmit 2>&1 | head -20
```

期望：无错误

**Step 5: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend/src/views/login/index.vue
git commit -m "feat(login): redesign login page with multi-tab support (password/sms/email)"
```

---

## Phase 3 完成验收

```bash
# 后端编译
cd /Users/shichenyang/IdeaProjects/aiperm/backend && ./gradlew build -x test 2>&1 | tail -5

# 前端 TypeScript 检查
cd /Users/shichenyang/IdeaProjects/aiperm/frontend && npx tsc --noEmit 2>&1 | wc -l
```

期望：后端 `BUILD SUCCESSFUL`，前端 TypeScript 错误数为 0。
