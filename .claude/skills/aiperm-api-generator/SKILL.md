---
name: aiperm-api-generator
description: aiperm 前端 API 手动编写技能。根据后端 Controller 模块结构，在前端创建对应的 API 文件。禁止使用 Orval 自动生成。触发场景：(1) 后端新增或修改了 API 接口 (2) 前端需要调用后端接口 (3) 创建新的 API 模块 (4) 查看后端接口并生成前端代码。关键词：API生成、接口调用、后端接口、fetch、axios、请求、Controller、API模块。
---

# aiperm API 生成器

## 核心原则

**根据后端模块结构，手动编写前端 API。**

- 后端有几个模块，前端就有几个对应的 API 模块
- 每个后端 Controller 对应一个前端 API 文件
- 禁止使用 Orval 自动生成

## 后端模块映射

```
backend/modules/                 frontend/src/api/
├── auth/controller/            ├── auth.ts
│   └── AuthController.java     │
├── oss/controller/             ├── oss.ts
│   └── OssController.java      │
├── log/controller/             ├── log.ts
│   └── SysOperLogController    │
├── system/controller/          └── system/
│   ├── SysUserController           ├── user.ts
│   ├── SysRoleController           ├── role.ts
│   ├── SysMenuController           ├── menu.ts
│   ├── SysPostController           ├── post.ts
│   ├── SysDeptController           ├── dept.ts
│   ├── SysDictTypeController       └── dict.ts
│   └── SysDictDataController
└── enterprise/controller/      └── enterprise/
    ├── SysNoticeController         ├── notice.ts
    ├── SysMessageController        ├── message.ts
    ├── SysJobController            └── job.ts
    └── SysConfigController             config.ts
```

## 生成流程

### 步骤 1：查看后端 Controller

```bash
# 查看所有 Controller
find backend/src/main/java -name "*Controller.java"

# 查看具体 Controller 的接口
cat backend/src/main/java/.../AuthController.java
```

### 步骤 2：分析接口结构

从 Controller 中提取：
- `@RequestMapping` - 模块路径前缀
- `@GetMapping/@PostMapping/@PutMapping/@DeleteMapping` - 接口路径和方法
- 参数类型 - DTO/Path 参数
- 返回类型 - VO/PageResult

### 步骤 3：创建前端 API 文件

## API 文件模板

### 基础模板

```typescript
// src/api/xxx.ts
import request from '@/utils/request'
import type { R, PageResult } from '@/types'

// 类型定义（从后端 DTO/VO 映射）
export interface XxxVO {
  id: number
  name: string
  status: number
  createTime: string
}

export interface XxxDTO {
  id?: number
  name?: string
  status?: number
  page?: number
  pageSize?: number
}

// API 函数
export const xxxApi = {
  // 分页查询
  list: (params: XxxDTO) =>
    request.get<PageResult<XxxVO>>('/xxx', { params }),

  // 根据 ID 查询
  getById: (id: number) =>
    request.get<XxxVO>(`/xxx/${id}`),

  // 创建
  create: (data: XxxDTO) =>
    request.post<number>('/xxx', data),

  // 更新
  update: (id: number, data: XxxDTO) =>
    request.put<void>(`/xxx/${id}`, data),

  // 删除
  delete: (id: number) =>
    request.delete<void>(`/xxx/${id}`),
}
```

### 认证模块示例

```typescript
// src/api/auth.ts
import request from '@/utils/request'
import type { R } from '@/types'

export interface LoginRequest {
  username: string
  password: string
  captchaKey: string
  captchaCode: string
}

export interface LoginVO {
  token: string
  username: string
  nickname: string
  avatar: string
}

export interface UserInfoVO {
  id: number
  username: string
  nickname: string
  avatar: string
  roles: string[]
  permissions: string[]
}

export interface CaptchaVO {
  captchaKey: string
  captchaImage: string
}

export interface MenuVO {
  id: number
  parentId: number
  menuName: string
  path: string
  icon: string
  menuType: string
  children?: MenuVO[]
}

export const authApi = {
  // 获取验证码
  captcha: () =>
    request.get<CaptchaVO>('/auth/captcha'),

  // 登录
  login: (data: LoginRequest) =>
    request.post<LoginVO>('/auth/login', data),

  // 登出
  logout: () =>
    request.post<void>('/auth/logout'),

  // 获取用户信息
  userInfo: () =>
    request.get<LoginVO>('/auth/user-info'),

  // 获取用户详细信息（含角色权限）
  info: () =>
    request.get<UserInfoVO>('/auth/info'),

  // 获取用户菜单
  menus: () =>
    request.get<MenuVO[]>('/auth/menus'),
}
```

### 系统模块示例

```typescript
// src/api/system/user.ts
import request from '@/utils/request'
import type { R, PageResult } from '@/types'

export interface UserVO {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  status: number
  deptId: number
  postIds: number[]
  roleIds: number[]
  createTime: string
}

export interface UserDTO {
  id?: number
  username?: string
  password?: string
  nickname?: string
  email?: string
  phone?: string
  status?: number
  deptId?: number
  postIds?: number[]
  roleIds?: number[]
  page?: number
  pageSize?: number
}

export const userApi = {
  // 分页查询
  list: (params: UserDTO) =>
    request.get<PageResult<UserVO>>('/system/user', { params }),

  // 根据 ID 查询
  getById: (id: number) =>
    request.get<UserVO>(`/system/user/${id}`),

  // 创建
  create: (data: UserDTO) =>
    request.post<void>('/system/user', data),

  // 更新
  update: (id: number, data: UserDTO) =>
    request.put<void>(`/system/user/${id}`, data),

  // 删除
  delete: (id: number) =>
    request.delete<void>(`/system/user/${id}`),

  // 修改状态
  changeStatus: (id: number, status: number) =>
    request.put<void>(`/system/user/${id}/status`, null, { params: { status } }),

  // 重置密码
  resetPassword: (id: number, data: UserDTO) =>
    request.put<void>(`/system/user/${id}/reset-password`, data),
}
```

## 在 Vue 组件中使用

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { authApi, type UserInfoVO } from '@/api/auth'

const loading = ref(false)
const userInfo = ref<UserInfoVO | null>(null)

async function fetchUserInfo() {
  loading.value = true
  try {
    const { data } = await authApi.userInfo()
    userInfo.value = data
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchUserInfo()
})
</script>
```

## 在 Pinia Store 中使用

```typescript
// stores/user.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi, type UserInfoVO } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>('')
  const userInfo = ref<UserInfoVO | null>(null)

  async function fetchUserInfo() {
    const { data } = await authApi.info()
    userInfo.value = data
    return data
  }

  async function logout() {
    await authApi.logout()
    token.value = ''
    userInfo.value = null
  }

  return { token, userInfo, fetchUserInfo, logout }
}, { persist: true })
```

## 类型定义约定

### 后端 → 前端类型映射

| 后端类型 | 前端类型 |
|---------|---------|
| `Long` | `number` |
| `Integer` | `number` |
| `String` | `string` |
| `LocalDateTime` | `string` |
| `List<T>` | `T[]` |
| `R<T>` | `{ code: number, message: string, data: T }` |
| `PageResult<T>` | `{ total: number, records: T[], page: number, pageSize: number }` |

### 通用类型

```typescript
// src/types/index.ts

// 统一响应
export interface R<T = any> {
  code: number
  message: string
  data: T
}

// 分页结果
export interface PageResult<T> {
  total: number
  records: T[]
  page: number
  pageSize: number
}
```

## 开发检查清单

每次创建或更新 API 时：

- [ ] 查看后端 Controller 确认接口路径和方法
- [ ] 查看后端 DTO/VO 确认字段类型
- [ ] 在正确的目录创建/更新 API 文件
- [ ] 导出类型定义和 API 对象
- [ ] 使用 `request.get/post/put/delete` 调用接口
- [ ] 在组件/Store 中正确导入使用

## 常见错误

### 错误 1：路径不一致

```typescript
// ❌ 错误：路径与后端不一致
request.get('/users')  // 后端是 /system/user

// ✅ 正确：与后端 @RequestMapping 一致
request.get('/system/user')
```

### 错误 2：方法不一致

```typescript
// ❌ 错误：方法与后端不一致
request.get('/system/user')  // 后端是 POST

// ✅ 正确：与后端 @PostMapping 一致
request.post('/system/user', data)
```

### 错误 3：类型未定义

```typescript
// ❌ 错误：使用 any
request.get<any>('/system/user')

// ✅ 正确：定义具体类型
request.get<PageResult<UserVO>>('/system/user')
```
