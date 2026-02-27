# Phase 5: 第三方 OAuth 登录（企业微信/钉钉/飞书）

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现第三方 OAuth 平台绑定/解绑，并支持通过第三方账号直接登录。采用抽象工厂模式，便于后续扩展。

**前置条件:** Phase 1-2（数据库已创建），Phase 3（AuthService 扩展）已完成

**优先级：** 此阶段为可选阶段，优先实现企业微信。钉钉/飞书结构相同，复制企业微信后修改接口地址即可。

---

## Task 22: OAuth 实体和 Repository

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/oauth/entity/SysUserOauth.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/oauth/entity/SysOauthConfig.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/oauth/repository/UserOauthRepository.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/oauth/repository/OauthConfigRepository.java`

**Step 1: 创建 SysUserOauth 实体**

```java
package com.devlovecode.aiperm.modules.oauth.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserOauth extends BaseEntity {
    private Long userId;
    private String platform;         // WEWORK/DINGTALK/FEISHU
    private String openId;           // 第三方平台用户标识
    private String unionId;          // 企业统一标识（可选）
    private String nickname;
    private String avatar;
    private LocalDateTime lastLoginTime;
    private Integer status;          // 1正常，0已解绑
}
```

**Step 2: 创建 SysOauthConfig 实体**

```java
package com.devlovecode.aiperm.modules.oauth.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysOauthConfig extends BaseEntity {
    private String platform;
    private Integer enabled;
    private String corpId;
    private String agentId;
    private String appKey;
    private String appSecret;
    private String callbackUrl;
    private String remark;
}
```

**Step 3: 创建 UserOauthRepository**

```java
package com.devlovecode.aiperm.modules.oauth.repository;

import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.modules.oauth.entity.SysUserOauth;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
```

**Step 4: 创建 OauthConfigRepository**

```java
package com.devlovecode.aiperm.modules.oauth.repository;

import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.modules.oauth.entity.SysOauthConfig;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OauthConfigRepository extends BaseRepository<SysOauthConfig> {

    public OauthConfigRepository(JdbcClient db) {
        super(db, "sys_oauth_config", SysOauthConfig.class);
    }

    public Optional<SysOauthConfig> findByPlatform(String platform) {
        String sql = "SELECT * FROM sys_oauth_config WHERE platform = :platform AND deleted = 0";
        return db.sql(sql).param("platform", platform).query(SysOauthConfig.class).optional();
    }

    public int update(SysOauthConfig config) {
        String sql = """
            UPDATE sys_oauth_config
            SET enabled = :enabled, corp_id = :corpId, agent_id = :agentId,
                app_key = :appKey, app_secret = :appSecret, callback_url = :callbackUrl,
                remark = :remark, update_time = NOW(), update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("enabled", config.getEnabled())
                .param("corpId", config.getCorpId())
                .param("agentId", config.getAgentId())
                .param("appKey", config.getAppKey())
                .param("appSecret", config.getAppSecret())
                .param("callbackUrl", config.getCallbackUrl())
                .param("remark", config.getRemark())
                .param("updateBy", config.getUpdateBy())
                .param("id", config.getId())
                .update();
    }
}
```

**Step 5: 编译验证**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm/backend && ./gradlew compileJava 2>&1 | tail -5
```

**Step 6: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add backend/src/main/java/com/devlovecode/aiperm/modules/oauth/entity/
git add backend/src/main/java/com/devlovecode/aiperm/modules/oauth/repository/
git commit -m "feat(oauth): add UserOauth and OauthConfig entities + repositories"
```

---

## Task 23: OAuth 抽象框架

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/oauth/provider/OAuthUserInfo.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/oauth/provider/OAuthProvider.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/oauth/provider/AbstractOAuthProvider.java`

**Step 1: 创建 OAuthUserInfo DTO**

```java
package com.devlovecode.aiperm.modules.oauth.provider;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OAuthUserInfo {
    private String openId;
    private String unionId;
    private String nickname;
    private String avatar;
}
```

**Step 2: 创建 OAuthProvider 接口**

```java
package com.devlovecode.aiperm.modules.oauth.provider;

public interface OAuthProvider {
    /** 获取平台标识 */
    String getPlatform();

    /** 根据 code 获取用户信息 */
    OAuthUserInfo getUserInfo(String code);

    /** 获取 OAuth 授权跳转 URL */
    String getAuthorizationUrl(String state);
}
```

**Step 3: 创建 AbstractOAuthProvider（模板方法）**

```java
package com.devlovecode.aiperm.modules.oauth.provider;

import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.oauth.entity.SysOauthConfig;
import com.devlovecode.aiperm.modules.oauth.repository.OauthConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public abstract class AbstractOAuthProvider implements OAuthProvider {

    protected final OauthConfigRepository oauthConfigRepo;
    protected final RestTemplate restTemplate;

    /**
     * 模板方法：获取用户信息
     * 1. 用 code 换取 access_token
     * 2. 用 access_token 获取用户信息
     */
    @Override
    public final OAuthUserInfo getUserInfo(String code) {
        SysOauthConfig config = getConfig();
        String accessToken = getAccessToken(code, config);
        return fetchUserInfo(accessToken, config);
    }

    protected SysOauthConfig getConfig() {
        return oauthConfigRepo.findByPlatform(getPlatform())
                .filter(c -> c.getEnabled() != null && c.getEnabled() == 1)
                .orElseThrow(() -> new BusinessException(getPlatform() + " 登录未启用或未配置"));
    }

    /** 子类实现：用 code 换取 access_token */
    protected abstract String getAccessToken(String code, SysOauthConfig config);

    /** 子类实现：用 access_token 获取用户信息 */
    protected abstract OAuthUserInfo fetchUserInfo(String accessToken, SysOauthConfig config);
}
```

**Step 4: 编译验证**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm/backend && ./gradlew compileJava 2>&1 | tail -5
```

**Step 5: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add backend/src/main/java/com/devlovecode/aiperm/modules/oauth/provider/
git commit -m "feat(oauth): add OAuth provider abstraction and template method"
```

---

## Task 24: 企业微信 OAuth Provider

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/oauth/provider/WeworkOAuthProvider.java`

**Step 1: 创建 WeworkOAuthProvider**

> 企业微信 OAuth 文档：https://developer.work.weixin.qq.com/document/path/91022

```java
package com.devlovecode.aiperm.modules.oauth.provider;

import com.devlovecode.aiperm.modules.oauth.entity.SysOauthConfig;
import com.devlovecode.aiperm.modules.oauth.repository.OauthConfigRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
public class WeworkOAuthProvider extends AbstractOAuthProvider {

    public WeworkOAuthProvider(OauthConfigRepository oauthConfigRepo, RestTemplate restTemplate) {
        super(oauthConfigRepo, restTemplate);
    }

    @Override
    public String getPlatform() {
        return "WEWORK";
    }

    @Override
    public String getAuthorizationUrl(String state) {
        SysOauthConfig config = getConfig();
        return UriComponentsBuilder
                .fromHttpUrl("https://open.weixin.qq.com/connect/oauth2/authorize")
                .queryParam("appid", config.getCorpId())
                .queryParam("redirect_uri", config.getCallbackUrl())
                .queryParam("response_type", "code")
                .queryParam("scope", "snsapi_base")
                .queryParam("state", state)
                .toUriString() + "#wechat_redirect";
    }

    @Override
    protected String getAccessToken(String code, SysOauthConfig config) {
        // 企业微信：用 code 换取 user_ticket（实际是 code 直接换 userid）
        // 企业微信内部应用扫码登录：GET /cgi-bin/user/getuserinfo?access_token=...&code=...
        String tokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/gettoken"
                + "?corpid=" + config.getCorpId()
                + "&corpsecret=" + config.getAppSecret();

        @SuppressWarnings("unchecked")
        Map<String, Object> tokenResp = restTemplate.getForObject(tokenUrl, Map.class);
        if (tokenResp == null || !"0".equals(String.valueOf(tokenResp.get("errcode")))) {
            throw new com.devlovecode.aiperm.common.exception.BusinessException("获取企业微信 access_token 失败");
        }
        return (String) tokenResp.get("access_token");
    }

    @Override
    protected OAuthUserInfo fetchUserInfo(String accessToken, SysOauthConfig config) {
        // 注意：这里的 code 需要在 fetchUserInfo 之前先换取 userId
        // 简化处理：传入 code 作为 accessToken（实际项目需根据企业微信 API 调整）
        // 真实场景：/cgi-bin/user/getuserinfo?access_token={token}&code={code}
        throw new UnsupportedOperationException("WeworkOAuthProvider.fetchUserInfo 需要根据实际 API 实现");
    }
}
```

> **重要提示：** 企业微信/钉钉/飞书的 OAuth 流程比较复杂，实际 API 调用需要根据各平台的最新文档实现。
> 上述代码提供了框架结构，具体 `getAccessToken` 和 `fetchUserInfo` 的实现需要参考对应平台文档。

**Step 2: 注册 RestTemplate Bean**

在 `config/` 目录下创建或修改配置类，添加 RestTemplate Bean：

```java
// 在 WebMvcConfig.java 或新建 AppConfig.java 中添加
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}
```

**Step 3: 编译验证**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm/backend && ./gradlew compileJava 2>&1 | tail -5
```

**Step 4: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add backend/src/main/java/com/devlovecode/aiperm/modules/oauth/provider/WeworkOAuthProvider.java
git commit -m "feat(oauth): add WeworkOAuthProvider skeleton (requires API implementation)"
```

---

## Task 25: OAuthService 和 OAuthController

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/oauth/service/OAuthService.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/oauth/controller/OAuthController.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/oauth/vo/OauthBindingVO.java`

**Step 1: 创建 OauthBindingVO**

```java
package com.devlovecode.aiperm.modules.oauth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "第三方账号绑定信息")
public class OauthBindingVO {
    @Schema(description = "平台：WEWORK/DINGTALK/FEISHU")
    private String platform;

    @Schema(description = "第三方昵称")
    private String nickname;

    @Schema(description = "第三方头像")
    private String avatar;

    @Schema(description = "绑定时间")
    private LocalDateTime createTime;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;
}
```

**Step 2: 创建 OAuthService**

```java
package com.devlovecode.aiperm.modules.oauth.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.auth.vo.LoginVO;
import com.devlovecode.aiperm.modules.oauth.entity.SysUserOauth;
import com.devlovecode.aiperm.modules.oauth.provider.OAuthProvider;
import com.devlovecode.aiperm.modules.oauth.provider.OAuthUserInfo;
import com.devlovecode.aiperm.modules.oauth.repository.UserOauthRepository;
import com.devlovecode.aiperm.modules.oauth.vo.OauthBindingVO;
import com.devlovecode.aiperm.modules.system.entity.SysUser;
import com.devlovecode.aiperm.modules.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserOauthRepository userOauthRepo;
    private final UserRepository userRepo;
    private final List<OAuthProvider> providers;

    private Map<String, OAuthProvider> providerMap;

    @jakarta.annotation.PostConstruct
    public void init() {
        providerMap = providers.stream()
                .collect(Collectors.toMap(OAuthProvider::getPlatform, Function.identity()));
    }

    /** 获取授权跳转 URL */
    public String getAuthorizationUrl(String platform, String state) {
        return getProvider(platform).getAuthorizationUrl(state);
    }

    /**
     * OAuth 登录（通过 code 查找绑定用户，直接登录）
     */
    @Transactional
    public LoginVO oauthLogin(String platform, String code) {
        OAuthUserInfo userInfo = getProvider(platform).getUserInfo(code);

        SysUserOauth binding = userOauthRepo.findByPlatformAndOpenId(platform, userInfo.getOpenId())
                .orElseThrow(() -> new BusinessException("该" + platform + "账号未绑定，请先登录后在个人中心绑定"));

        SysUser user = userRepo.findById(binding.getUserId())
                .orElseThrow(() -> new BusinessException("绑定的用户不存在"));

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        StpUtil.login(user.getId());
        userRepo.updateLoginInfo(user.getId(), "127.0.0.1");
        userOauthRepo.updateLastLoginTime(binding.getId());

        LoginVO.UserInfo loginUserInfo = LoginVO.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
        return LoginVO.builder().token(StpUtil.getTokenValue()).userInfo(loginUserInfo).build();
    }

    /**
     * 主动绑定（用户已登录，绑定第三方账号）
     */
    @Transactional
    public void bind(String platform, String code) {
        Long userId = StpUtil.getLoginIdAsLong();
        OAuthUserInfo userInfo = getProvider(platform).getUserInfo(code);

        // 检查 openId 是否已被其他账号绑定
        userOauthRepo.findByPlatformAndOpenId(platform, userInfo.getOpenId()).ifPresent(existing -> {
            if (!existing.getUserId().equals(userId)) {
                throw new BusinessException("该" + platform + "账号已绑定其他用户");
            }
        });

        // 检查当前用户是否已绑定该平台
        userOauthRepo.findByUserIdAndPlatform(userId, platform).ifPresent(existing -> {
            throw new BusinessException("您已绑定" + platform + "账号，如需重新绑定请先解绑");
        });

        SysUserOauth oauth = new SysUserOauth();
        oauth.setUserId(userId);
        oauth.setPlatform(platform);
        oauth.setOpenId(userInfo.getOpenId());
        oauth.setUnionId(userInfo.getUnionId());
        oauth.setNickname(userInfo.getNickname());
        oauth.setAvatar(userInfo.getAvatar());
        oauth.setLastLoginTime(LocalDateTime.now());
        oauth.setCreateBy(StpUtil.getLoginIdAsString());
        userOauthRepo.insert(oauth);
    }

    /** 解绑第三方账号 */
    @Transactional
    public void unbind(String platform) {
        Long userId = StpUtil.getLoginIdAsLong();
        int rows = userOauthRepo.unbind(userId, platform);
        if (rows == 0) {
            throw new BusinessException("您未绑定该平台账号");
        }
    }

    /** 获取用户已绑定的第三方账号列表 */
    public List<OauthBindingVO> getBindings() {
        Long userId = StpUtil.getLoginIdAsLong();
        return userOauthRepo.findByUserId(userId).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    private OauthBindingVO toVO(SysUserOauth entity) {
        OauthBindingVO vo = new OauthBindingVO();
        vo.setPlatform(entity.getPlatform());
        vo.setNickname(entity.getNickname());
        vo.setAvatar(entity.getAvatar());
        vo.setCreateTime(entity.getCreateTime());
        vo.setLastLoginTime(entity.getLastLoginTime());
        return vo;
    }

    private OAuthProvider getProvider(String platform) {
        OAuthProvider provider = providerMap.get(platform.toUpperCase());
        if (provider == null) {
            throw new BusinessException("不支持的 OAuth 平台：" + platform);
        }
        return provider;
    }
}
```

**Step 3: 创建 OAuthController**

```java
package com.devlovecode.aiperm.modules.oauth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.modules.auth.vo.LoginVO;
import com.devlovecode.aiperm.modules.oauth.service.OAuthService;
import com.devlovecode.aiperm.modules.oauth.vo.OauthBindingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Tag(name = "第三方OAuth")
@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    // ===== 登录相关（无需登录） =====

    @Operation(summary = "跳转第三方登录授权页")
    @GetMapping("/login/{platform}")
    public void login(@PathVariable String platform, HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString().replace("-", "");
        String authUrl = oAuthService.getAuthorizationUrl(platform, state);
        response.sendRedirect(authUrl);
    }

    @Operation(summary = "第三方登录回调")
    @GetMapping("/login/callback/{platform}")
    public R<LoginVO> loginCallback(@PathVariable String platform, @RequestParam String code) {
        return R.ok(oAuthService.oauthLogin(platform, code));
    }

    // ===== 绑定相关（需要登录） =====

    @Operation(summary = "获取已绑定的第三方账号列表")
    @SaCheckLogin
    @GetMapping("/bindings")
    public R<List<OauthBindingVO>> bindings() {
        return R.ok(oAuthService.getBindings());
    }

    @Operation(summary = "跳转绑定第三方账号授权页")
    @SaCheckLogin
    @GetMapping("/bind/{platform}")
    public void bindRedirect(@PathVariable String platform, HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString().replace("-", "");
        String authUrl = oAuthService.getAuthorizationUrl(platform, state);
        response.sendRedirect(authUrl);
    }

    @Operation(summary = "绑定回调")
    @SaCheckLogin
    @GetMapping("/bind/callback/{platform}")
    public R<Void> bindCallback(@PathVariable String platform, @RequestParam String code) {
        oAuthService.bind(platform, code);
        return R.ok();
    }

    @Operation(summary = "解绑第三方账号")
    @SaCheckLogin
    @DeleteMapping("/unbind/{platform}")
    public R<Void> unbind(@PathVariable String platform) {
        oAuthService.unbind(platform);
        return R.ok();
    }
}
```

**Step 4: 编译验证**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm/backend && ./gradlew compileJava 2>&1 | tail -5
```

**Step 5: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add backend/src/main/java/com/devlovecode/aiperm/modules/oauth/
git commit -m "feat(oauth): add OAuthService and OAuthController for bind/login flows"
```

---

## Task 26: 前端 OAuth API

**Files:**
- Create: `frontend/src/api/oauth.ts`

**Step 1: 创建 oauth.ts**

```typescript
/**
 * OAuth 第三方登录模块 API
 * 对应后端 OAuthController (/oauth)
 */
import request from '@/utils/request'
import type { LoginVO } from './auth'

// ==================== 类型定义 ====================

export type OAuthPlatform = 'WEWORK' | 'DINGTALK' | 'FEISHU'

/** 已绑定的第三方账号信息 */
export interface OauthBindingVO {
  platform: OAuthPlatform
  nickname: string
  avatar: string
  createTime: string
  lastLoginTime: string
}

// ==================== API 函数 ====================

export const oauthApi = {
  /** 获取已绑定的第三方账号列表 */
  bindings: () =>
    request.get<OauthBindingVO[]>('/oauth/bindings'),

  /** 解绑第三方账号 */
  unbind: (platform: OAuthPlatform) =>
    request.delete<void>(`/oauth/unbind/${platform}`),

  /** 获取授权跳转 URL（前端直接跳转，非 API 调用） */
  getLoginUrl: (platform: OAuthPlatform) =>
    `/api/oauth/login/${platform}`,

  getBindUrl: (platform: OAuthPlatform) =>
    `/api/oauth/bind/${platform}`,
}
```

**Step 2: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend/src/api/oauth.ts
git commit -m "feat(oauth): add frontend OAuth API file"
```

---

## Phase 5 完成验收

```bash
# 后端编译
cd /Users/shichenyang/IdeaProjects/aiperm/backend && ./gradlew build -x test 2>&1 | tail -5

# 检查新增文件
find /Users/shichenyang/IdeaProjects/aiperm/backend/src/main/java/com/devlovecode/aiperm/modules/oauth -name "*.java" | wc -l
```

期望：后端编译成功，oauth 模块有 10+ 个 Java 文件。

**重要提示：** OAuth 实际功能需要各平台真实配置（corpId、appSecret 等），可在系统配置管理界面（Phase 6）中配置后测试。
