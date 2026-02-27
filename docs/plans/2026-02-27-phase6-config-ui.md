# Phase 6: 系统配置管理界面

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为运维人员提供图形化配置界面，管理短信/邮件服务配置、2FA策略、OAuth配置。

**前置条件:** Phase 1-5 的后端代码已完成（数据库表、Repository、Entity 已存在）

---

## Task 27: 短信/邮件配置管理后端接口

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/captcha/service/CaptchaConfigService.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/captcha/controller/CaptchaConfigController.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/captcha/dto/CaptchaConfigDTO.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/captcha/vo/CaptchaConfigVO.java`

**Step 1: 创建 CaptchaConfigVO**

```java
package com.devlovecode.aiperm.modules.captcha.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "验证码配置响应（密钥脱敏处理）")
public class CaptchaConfigVO {
    private Long id;
    private String type;             // SMS/EMAIL
    private Integer enabled;

    // SMS 配置（密钥脱敏）
    private String smsProvider;
    private String smsAccessKey;     // 返回时脱敏：前4位+****
    private String smsSignName;
    private String smsTemplateCode;

    // Email 配置（密码脱敏）
    private String emailHost;
    private Integer emailPort;
    private String emailUsername;
    private String emailFrom;
    private String emailFromName;

    // 通用
    private Integer codeLength;
    private Integer expireMinutes;
    private Integer dailyLimit;
}
```

**Step 2: 创建 CaptchaConfigDTO**

```java
package com.devlovecode.aiperm.modules.captcha.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "验证码配置更新请求")
public class CaptchaConfigDTO {

    @JsonView(Views.Update.class)
    private Integer enabled;

    // SMS
    @JsonView(Views.Update.class)
    private String smsProvider;
    @JsonView(Views.Update.class)
    private String smsAccessKey;
    @JsonView(Views.Update.class)
    private String smsSecretKey;
    @JsonView(Views.Update.class)
    private String smsSignName;
    @JsonView(Views.Update.class)
    private String smsTemplateCode;

    // Email
    @JsonView(Views.Update.class)
    private String emailHost;
    @JsonView(Views.Update.class)
    private Integer emailPort;
    @JsonView(Views.Update.class)
    private String emailUsername;
    @JsonView(Views.Update.class)
    private String emailPassword;
    @JsonView(Views.Update.class)
    private String emailFrom;
    @JsonView(Views.Update.class)
    private String emailFromName;

    // 通用
    @JsonView(Views.Update.class)
    private Integer codeLength;
    @JsonView(Views.Update.class)
    private Integer expireMinutes;
    @JsonView(Views.Update.class)
    private Integer dailyLimit;
}
```

**Step 3: 创建 CaptchaConfigService**

```java
package com.devlovecode.aiperm.modules.captcha.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.captcha.dto.CaptchaConfigDTO;
import com.devlovecode.aiperm.modules.captcha.entity.SysCaptchaConfig;
import com.devlovecode.aiperm.modules.captcha.repository.CaptchaConfigRepository;
import com.devlovecode.aiperm.modules.captcha.vo.CaptchaConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CaptchaConfigService {

    private final CaptchaConfigRepository captchaConfigRepo;

    /** 获取配置（密钥脱敏） */
    public CaptchaConfigVO getConfig(String type) {
        SysCaptchaConfig config = captchaConfigRepo.findByType(type.toUpperCase())
                .orElseThrow(() -> new BusinessException("配置不存在：" + type));
        return toVO(config);
    }

    /** 更新配置 */
    @Transactional
    public void updateConfig(String type, CaptchaConfigDTO dto) {
        SysCaptchaConfig config = captchaConfigRepo.findByType(type.toUpperCase())
                .orElseThrow(() -> new BusinessException("配置不存在：" + type));

        config.setEnabled(dto.getEnabled());
        config.setSmsProvider(dto.getSmsProvider());

        // 密钥：空值代表不更新（前端脱敏显示，不传空值）
        if (dto.getSmsAccessKey() != null && !dto.getSmsAccessKey().contains("****")) {
            config.setSmsAccessKey(dto.getSmsAccessKey());
        }
        if (dto.getSmsSecretKey() != null && !dto.getSmsSecretKey().isBlank()) {
            config.setSmsSecretKey(dto.getSmsSecretKey());
        }
        config.setSmsSignName(dto.getSmsSignName());
        config.setSmsTemplateCode(dto.getSmsTemplateCode());
        config.setEmailHost(dto.getEmailHost());
        config.setEmailPort(dto.getEmailPort());
        config.setEmailUsername(dto.getEmailUsername());
        if (dto.getEmailPassword() != null && !dto.getEmailPassword().isBlank()) {
            config.setEmailPassword(dto.getEmailPassword());
        }
        config.setEmailFrom(dto.getEmailFrom());
        config.setEmailFromName(dto.getEmailFromName());
        config.setCodeLength(dto.getCodeLength());
        config.setExpireMinutes(dto.getExpireMinutes());
        config.setDailyLimit(dto.getDailyLimit());
        config.setUpdateBy(StpUtil.getLoginIdAsString());

        captchaConfigRepo.update(config);
    }

    private CaptchaConfigVO toVO(SysCaptchaConfig entity) {
        CaptchaConfigVO vo = new CaptchaConfigVO();
        vo.setId(entity.getId());
        vo.setType(entity.getType());
        vo.setEnabled(entity.getEnabled());
        vo.setSmsProvider(entity.getSmsProvider());
        // 密钥脱敏
        vo.setSmsAccessKey(desensitize(entity.getSmsAccessKey()));
        vo.setSmsSignName(entity.getSmsSignName());
        vo.setSmsTemplateCode(entity.getSmsTemplateCode());
        vo.setEmailHost(entity.getEmailHost());
        vo.setEmailPort(entity.getEmailPort());
        vo.setEmailUsername(entity.getEmailUsername());
        vo.setEmailFrom(entity.getEmailFrom());
        vo.setEmailFromName(entity.getEmailFromName());
        vo.setCodeLength(entity.getCodeLength());
        vo.setExpireMinutes(entity.getExpireMinutes());
        vo.setDailyLimit(entity.getDailyLimit());
        return vo;
    }

    private String desensitize(String value) {
        if (value == null || value.length() <= 4) return value;
        return value.substring(0, 4) + "****";
    }
}
```

**Step 4: 创建 CaptchaConfigController**

```java
package com.devlovecode.aiperm.modules.captcha.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.captcha.dto.CaptchaConfigDTO;
import com.devlovecode.aiperm.modules.captcha.service.CaptchaConfigService;
import com.devlovecode.aiperm.modules.captcha.vo.CaptchaConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "验证码配置管理")
@RestController
@RequestMapping("/system/captcha-config")
@SaCheckLogin
@RequiredArgsConstructor
public class CaptchaConfigController {

    private final CaptchaConfigService captchaConfigService;

    @Operation(summary = "获取验证码配置（SMS或EMAIL）")
    @SaCheckPermission("system:captcha:config")
    @GetMapping("/{type}")
    public R<CaptchaConfigVO> getConfig(@PathVariable String type) {
        return R.ok(captchaConfigService.getConfig(type));
    }

    @Operation(summary = "更新验证码配置")
    @SaCheckPermission("system:captcha:config")
    @Log(title = "验证码配置", operType = OperType.UPDATE)
    @PutMapping("/{type}")
    public R<Void> updateConfig(
            @PathVariable String type,
            @RequestBody @Validated({jakarta.validation.groups.Default.class, Views.Update.class}) CaptchaConfigDTO dto) {
        captchaConfigService.updateConfig(type, dto);
        return R.ok();
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
git add backend/src/main/java/com/devlovecode/aiperm/modules/captcha/
git commit -m "feat(config): add CaptchaConfig management API with key desensitization"
```

---

## Task 28: 2FA 策略配置后端接口

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/mfa/service/MfaPolicyService.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/mfa/controller/MfaPolicyController.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/mfa/dto/MfaPolicyDTO.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/mfa/vo/MfaPolicyVO.java`

**Step 1: 创建 MfaPolicyVO 和 MfaPolicyDTO**

```java
// MfaPolicyVO.java
package com.devlovecode.aiperm.modules.mfa.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MfaPolicyVO {
    private Long id;
    private String name;
    private String permPattern;
    private String apiPattern;
    private Integer enabled;
    private LocalDateTime createTime;
}
```

```java
// MfaPolicyDTO.java
package com.devlovecode.aiperm.modules.mfa.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MfaPolicyDTO {

    @JsonView(Views.Query.class)
    private Integer page = 1;

    @JsonView(Views.Query.class)
    private Integer pageSize = 10;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @NotBlank(message = "策略名称不能为空", groups = {Views.Create.class, Views.Update.class})
    private String name;

    @JsonView({Views.Create.class, Views.Update.class})
    private String permPattern;

    @JsonView({Views.Create.class, Views.Update.class})
    private String apiPattern;

    @JsonView({Views.Create.class, Views.Update.class})
    private Integer enabled;
}
```

**Step 2: 创建 MfaPolicyService**

```java
package com.devlovecode.aiperm.modules.mfa.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.mfa.dto.MfaPolicyDTO;
import com.devlovecode.aiperm.modules.mfa.entity.SysMfaPolicy;
import com.devlovecode.aiperm.modules.mfa.repository.MfaPolicyRepository;
import com.devlovecode.aiperm.modules.mfa.vo.MfaPolicyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MfaPolicyService {

    private final MfaPolicyRepository mfaPolicyRepo;

    public List<MfaPolicyVO> listAll() {
        return mfaPolicyRepo.findAll().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Transactional
    public void create(MfaPolicyDTO dto) {
        SysMfaPolicy policy = new SysMfaPolicy();
        policy.setName(dto.getName());
        policy.setPermPattern(dto.getPermPattern());
        policy.setApiPattern(dto.getApiPattern());
        policy.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : 1);
        policy.setCreateBy(StpUtil.getLoginIdAsString());
        mfaPolicyRepo.insert(policy);
    }

    @Transactional
    public void update(Long id, MfaPolicyDTO dto) {
        SysMfaPolicy policy = mfaPolicyRepo.findById(id)
                .orElseThrow(() -> new BusinessException("策略不存在"));
        policy.setName(dto.getName());
        policy.setPermPattern(dto.getPermPattern());
        policy.setApiPattern(dto.getApiPattern());
        policy.setEnabled(dto.getEnabled());
        policy.setUpdateBy(StpUtil.getLoginIdAsString());
        mfaPolicyRepo.update(policy);
    }

    @Transactional
    public void delete(Long id) {
        mfaPolicyRepo.deleteById(id);
    }

    private MfaPolicyVO toVO(SysMfaPolicy entity) {
        MfaPolicyVO vo = new MfaPolicyVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setPermPattern(entity.getPermPattern());
        vo.setApiPattern(entity.getApiPattern());
        vo.setEnabled(entity.getEnabled());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}
```

**Step 3: 创建 MfaPolicyController**

```java
package com.devlovecode.aiperm.modules.mfa.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.mfa.dto.MfaPolicyDTO;
import com.devlovecode.aiperm.modules.mfa.service.MfaPolicyService;
import com.devlovecode.aiperm.modules.mfa.vo.MfaPolicyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "2FA策略管理")
@RestController
@RequestMapping("/system/mfa-policy")
@SaCheckLogin
@RequiredArgsConstructor
public class MfaPolicyController {

    private final MfaPolicyService mfaPolicyService;

    @Operation(summary = "查询所有2FA策略")
    @SaCheckPermission("system:mfa:policy")
    @GetMapping
    public R<List<MfaPolicyVO>> list() {
        return R.ok(mfaPolicyService.listAll());
    }

    @Operation(summary = "创建2FA策略")
    @SaCheckPermission("system:mfa:policy")
    @Log(title = "2FA策略管理", operType = OperType.CREATE)
    @PostMapping
    public R<Void> create(@RequestBody @Validated({jakarta.validation.groups.Default.class, Views.Create.class}) MfaPolicyDTO dto) {
        mfaPolicyService.create(dto);
        return R.ok();
    }

    @Operation(summary = "更新2FA策略")
    @SaCheckPermission("system:mfa:policy")
    @Log(title = "2FA策略管理", operType = OperType.UPDATE)
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id,
                          @RequestBody @Validated({jakarta.validation.groups.Default.class, Views.Update.class}) MfaPolicyDTO dto) {
        mfaPolicyService.update(id, dto);
        return R.ok();
    }

    @Operation(summary = "删除2FA策略")
    @SaCheckPermission("system:mfa:policy")
    @Log(title = "2FA策略管理", operType = OperType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        mfaPolicyService.delete(id);
        return R.ok();
    }
}
```

**Step 4: 编译验证 + Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm/backend && ./gradlew compileJava 2>&1 | tail -5
```

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add backend/src/main/java/com/devlovecode/aiperm/modules/mfa/
git commit -m "feat(config): add MFA policy management CRUD API"
```

---

## Task 29: OAuth 配置管理后端接口

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/oauth/service/OauthConfigService.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/oauth/controller/OauthConfigController.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/oauth/dto/OauthConfigDTO.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/oauth/vo/OauthConfigVO.java`

**Step 1: 创建 OauthConfigVO（脱敏处理）**

```java
package com.devlovecode.aiperm.modules.oauth.vo;

import lombok.Data;

@Data
public class OauthConfigVO {
    private Long id;
    private String platform;
    private Integer enabled;
    private String corpId;
    private String agentId;
    private String appKey;      // 脱敏：前4位+****
    private String callbackUrl;
    private String remark;
}
```

**Step 2: 创建 OauthConfigDTO**

```java
package com.devlovecode.aiperm.modules.oauth.dto;

import lombok.Data;

@Data
public class OauthConfigDTO {
    private Integer enabled;
    private String corpId;
    private String agentId;
    private String appKey;
    private String appSecret;   // 更新时传入，获取时脱敏
    private String callbackUrl;
    private String remark;
}
```

**Step 3: 创建 OauthConfigService**

```java
package com.devlovecode.aiperm.modules.oauth.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.oauth.dto.OauthConfigDTO;
import com.devlovecode.aiperm.modules.oauth.entity.SysOauthConfig;
import com.devlovecode.aiperm.modules.oauth.repository.OauthConfigRepository;
import com.devlovecode.aiperm.modules.oauth.vo.OauthConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OauthConfigService {

    private final OauthConfigRepository oauthConfigRepo;

    public OauthConfigVO getConfig(String platform) {
        SysOauthConfig config = oauthConfigRepo.findByPlatform(platform.toUpperCase())
                .orElseThrow(() -> new BusinessException("平台配置不存在：" + platform));
        return toVO(config);
    }

    @Transactional
    public void updateConfig(String platform, OauthConfigDTO dto) {
        SysOauthConfig config = oauthConfigRepo.findByPlatform(platform.toUpperCase())
                .orElseThrow(() -> new BusinessException("平台配置不存在：" + platform));

        config.setEnabled(dto.getEnabled());
        config.setCorpId(dto.getCorpId());
        config.setAgentId(dto.getAgentId());

        if (dto.getAppKey() != null && !dto.getAppKey().contains("****")) {
            config.setAppKey(dto.getAppKey());
        }
        if (dto.getAppSecret() != null && !dto.getAppSecret().isBlank()) {
            config.setAppSecret(dto.getAppSecret());
        }
        config.setCallbackUrl(dto.getCallbackUrl());
        config.setRemark(dto.getRemark());
        config.setUpdateBy(StpUtil.getLoginIdAsString());

        oauthConfigRepo.update(config);
    }

    private OauthConfigVO toVO(SysOauthConfig entity) {
        OauthConfigVO vo = new OauthConfigVO();
        vo.setId(entity.getId());
        vo.setPlatform(entity.getPlatform());
        vo.setEnabled(entity.getEnabled());
        vo.setCorpId(entity.getCorpId());
        vo.setAgentId(entity.getAgentId());
        vo.setAppKey(desensitize(entity.getAppKey()));
        vo.setCallbackUrl(entity.getCallbackUrl());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    private String desensitize(String value) {
        if (value == null || value.length() <= 4) return value;
        return value.substring(0, 4) + "****";
    }
}
```

**Step 4: 创建 OauthConfigController**

```java
package com.devlovecode.aiperm.modules.oauth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.oauth.dto.OauthConfigDTO;
import com.devlovecode.aiperm.modules.oauth.service.OauthConfigService;
import com.devlovecode.aiperm.modules.oauth.vo.OauthConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "OAuth配置管理")
@RestController
@RequestMapping("/system/oauth-config")
@SaCheckLogin
@RequiredArgsConstructor
public class OauthConfigController {

    private final OauthConfigService oauthConfigService;

    @Operation(summary = "获取OAuth平台配置")
    @SaCheckPermission("system:oauth:config")
    @GetMapping("/{platform}")
    public R<OauthConfigVO> getConfig(@PathVariable String platform) {
        return R.ok(oauthConfigService.getConfig(platform));
    }

    @Operation(summary = "更新OAuth平台配置")
    @SaCheckPermission("system:oauth:config")
    @Log(title = "OAuth配置管理", operType = OperType.UPDATE)
    @PutMapping("/{platform}")
    public R<Void> updateConfig(@PathVariable String platform,
                                @RequestBody OauthConfigDTO dto) {
        oauthConfigService.updateConfig(platform, dto);
        return R.ok();
    }
}
```

**Step 5: 编译验证 + Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm/backend && ./gradlew build -x test 2>&1 | tail -5
```

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add backend/src/main/java/com/devlovecode/aiperm/modules/oauth/
git commit -m "feat(config): add OAuth config management API"
```

---

## Task 30: 前端配置管理 API（aiperm-api-generator 规范）

**Files:**
- Create: `frontend/src/api/system/captchaConfig.ts`
- Create: `frontend/src/api/system/mfaPolicy.ts`
- Create: `frontend/src/api/system/oauthConfig.ts`

**Step 1: 创建 captchaConfig.ts**

```typescript
/**
 * 验证码配置管理 API
 * 对应后端 CaptchaConfigController (/system/captcha-config)
 */
import request from '@/utils/request'

export type CaptchaType = 'SMS' | 'EMAIL'

export interface CaptchaConfigVO {
  id: number
  type: CaptchaType
  enabled: number
  smsProvider?: string
  smsAccessKey?: string
  smsSignName?: string
  smsTemplateCode?: string
  emailHost?: string
  emailPort?: number
  emailUsername?: string
  emailFrom?: string
  emailFromName?: string
  codeLength: number
  expireMinutes: number
  dailyLimit: number
}

export interface CaptchaConfigDTO {
  enabled?: number
  smsProvider?: string
  smsAccessKey?: string
  smsSecretKey?: string
  smsSignName?: string
  smsTemplateCode?: string
  emailHost?: string
  emailPort?: number
  emailUsername?: string
  emailPassword?: string
  emailFrom?: string
  emailFromName?: string
  codeLength?: number
  expireMinutes?: number
  dailyLimit?: number
}

export const captchaConfigApi = {
  getConfig: (type: CaptchaType) =>
    request.get<CaptchaConfigVO>(`/system/captcha-config/${type}`),

  updateConfig: (type: CaptchaType, data: CaptchaConfigDTO) =>
    request.put<void>(`/system/captcha-config/${type}`, data),
}
```

**Step 2: 创建 mfaPolicy.ts**

```typescript
/**
 * 2FA 策略管理 API
 * 对应后端 MfaPolicyController (/system/mfa-policy)
 */
import request from '@/utils/request'

export interface MfaPolicyVO {
  id: number
  name: string
  permPattern: string
  apiPattern: string
  enabled: number
  createTime: string
}

export interface MfaPolicyDTO {
  name: string
  permPattern?: string
  apiPattern?: string
  enabled?: number
}

export const mfaPolicyApi = {
  list: () =>
    request.get<MfaPolicyVO[]>('/system/mfa-policy'),

  create: (data: MfaPolicyDTO) =>
    request.post<void>('/system/mfa-policy', data),

  update: (id: number, data: MfaPolicyDTO) =>
    request.put<void>(`/system/mfa-policy/${id}`, data),

  delete: (id: number) =>
    request.delete<void>(`/system/mfa-policy/${id}`),
}
```

**Step 3: 创建 oauthConfig.ts**

```typescript
/**
 * OAuth 配置管理 API
 * 对应后端 OauthConfigController (/system/oauth-config)
 */
import request from '@/utils/request'

export type OAuthPlatform = 'WEWORK' | 'DINGTALK' | 'FEISHU'

export interface OauthConfigVO {
  id: number
  platform: OAuthPlatform
  enabled: number
  corpId?: string
  agentId?: string
  appKey?: string
  callbackUrl?: string
  remark?: string
}

export interface OauthConfigDTO {
  enabled?: number
  corpId?: string
  agentId?: string
  appKey?: string
  appSecret?: string
  callbackUrl?: string
  remark?: string
}

export const oauthConfigApi = {
  getConfig: (platform: OAuthPlatform) =>
    request.get<OauthConfigVO>(`/system/oauth-config/${platform}`),

  updateConfig: (platform: OAuthPlatform, data: OauthConfigDTO) =>
    request.put<void>(`/system/oauth-config/${platform}`, data),
}
```

**Step 4: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend/src/api/system/captchaConfig.ts
git add frontend/src/api/system/mfaPolicy.ts
git add frontend/src/api/system/oauthConfig.ts
git commit -m "feat(config): add frontend API files for captcha/mfa/oauth config management"
```

---

## Task 31: 前端配置管理页面（短信/邮件配置）

**Files:**
- Create: `frontend/src/views/enterprise/captcha-config/index.vue`

**Step 1: 创建 captcha-config/index.vue**

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElTabs, ElTabPane } from 'element-plus'
import { captchaConfigApi, type CaptchaConfigDTO } from '@/api/system/captchaConfig'

const activeTab = ref<'SMS' | 'EMAIL'>('SMS')

// SMS 表单
const smsForm = ref<CaptchaConfigDTO & { enabled?: number }>({
  enabled: 0,
  smsProvider: 'ALIYUN',
  smsAccessKey: '',
  smsSecretKey: '',
  smsSignName: '',
  smsTemplateCode: '',
  codeLength: 6,
  expireMinutes: 5,
  dailyLimit: 10,
})

// Email 表单
const emailForm = ref<CaptchaConfigDTO & { enabled?: number }>({
  enabled: 0,
  emailHost: '',
  emailPort: 465,
  emailUsername: '',
  emailPassword: '',
  emailFrom: '',
  emailFromName: '',
  codeLength: 6,
  expireMinutes: 5,
  dailyLimit: 10,
})

const loading = ref(false)
const saveLoading = ref(false)

async function loadConfig(type: 'SMS' | 'EMAIL') {
  loading.value = true
  try {
    const data = await captchaConfigApi.getConfig(type)
    if (!data) return
    if (type === 'SMS') {
      Object.assign(smsForm.value, data)
      smsForm.value.smsSecretKey = ''  // 密钥不回显
    } else {
      Object.assign(emailForm.value, data)
      emailForm.value.emailPassword = ''  // 密码不回显
    }
  }
  catch (e: any) {
    ElMessage.error('加载配置失败：' + (e?.message || '未知错误'))
  }
  finally {
    loading.value = false
  }
}

async function saveConfig(type: 'SMS' | 'EMAIL') {
  saveLoading.value = true
  try {
    const dto = type === 'SMS' ? smsForm.value : emailForm.value
    await captchaConfigApi.updateConfig(type, dto)
    ElMessage.success('保存成功')
  }
  catch (e: any) {
    ElMessage.error('保存失败：' + (e?.message || '未知错误'))
  }
  finally {
    saveLoading.value = false
  }
}

onMounted(() => {
  loadConfig('SMS')
  loadConfig('EMAIL')
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>验证码服务配置</h2>
    </div>

    <el-tabs v-model="activeTab">
      <!-- 短信配置 -->
      <el-tab-pane label="短信（SMS）" name="SMS">
        <el-form :model="smsForm" label-width="140px" v-loading="loading">
          <el-form-item label="启用短信服务">
            <el-switch v-model="smsForm.enabled" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="服务商">
            <el-select v-model="smsForm.smsProvider">
              <el-option label="阿里云" value="ALIYUN" />
              <el-option label="腾讯云" value="TENCENT" />
            </el-select>
          </el-form-item>
          <el-form-item label="AccessKey">
            <el-input v-model="smsForm.smsAccessKey" placeholder="前4位+****（修改时重新输入完整值）" />
          </el-form-item>
          <el-form-item label="SecretKey">
            <el-input v-model="smsForm.smsSecretKey" type="password" placeholder="修改时输入新值，不修改留空" />
          </el-form-item>
          <el-form-item label="签名名称">
            <el-input v-model="smsForm.smsSignName" />
          </el-form-item>
          <el-form-item label="模板ID">
            <el-input v-model="smsForm.smsTemplateCode" />
          </el-form-item>
          <el-form-item label="验证码长度">
            <el-input-number v-model="smsForm.codeLength" :min="4" :max="8" />
          </el-form-item>
          <el-form-item label="过期时间（分钟）">
            <el-input-number v-model="smsForm.expireMinutes" :min="1" :max="30" />
          </el-form-item>
          <el-form-item label="每日发送上限">
            <el-input-number v-model="smsForm.dailyLimit" :min="1" :max="100" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saveLoading" @click="saveConfig('SMS')">
              保存配置
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 邮件配置 -->
      <el-tab-pane label="邮件（EMAIL）" name="EMAIL">
        <el-form :model="emailForm" label-width="140px" v-loading="loading">
          <el-form-item label="启用邮件服务">
            <el-switch v-model="emailForm.enabled" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="SMTP 服务器">
            <el-input v-model="emailForm.emailHost" placeholder="smtp.example.com" />
          </el-form-item>
          <el-form-item label="SMTP 端口">
            <el-input-number v-model="emailForm.emailPort" :min="1" :max="65535" />
          </el-form-item>
          <el-form-item label="邮箱账号">
            <el-input v-model="emailForm.emailUsername" />
          </el-form-item>
          <el-form-item label="密码/授权码">
            <el-input v-model="emailForm.emailPassword" type="password" placeholder="修改时输入新值，不修改留空" />
          </el-form-item>
          <el-form-item label="发件人地址">
            <el-input v-model="emailForm.emailFrom" />
          </el-form-item>
          <el-form-item label="发件人名称">
            <el-input v-model="emailForm.emailFromName" placeholder="AIPerm" />
          </el-form-item>
          <el-form-item label="验证码长度">
            <el-input-number v-model="emailForm.codeLength" :min="4" :max="8" />
          </el-form-item>
          <el-form-item label="过期时间（分钟）">
            <el-input-number v-model="emailForm.expireMinutes" :min="1" :max="30" />
          </el-form-item>
          <el-form-item label="每日发送上限">
            <el-input-number v-model="emailForm.dailyLimit" :min="1" :max="100" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saveLoading" @click="saveConfig('EMAIL')">
              保存配置
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.page-container { padding: 24px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 18px; font-weight: 600; color: #1e293b; }
</style>
```

**Step 2: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend/src/views/enterprise/captcha-config/
git commit -m "feat(config): add SMS/Email captcha config management page"
```

---

## Task 32: 前端 2FA 策略配置页面

**Files:**
- Create: `frontend/src/views/enterprise/mfa-policy/index.vue`

**Step 1: 创建 mfa-policy/index.vue**

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { mfaPolicyApi, type MfaPolicyVO, type MfaPolicyDTO } from '@/api/system/mfaPolicy'

const tableData = ref<MfaPolicyVO[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saveLoading = ref(false)

const form = ref<MfaPolicyDTO>({
  name: '',
  permPattern: '',
  apiPattern: '',
  enabled: 1,
})

async function loadData() {
  loading.value = true
  try {
    const data = await mfaPolicyApi.list()
    tableData.value = data || []
  }
  finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.value = { name: '', permPattern: '', apiPattern: '', enabled: 1 }
  dialogVisible.value = true
}

function openEdit(row: MfaPolicyVO) {
  editingId.value = row.id
  form.value = {
    name: row.name,
    permPattern: row.permPattern,
    apiPattern: row.apiPattern,
    enabled: row.enabled,
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.name) {
    ElMessage.warning('策略名称不能为空')
    return
  }
  saveLoading.value = true
  try {
    if (editingId.value) {
      await mfaPolicyApi.update(editingId.value, form.value)
    } else {
      await mfaPolicyApi.create(form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  }
  catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  }
  finally {
    saveLoading.value = false
  }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确认删除此策略？', '提示', { type: 'warning' })
  try {
    await mfaPolicyApi.delete(id)
    ElMessage.success('删除成功')
    loadData()
  }
  catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>2FA 策略配置</h2>
      <el-button type="primary" @click="openCreate">新增策略</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border>
      <el-table-column prop="name" label="策略名称" />
      <el-table-column prop="permPattern" label="权限标识匹配" />
      <el-table-column prop="apiPattern" label="API路径匹配" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'">
            {{ row.enabled ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑策略' : '新增策略'" width="500px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="策略名称" required>
          <el-input v-model="form.name" placeholder="如：修改密码" />
        </el-form-item>
        <el-form-item label="权限标识匹配">
          <el-input v-model="form.permPattern" placeholder="如：system:user:delete" />
          <div class="form-tip">支持通配符 *，如：system:user:*</div>
        </el-form-item>
        <el-form-item label="API路径匹配">
          <el-input v-model="form.apiPattern" placeholder="如：/system/user/*" />
          <div class="form-tip">支持通配符 *，如：/system/user/*</div>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container { padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 18px; font-weight: 600; color: #1e293b; }
.form-tip { font-size: 12px; color: #94a3b8; margin-top: 4px; }
</style>
```

**Step 2: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend/src/views/enterprise/mfa-policy/
git commit -m "feat(config): add MFA policy management page"
```

---

## Phase 6 完成验收

```bash
# 后端全量编译
cd /Users/shichenyang/IdeaProjects/aiperm/backend && ./gradlew build -x test 2>&1 | tail -5

# 前端 TypeScript 检查
cd /Users/shichenyang/IdeaProjects/aiperm/frontend && npx tsc --noEmit 2>&1 | wc -l
```

期望：后端 `BUILD SUCCESSFUL`，前端 TypeScript 错误 0。

---

## 全部计划完成总结

完成所有 Phase 后，以下功能已实现：

| 功能 | 状态 | 说明 |
|------|------|------|
| 短信验证码发送 | ✅ | 含限流、每日上限 |
| 邮件验证码发送 | ✅ | 动态 SMTP 配置 |
| 手机号登录 | ✅ | 策略模式，可扩展 |
| 邮箱登录 | ✅ | 策略模式，可扩展 |
| TOTP 2FA 绑定 | ✅ | 超管强制绑定 |
| TOTP 2FA 验证 | ✅ | Redis 30分钟有效期 |
| 2FA 拦截器 | ✅ | 通配符 API 路径匹配 |
| 前端 Tab 登录页 | ✅ | 动态显示可用登录方式 |
| 前端 2FA 弹窗 | ✅ | 绑定 + 验证双弹窗 |
| OAuth 框架 | ✅ | 抽象基类，可扩展 |
| 企业微信 OAuth | ⚠️ | 需要实际 API 实现 |
| 配置管理后端 | ✅ | 短信/邮件/2FA/OAuth |
| 配置管理前端 | ✅ | 短信/邮件/2FA策略页面 |
