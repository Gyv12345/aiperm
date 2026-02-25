---
name: sa-token-springboot
description: Sa-Token 是一个轻量级 Java 权限认证框架，提供登录认证、权限认证、单点登录、OAuth2.0 等功能。当用户需要：(1) 在 Spring Boot 项目中集成 Sa-Token 进行权限认证；(2) 实现登录/登出、权限校验、角色校验；(3) 配置 Sa-Token 的 token 有效期、并发登录等参数；(4) 集成 Redis 进行会话持久化；(5) 使用注解鉴权或路由拦截鉴权；(6) 实现前后端分离模式的无 Cookie 认证时使用此技能。
---

# Sa-Token Spring Boot 集成指南

Sa-Token 是一个轻量级 Java 权限认证框架，主要解决：登录认证、权限认证、Session会话、单点登录、OAuth2.0、微服务鉴权等问题。

## 快速开始

### 1. 添加依赖

**Spring Boot 2.x:**
```xml
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-spring-boot-starter</artifactId>
    <version>1.44.0</version>
</dependency>
```

**Spring Boot 3.x:**
```xml
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-spring-boot3-starter</artifactId>
    <version>1.44.0</version>
</dependency>
```

### 2. 基础配置 (application.yml)

```yaml
sa-token:
  token-name: satoken           # token 名称（同时也是 cookie 名称）
  timeout: 2592000              # token 有效期（单位：秒），默认30天，-1 代表永久有效
  active-timeout: -1            # token 最低活跃频率（单位：秒），-1 代表不限制
  is-concurrent: true           # 是否允许同一账号多地同时登录
  is-share: false               # 多人登录同一账号时，是否共用一个 token
  token-style: uuid             # token 风格
  is-log: true                  # 是否输出操作日志
```

### 3. 登录认证

```java
// 登录
StpUtil.login(10001);           // 参数为账号id

// 判断是否登录
StpUtil.isLogin();              // 返回 true/false

// 校验登录（未登录抛出异常）
StpUtil.checkLogin();

// 获取登录账号id
StpUtil.getLoginId();
StpUtil.getLoginIdAsLong();
StpUtil.getLoginIdAsString();

// 注销登录
StpUtil.logout();
```

### 4. 注册拦截器（路由鉴权）

```java
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns("/user/doLogin");
    }
}
```

---

## 核心功能

### 登录认证

```java
@RestController
@RequestMapping("/user/")
public class LoginController {

    @RequestMapping("doLogin")
    public SaResult doLogin(String username, String password) {
        if("zhang".equals(username) && "123456".equals(password)) {
            StpUtil.login(10001);
            return SaResult.ok("登录成功");
        }
        return SaResult.error("登录失败");
    }

    @RequestMapping("isLogin")
    public SaResult isLogin() {
        return SaResult.ok("是否登录：" + StpUtil.isLogin());
    }

    @RequestMapping("logout")
    public SaResult logout() {
        StpUtil.logout();
        return SaResult.ok();
    }
}
```

### 权限认证

**1. 实现 StpInterface 接口：**

```java
@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> list = new ArrayList<>();
        list.add("user.add");
        list.add("user.update");
        list.add("user.get");
        return list;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> list = new ArrayList<>();
        list.add("admin");
        return list;
    }
}
```

**2. 权限校验 API：**

```java
// 判断权限
StpUtil.hasPermission("user.add");              // true/false
StpUtil.checkPermission("user.add");            // 校验，失败抛异常
StpUtil.checkPermissionAnd("a", "b");           // 必须全部拥有
StpUtil.checkPermissionOr("a", "b");            // 拥有其一即可

// 判断角色
StpUtil.hasRole("admin");                       // true/false
StpUtil.checkRole("admin");                     // 校验，失败抛异常
StpUtil.checkRoleAnd("admin", "manager");       // 必须全部拥有
StpUtil.checkRoleOr("admin", "manager");        // 拥有其一即可
```

### 注解鉴权

**注册拦截器后使用：**

```java
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
    }
}
```

**可用注解：**

| 注解 | 说明 |
|------|------|
| `@SaCheckLogin` | 登录校验 |
| `@SaCheckRole("admin")` | 角色校验 |
| `@SaCheckPermission("user:add")` | 权限校验 |
| `@SaCheckSafe` | 二级认证校验 |
| `@SaIgnore` | 忽略校验 |

**使用示例：**

```java
@SaCheckLogin
@RequestMapping("info")
public String info() {
    return "查询用户信息";
}

@SaCheckPermission("user-add")
@RequestMapping("add")
public String add() {
    return "用户增加";
}

@SaCheckPermission(value = {"user-add", "user-all"}, mode = SaMode.OR)
@RequestMapping("addOr")
public String addOr() {
    return "用户增加";
}
```

### 路由拦截鉴权

```java
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
            // 登录校验
            SaRouter.match("/**", "/user/doLogin", r -> StpUtil.checkLogin());

            // 角色校验
            SaRouter.match("/admin/**", r -> StpUtil.checkRoleOr("admin", "super-admin"));

            // 权限校验
            SaRouter.match("/user/**", r -> StpUtil.checkPermission("user"));
            SaRouter.match("/goods/**", r -> StpUtil.checkPermission("goods"));
        })).addPathPatterns("/**");
    }
}
```

### Session 会话

```java
// Account-Session（账号级别）
StpUtil.getSession().set("user", user);
SysUser user = (SysUser) StpUtil.getSession().get("user");

// Token-Session（令牌级别）
StpUtil.getTokenSession().set("device", "web");

// Custom-Session（自定义）
SaSessionCustomUtil.getSessionById("goods-10001").set("stock", 100);
```

### 前后端分离（无 Cookie 模式）

**后端返回 Token：**

```java
@RequestMapping("doLogin")
public SaResult doLogin() {
    StpUtil.login(10001);
    SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
    return SaResult.data(tokenInfo);  // 返回 tokenName 和 tokenValue
}
```

**前端提交 Token（Header 方式）：**

```javascript
// 将 token 塞到请求 header 里
fetch('/api/user/info', {
    headers: {
        'satoken': 'your-token-value'
    }
});
```

---

## 配置项详解

### 核心模块配置

| 参数名称 | 类型 | 默认值 | 说明 |
|---------|------|--------|------|
| tokenName | String | satoken | Token 名称（同时也是 Cookie 名称、数据持久化前缀） |
| timeout | long | 2592000 | Token 有效期（单位：秒），默认30天，-1代表永不过期 |
| activeTimeout | long | -1 | Token 最低活跃频率（单位：秒），超时冻结，-1代表不限制 |
| dynamicActiveTimeout | Boolean | false | 是否启用动态 activeTimeout 功能 |
| isConcurrent | Boolean | true | 是否允许同一账号并发登录（true=允许，false=新登录挤掉旧登录） |
| isShare | Boolean | false | 多人登录同一账号时，是否共用一个 token |
| replacedRange | SaReplacedRange | CURR_DEVICE_TYPE | isConcurrent=false 时顶人下线的范围（CURR_DEVICE_TYPE/ALL_DEVICE_TYPE） |
| maxLoginCount | int | 12 | 同一账号最大登录数量，-1代表不限（需 isConcurrent=true, isShare=false） |
| overflowLogoutMode | SaLogoutMode | LOGOUT | 溢出 maxLoginCount 的客户端注销方式（LOGOUT/KICKOUT/REPLACED） |
| maxTryTimes | int | 12 | 创建 Token 时的最高循环次数，-1=不循环重试 |
| isReadBody | Boolean | true | 是否尝试从请求体里读取 Token |
| isReadHeader | Boolean | true | 是否尝试从 header 里读取 Token |
| isReadCookie | Boolean | true | 是否尝试从 cookie 里读取 Token |
| isLastingCookie | Boolean | true | 是否为持久 Cookie（临时 Cookie 在浏览器关闭时自动删除） |
| isWriteHeader | Boolean | false | 是否在登录后将 Token 写入到响应头 |
| logoutRange | SaLogoutRange | TOKEN | 注销范围（TOKEN=只注销当前token，ACCOUNT=注销所有客户端会话） |
| isLogoutKeepFreezeOps | Boolean | false | 如果 token 已被冻结，是否保留其操作权 |
| isLogoutKeepTokenSession | Boolean | false | 在注销 token 后，是否保留其对应的 Token-Session |
| rightNowCreateTokenSession | Boolean | false | 在登录时是否立即创建 Token-Session |
| tokenStyle | String | uuid | token 风格（uuid/simple-uuid/random-32/random-64/random-128/tik） |
| dataRefreshPeriod | int | 30 | 清理过期数据间隔时间（单位: 秒），-1代表不启动定时清理 |
| tokenSessionCheckLogin | Boolean | true | 获取 Token-Session 时是否必须登录 |
| autoRenew | Boolean | true | 是否打开自动续签 |
| tokenPrefix | String | null | token 前缀，例如填写 `Bearer` |
| cookieAutoFillPrefix | Boolean | false | cookie 模式是否自动填充 token 提交前缀 |
| isPrint | Boolean | true | 是否在初始化配置时打印版本字符画 |
| isLog | Boolean | false | 是否打印操作日志 |
| logLevel | String | trace | 日志等级（trace/debug/info/warn/error/fatal） |
| logLevelInt | int | 1 | 日志等级 int 值（1-6） |
| isColorLog | Boolean | null | 是否打印彩色日志（null=自动判断） |
| jwtSecretKey | String | null | jwt 秘钥（需集成 sa-token-temp-jwt 模块） |
| sameTokenTimeout | long | 86400 | Same-Token 的有效期（单位: 秒） |
| basic | String | "" | Http Basic 认证的账号和密码 |
| currDomain | String | null | 配置当前项目的网络访问地址 |
| checkSameToken | Boolean | false | 是否校验 Same-Token（部分 rpc 插件有效） |

### Cookie 配置

| 参数名称 | 类型 | 默认值 | 说明 |
|---------|------|--------|------|
| domain | String | null | 作用域（写入 Cookie 时显式指定的作用域） |
| path | String | / | 路径，默认写在域名根路径下 |
| secure | Boolean | false | 是否只在 https 协议下有效 |
| httpOnly | Boolean | false | 是否禁止 js 操作 Cookie |
| sameSite | String | Lax | 第三方限制级别（Strict/Lax/None） |
| extraAttrs | Map | {} | 额外扩展属性 |

```yaml
sa-token:
  cookie:
    domain: stp.com
    path: /
    secure: false
    httpOnly: true
    sameSite: Lax
    extraAttrs:
      Priority: Medium
      Partitioned: ""
```

### Sign 参数签名配置

| 参数名称 | 类型 | 默认值 | 说明 |
|---------|------|--------|------|
| secretKey | String | null | API 调用签名秘钥 |
| timestampDisparity | long | 900000 | 时间戳允许的差距（单位：ms），-1 代表不校验，默认15分钟 |
| digestAlgo | String | md5 | 对 fullStr 的摘要算法 |

```yaml
sa-token:
  sign:
    secret-key: kQwIOrYvnXmSDkwEiFngrKidMcdrgKor
```

### API Key 配置

| 参数名称 | 类型 | 默认值 | 说明 |
|---------|------|--------|------|
| prefix | String | AK- | API Key 前缀 |
| timeout | long | 2592000 | API Key 有效期，-1=永久有效，默认30天 |
| isRecordIndex | Boolean | true | 框架是否记录索引信息 |

```yaml
sa-token:
  api-key:
    prefix: AK-
    timeout: 2592000
    is-record-index: true
```

### SSO-Server 端配置

| 参数名称 | 类型 | 默认值 | 说明 |
|---------|------|--------|------|
| mode | String | - | 指定当前系统集成 SSO 时使用的模式（约定型配置项） |
| ticketTimeout | long | 300 | ticket 有效期（单位: 秒） |
| homeRoute | String | - | 主页路由：在 /sso/auth 登录页不指定 redirect 时默认跳转的地址 |
| isSlo | Boolean | true | 是否打开单点注销功能 |
| autoRenewTimeout | Boolean | false | 是否在每次下发 ticket 时自动续期 token 的有效期 |
| maxRegClient | int | 32 | 在 Access-Session 上记录 Client 信息的最高数量（-1=无限） |
| isCheckSign | Boolean | true | 是否校验参数签名（生产环境务必为true） |
| clients | Map | {} | 以 Map<String, SaSsoClientModel> 格式配置 Client 列表 |
| allowAnonClient | Boolean | false | 是否允许匿名 Client 接入 |
| allowUrl | String | - | 所有允许的授权回调地址，多个用逗号隔开 |
| secretKey | String | - | API 调用签名秘钥 |

```yaml
sa-token:
  sso-server:
    ticket-timeout: 300
    home-route: /home
    is-slo: true
    clients:
      sso-client1:
        client: sso-client1
        allow-url: "*"
        secret-key: SSO-C1-xxx
```

### SSO-Client 端配置

| 参数名称 | 类型 | 默认值 | 说明 |
|---------|------|--------|------|
| mode | String | - | 指定当前系统集成 SSO 时使用的模式（约定型配置项） |
| client | String | "" | 当前 Client 名称标识，用于和 ticket 码的互相锁定 |
| serverUrl | String | null | Server 端主机总地址 |
| authUrl | String | /sso/auth | Server 端单点登录授权地址 |
| signoutUrl | String | /sso/signout | Server 端单点注销地址 |
| pushUrl | String | /sso/pushS | Server 端的推送消息地址 |
| getDataUrl | String | /sso/getData | Server 端的拉取数据地址 |
| currSsoLogin | String | null | 当前 Client 端的登录地址（为空时自动获取） |
| currSsoLogoutCall | String | null | 当前 Client 端的单点注销回调 URL |
| isHttp | Boolean | false | 是否打开模式三（使用 http 请求校验 ticket） |
| isSlo | Boolean | true | 是否打开单点注销功能 |
| regLogoutCall | Boolean | false | 是否注册单点登录注销回调 |
| secretKey | String | "" | API 调用签名秘钥 |
| isCheckSign | Boolean | true | 是否校验参数签名 |

```yaml
sa-token:
  sso-client:
    server-url: http://sa-sso-server.com:9000
    is-slo: true
```

### OAuth2-Server 配置

| 参数名称 | 类型 | 默认值 | 说明 |
|---------|------|--------|------|
| enableAuthorizationCode | Boolean | true | 是否打开模式：授权码（Authorization Code） |
| enableImplicit | Boolean | true | 是否打开模式：隐藏式（Implicit） |
| enablePassword | Boolean | true | 是否打开模式：密码式（Password） |
| enableClientCredentials | Boolean | true | 是否打开模式：凭证式（Client Credentials） |
| codeTimeout | long | 300 | Code 授权码保存的时间（单位：秒） |
| accessTokenTimeout | long | 7200 | Access-Token 保存的时间（单位：秒） |
| refreshTokenTimeout | long | 2592000 | Refresh-Token 保存的时间（单位：秒） |
| clientTokenTimeout | long | 7200 | Client-Token 保存的时间（单位：秒） |
| maxAccessTokenCount | int | 12 | 单个应用单个用户最多同时存在的 Access-Token 数量 |
| maxRefreshTokenCount | int | 12 | 单个应用单个用户最多同时存在的 Refresh-Token 数量 |
| maxClientTokenCount | int | 12 | 单个应用最多同时存在的 Client-Token 数量 |
| isNewRefresh | Boolean | false | 是否在每次 Refresh-Token 刷新 Access-Token 时产生新的 Refresh-Token |
| openidDigestPrefix | String | openid_default_digest_prefix | 默认 openid 生成算法中使用的摘要前缀 |
| unionidDigestPrefix | String | unionid_default_digest_prefix | 默认 unionid 生成算法中使用的摘要前缀 |
| higherScope | String | - | 指定高级权限，多个用逗号隔开 |
| lowerScope | String | - | 指定低级权限，多个用逗号隔开 |
| mode4ReturnAccessToken | Boolean | false | 模式4是否返回 AccessToken 字段 |
| hideStatusField | Boolean | false | 是否在返回值中隐藏默认的状态字段 |

```yaml
sa-token:
  oauth2-server:
    enable-authorization-code: true
    enable-implicit: true
    enable-password: true
    enable-client-credentials: true
    access-token-timeout: 7200
    refresh-token-timeout: 2592000
```

### OIDC 配置

| 参数名称 | 类型 | 默认值 | 说明 |
|---------|------|--------|------|
| iss | String | - | iss 值，如不配置则自动计算 |
| idTokenTimeout | long | 600 | idToken 有效期（单位秒） |

```yaml
sa-token:
  oauth2-server:
    oidc:
      iss: xxx
      idTokenTimeout: 600
```

### 代码配置方式

**方式1：覆盖配置**
```java
@Configuration
public class SaTokenConfigure {
    @Bean
    @Primary
    public SaTokenConfig getSaTokenConfigPrimary() {
        SaTokenConfig config = new SaTokenConfig();
        config.setTokenName("satoken");
        config.setTimeout(30 * 24 * 60 * 60);
        config.setActiveTimeout(-1);
        config.setIsConcurrent(true);
        config.setIsShare(false);
        config.setTokenStyle("uuid");
        config.setIsLog(false);
        return config;
    }
}
```

**方式2：合并配置（代码配置优先）**
```java
@Configuration
public class SaTokenConfigure {
    @Autowired
    public void configSaToken(SaTokenConfig config) {
        config.setTokenName("satoken");
        config.setTimeout(30 * 24 * 60 * 60);
    }
}
```

---

## Redis 集成

### 添加依赖

```xml
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-redis-template</artifactId>
    <version>1.44.0</version>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
```

### Redis 配置

**Spring Boot 2.x:**
```yaml
spring:
  redis:
    database: 1
    host: 127.0.0.1
    port: 6379
    timeout: 10s
    lettuce:
      pool:
        max-active: 200
        max-idle: 10
```

**Spring Boot 3.x:** 将 `spring.redis` 改为 `spring.data.redis`

---

## 异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public SaResult handleNotLogin(NotLoginException e) {
        return SaResult.error("未登录：" + e.getMessage());
    }

    @ExceptionHandler(NotPermissionException.class)
    public SaResult handleNotPermission(NotPermissionException e) {
        return SaResult.error("无权限：" + e.getPermission());
    }

    @ExceptionHandler(NotRoleException.class)
    public SaResult handleNotRole(NotRoleException e) {
        return SaResult.error("无角色：" + e.getRole());
    }
}
```

---

## 常用 API 速查

| API | 说明 |
|-----|------|
| `StpUtil.login(id)` | 登录 |
| `StpUtil.logout()` | 注销 |
| `StpUtil.isLogin()` | 是否登录 |
| `StpUtil.getLoginId()` | 获取账号id |
| `StpUtil.getTokenValue()` | 获取 token |
| `StpUtil.getSession()` | 获取 Session |
| `StpUtil.hasPermission(p)` | 是否有权限 |
| `StpUtil.hasRole(r)` | 是否有角色 |
| `StpUtil.kickout(id)` | 踢人下线 |
| `StpUtil.disable(id, time)` | 封禁账号 |

---

## 参考链接

- 官方文档：https://sa-token.cc
- GitHub：https://github.com/dromara/sa-token
