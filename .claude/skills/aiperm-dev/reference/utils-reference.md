## 工具类使用参考

### Hutool 工具类

#### ObjectUtil - 对象判空
```java
import cn.hutool.core.util.ObjectUtil;

// 判断对象是否为空
boolean isEmpty = ObjectUtil.isEmpty(obj);

// 判断对象是否不为空
boolean isNotEmpty = ObjectUtil.isNotEmpty(obj);

// 获取默认值
String value = ObjectUtil.defaultIfNull(str, "default");
```

#### StrUtil - 字符串处理
```java
import cn.hutool.core.util.StrUtil;

// 判断字符串是否为空
boolean isEmpty = StrUtil.isEmpty(str);

// 去除前后空格
String trimmed = StrUtil.trim(str);

// 字符串格式化
String formatted = StrUtil.format("Hello {}", "World");
```

#### CollUtil - 集合操作
```java
import cn.hutool.core.collection.CollUtil;

// 判断集合是否为空
boolean isEmpty = CollUtil.isEmpty(list);

// 创建集合
List<String> list = CollUtil.newArrayList("a", "b", "c");
```

#### DateUtil - 日期时间处理
```java
import cn.hutool.core.date.DateUtil;

// 获取当前时间
Date now = DateUtil.date();

// 日期格式化
String formatted = DateUtil.format(now, "yyyy-MM-dd HH:mm:ss");
```

#### BeanUtil - 对象复制转换
```java
import cn.hutool.core.bean.BeanUtil;

// 对象复制
DestType dest = BeanUtil.copyProperties(source, DestType.class);

// List 复制
List<DestType> destList = BeanUtil.copyToList(sourceList, DestType.class);
```

### Sa-Token 工具类

#### 获取登录用户信息
```java
import cn.dev33.satoken.stp.StpUtil;

// 获取当前登录用户ID
Long userId = StpUtil.getLoginIdAsLong();

// 获取当前登录用户名
String username = StpUtil.getLoginIdAsString();

// 获取 Token
String token = StpUtil.getTokenValue();
```

### Redis 工具类

aiperm 使用 Spring Data Redis 和 RedisTemplate。

```java
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    // 设置缓存
    public void set(String key, Object value) {
        redisTemplate.opsForValue(key, value);
    }

    // 获取缓存
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue(key);
    }
}
```

### 异常处理

#### BusinessException - 业务异常
```java
package com.devlovecode.aiperm.common.exception;

import lombok.Getter;

/**
 * 业务异常类
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    public BusinessException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public BusinessException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}
```

#### ErrorCode - 错误码枚举
```java
package com.devlovecode.aiperm.common.enums;

import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
public enum ErrorCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败
     */
    FAIL(500, "操作失败"),

    /**
     * 未授权
     */
    UNAuthorized(401, "未授权")

    /**
     * 禁止访问
     */
    Forbidden(403, "禁止访问"),

    /**
     * 资源不存在
     */
    NotFound(404, "资源不存在");

    /**
     * 参数错误
            */
    BadRequestError(400, "请求参数错误");
}
```

#### 全局异常处理器

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e) {
        return R.fail(e.getCode(), e.getMessage());
    }
}
```
