## 工具类使用参考

### Hutool 工具类

#### StrUtil - 字符串处理
```java
import cn.hutool.core.util.StrUtil;

// 判断字符串是否为空白
boolean isBlank = StrUtil.isBlank(str);

// 判断字符串是否不为空白
boolean isNotBlank = StrUtil.isNotBlank(str);

// 去除前后空格
String trimmed = StrUtil.trim(str);
```

#### CollUtil - 集合操作
```java
import cn.hutool.core.collection.CollUtil;

// 判断集合是否为空
boolean isEmpty = CollUtil.isEmpty(list);

// 判断集合是否不为空
boolean isNotEmpty = CollUtil.isNotEmpty(list);
```

#### BCrypt - 密码加密
```java
import cn.hutool.crypto.digest.BCrypt;

// 加密密码
String hashedPassword = BCrypt.hashpw(password);

// 验证密码
boolean isValid = BCrypt.checkpw(password, hashedPassword);
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

// 检查是否登录
boolean isLogin = StpUtil.isLogin();

// 登录
StpUtil.login(userId);

// 登出
StpUtil.logout();
```

### 异常处理

#### BusinessException - 业务异常
```java
import com.devlovecode.aiperm.common.exception.BusinessException;

// 抛出业务异常
throw new BusinessException("数据不存在");

// 带错误码的异常
throw new BusinessException(ErrorCode.NOT_FOUND);
```

### SqlBuilder - SQL 条件构建器

```java
import com.devlovecode.aiperm.common.repository.SqlBuilder;

SqlBuilder sb = new SqlBuilder();

// LIKE 模糊查询（条件满足时添加）
sb.likeIf(name != null && !name.isBlank(), "name", name);

// 精确条件（条件满足时添加）
sb.whereIf(status != null, "status = ?", status);

// IN 条件
sb.inIf(ids != null && !ids.isEmpty(), "id", ids);

// 获取 WHERE 子句（带 AND 前缀）
String whereClause = sb.getWhereClause();

// 获取参数列表
List<Object> params = sb.getParams();
```

### R - 统一响应封装

```java
import com.devlovecode.aiperm.common.domain.R;

// 成功响应
R.ok()              // 无数据
R.ok(data)          // 带数据

// 失败响应
R.fail()            // 无消息
R.fail("错误消息")   // 带消息
R.fail(code, "错误消息")  // 带错误码
```

### PageResult - 分页结果封装

```java
import com.devlovecode.aiperm.common.domain.PageResult;

// 构建分页结果
PageResult<XxxVO> result = PageResult.of(total, list, pageNum, pageSize);

// 空分页结果
PageResult<XxxVO> empty = PageResult.empty(pageNum, pageSize);

// 类型转换
PageResult<XxxVO> voResult = entityResult.map(this::toVO);
```
