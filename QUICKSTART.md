# RBAC 项目快速启动指南

## 📦 项目概述

这是一个完整的 RBAC（基于角色的访问控制）项目基座，采用前后端分离架构：
- **后端**: Spring Boot 3.5.11 + Java 21 + Sa-Token + MyBatis-Plus
- **前端**: Vue 3.5 + Vite 7 + TypeScript 5.9 + Element Plus

## 🚀 快速启动

### 第一步：初始化数据库

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE aiperm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 执行初始化脚本
mysql -u root -p aiperm < src/main/resources/db/migration/V1.0.0__init_rbac_schema.sql
```

### 第二步：启动后端

```bash
# 方式1：使用 Gradle
./gradlew bootRun

# 方式2：构建并运行 JAR
./gradlew build
java -jar build/libs/aiperm-0.0.1-SNAPSHOT.jar
```

后端服务地址：
- API接口：http://localhost:8080/api
- Swagger文档：http://localhost:8080/api/swagger-ui.html
- API文档（JSON）：http://localhost:8080/api/v3/api-docs

### 第三步：初始化前端

```bash
cd frontend

# 1. 安装依赖
pnpm install

# 2. 生成API客户端代码
pnpm run generate:api

# 3. 启动开发服务器
pnpm run dev
```

前端服务地址：http://localhost:5173

## 📝 默认账号

数据库初始化时会创建默认管理员账号：
- 用户名：`admin`
- 密码：`admin123`

## 🔧 开发指南

### 后端开发

1. **添加新实体**
   ```java
   // src/main/java/com/devlovecode/aiperm/modules/yourmodule/entity/YourEntity.java
   @TableName("your_table")
   public class YourEntity extends BaseEntity {
       // 字段定义
   }
   ```

2. **创建Mapper**
   ```java
   // src/main/java/com/devlovecode/aiperm/modules/yourmodule/mapper/YourMapper.java
   @Mapper
   public interface YourMapper extends BaseMapper<YourEntity> {
   }
   ```

3. **创建Service**
   ```java
   // 接口
   public interface IYourService extends IService<YourEntity> {
   }
   
   // 实现
   @Service
   public class YourServiceImpl extends ServiceImpl<YourMapper, YourEntity> implements IYourService {
   }
   ```

4. **创建Controller**
   ```java
   @RestController
   @RequestMapping("/your-path")
   @Tag(name = "Your API", description = "Your API Description")
   public class YourController {
       // RESTful API 方法
   }
   ```

### 前端开发

1. **生成API代码**
   
   后端修改API后，运行以下命令更新前端API代码：
   ```bash
   cd frontend
   pnpm run generate:api
   ```

2. **使用生成的API**
   ```typescript
   import { useGetUserList } from '@/api/generated'
   
   const { data, isLoading } = useGetUserList({
     page: 1,
     pageSize: 10
   })
   ```

3. **添加新页面**
   - 在 `frontend/src/views/` 下创建页面组件
   - 在 `frontend/src/router/` 中添加路由配置

## 📚 项目结构

### 后端结构
```
src/main/java/com/devlovecode/aiperm/
├── common/              # 公共模块
│   ├── config/         # 配置类
│   ├── domain/         # 领域对象
│   ├── enums/          # 枚举
│   └── exception/      # 异常处理
├── config/             # 核心配置
└── modules/            # 业务模块
    ├── auth/          # 认证模块
    └── system/        # 系统模块
        ├── controller/
        ├── service/
        ├── mapper/
        ├── entity/
        ├── dto/
        └── vo/
```

### 前端结构
```
frontend/src/
├── api/                # API接口（Orval自动生成）
├── assets/            # 静态资源
├── components/        # 公共组件
├── composables/       # 组合式API
├── directives/        # 自定义指令
├── layouts/           # 布局组件
├── router/            # 路由配置
├── stores/            # Pinia状态管理
├── utils/             # 工具函数
└── views/             # 页面视图
```

## 🔐 权限控制

### 后端权限注解
```java
// 检查登录
@SaCheckLogin

// 检查角色
@SaCheckRole("admin")

// 检查权限
@SaCheckPermission("user:create")
```

### 前端权限指令
```vue
<template>
  <!-- 按钮权限控制 -->
  <button v-permission="'user:create'">创建用户</button>
</template>
```

## 🛠️ 常用命令

### 后端
```bash
# 编译项目
./gradlew build

# 运行测试
./gradlew test

# 清理构建
./gradlew clean
```

### 前端
```bash
# 开发模式
pnpm run dev

# 生产构建
pnpm run build

# 预览构建结果
pnpm run preview

# 生成API代码
pnpm run generate:api
```

## 📖 技术文档

- [Spring Boot 文档](https://spring.io/projects/spring-boot)
- [Sa-Token 文档](https://sa-token.cc/)
- [MyBatis-Plus 文档](https://baomidou.com/)
- [Vue 3 文档](https://vuejs.org/)
- [Element Plus 文档](https://element-plus.org/)
- [Orval 文档](https://orval.dev/)

## ❓ 常见问题

**Q: 后端启动失败，提示数据库连接错误？**
A: 检查 `application.yaml` 中的数据库配置，确保 MySQL 已启动且数据库已创建。

**Q: 前端API生成失败？**
A: 确保后端已启动，且可以访问 http://localhost:8080/api/v3/api-docs

**Q: 登录失败？**
A: 检查 Redis 是否已启动，Sa-Token 需要 Redis 存储会话信息。

## 📞 支持

如有问题，请查看项目文档或提交 Issue。
