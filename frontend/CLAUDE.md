# Frontend - aiperm 前端开发规范

> 本文件定义 aiperm 前端开发的所有规范和约定。AI 助手在开发前端代码时必须遵循此文档。

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5.x | 前端框架 |
| TypeScript | 5.9.x | 类型安全 |
| Vite | 7.3.x | 构建工具 |
| Element Plus | 2.13.x | UI 组件库 |
| Pinia | 3.0.x | 状态管理 |
| Vue Router | 5.0.x | 路由管理 |
| Orval | 7.13.x | API 代码生成 |
| Axios | 1.13.x | HTTP 客户端 |
| VueUse | 14.x | 组合式工具 |
| UnoCSS | 66.x | 原子化 CSS |

## 目录结构

```
frontend/src/
├── api/                        # API 层（Orval 自动生成）
│   └── generated.ts            # 生成的 API 函数
├── models/                     # TypeScript 类型（Orval 自动生成）
├── stores/                     # Pinia 状态管理
│   ├── user.ts                 # 用户状态（token、权限）
│   └── app.ts                  # 应用状态（侧边栏、主题）
├── router/                     # Vue Router 路由
├── views/                      # 页面视图
│   ├── dashboard/              # 仪表盘
│   ├── login/                  # 登录页
│   └── error/                  # 错误页（404）
├── components/                 # 公共组件
│   └── layout/                 # 布局组件
├── composables/                # 组合式函数
├── utils/                      # 工具函数
│   └── api-mutator.ts          # Axios 请求拦截器
└── main.ts                     # 应用入口
```

## API 开发流程（重要！）

**前端绝对禁止手写 API 调用代码！**

### 标准流程

1. **后端开发/修改 API** → 在 Controller 中添加或修改接口
2. **启动后端服务** → `cd backend && ./gradlew bootRun`
3. **生成 API 客户端** → `cd frontend && pnpm run generate:api`
4. **查看生成的类型** → 检查 `src/models/` 中的 TypeScript 类型
5. **使用生成的 API** → 导入并使用生成的 API 函数

### 错误做法（禁止）

```typescript
// 不要这样！缺乏类型安全
const response = await axios.get('/api/users')
const users = response.data // 类型为 any
```

### 正确做法

```typescript
// 1. 先生成 API
// cd frontend && pnpm run generate:api

// 2. 导入生成的类型和 API
import type { UserVO } from '@/models'
import { userControllerFindAll } from '@/api/generated'

// 3. 使用生成的 API（完全类型安全）
const { data } = await userControllerFindAll({ page: 1, pageSize: 10 })
// data 的类型自动推断
```

## 常用命令

```bash
# 安装依赖
pnpm install

# 生成 API 客户端（后端 API 变更后必须执行）
pnpm run generate:api

# 启动开发服务器
pnpm run dev

# 构建生产版本
pnpm run build

# 预览生产版本
pnpm run preview
```

## 组件开发规范

### Vue SFC 结构

```vue
<script setup lang="ts">
// 1. 导入
import { ref, onMounted } from 'vue'
import type { UserVO } from '@/models'
import { userControllerFindAll } from '@/api/generated'

// 2. Props/Emits
const props = defineProps<{
  id: number
}>()

const emit = defineEmits<{
  (e: 'update', value: string): void
}>()

// 3. 响应式状态
const loading = ref(false)
const users = ref<UserVO[]>([])

// 4. 方法
const fetchUsers = async () => {
  loading.value = true
  try {
    const { data } = await userControllerFindAll({})
    users.value = data
  } finally {
    loading.value = false
  }
}

// 5. 生命周期
onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <!-- 模板内容 -->
</template>

<style scoped>
/* 样式 */
</style>
```

### 命名约定

| 类型 | 约定 | 示例 |
|------|------|------|
| 组件文件 | PascalCase | `UserList.vue` |
| 组合式函数 | use 前缀 | `useUserList.ts` |
| Store 文件 | 小写 | `user.ts` |
| 类型/接口 | PascalCase | `UserVO`, `LoginRequest` |

## ⚠️ 前后端接口规范（重要！）

**前后端字段名必须保持一致！**

### 分页结果 PageResult

后端返回的分页格式：
```json
{
  "total": 100,
  "list": [...],
  "pageNum": 1,
  "pageSize": 10,
  "pages": 10
}
```

前端类型定义（`src/types/index.ts`）：
```typescript
export interface PageResult<T> {
  total: number
  list: T[]        // 注意：是 list，不是 records！
  pageNum: number  // 注意：是 pageNum，不是 page！
  pageSize: number
  pages: number
}
```

### 正确使用方式

```typescript
// ✅ 正确
const result = await userApi.list(params) as PageResult<UserVO>
tableData.value = result.list || []
pagination.total = result.total || 0

// ❌ 错误 - 会导致数据不显示
tableData.value = result.records || []  // records 不存在！
```

## 状态管理

### Pinia Store 规范

```typescript
// stores/user.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>('')
  const userInfo = ref<UserVO | null>(null)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)

  // 方法
  const setToken = (newToken: string) => {
    token.value = newToken
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    setToken,
    logout
  }
}, {
  persist: true  // 持久化
})
```

## 路由配置

```typescript
// router/index.ts
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/components/layout/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue')
      }
    ]
  }
]
```

## 请求拦截器

```typescript
// utils/api-mutator.ts
import axios from 'axios'

const instance = axios.create({
  baseURL: '/api'
})

// 请求拦截
instance.interceptors.request.use((config) => {
  const token = useUserStore().token
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

// 响应拦截
instance.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      // 跳转登录
      router.push('/login')
    }
    return Promise.reject(error)
  }
)
```

## Element Plus 使用

### 表单验证

```vue
<template>
  <el-form ref="formRef" :model="form" :rules="rules">
    <el-form-item label="用户名" prop="username">
      <el-input v-model="form.username" />
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
const formRef = ref()
const form = ref({
  username: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ]
}

const submit = async () => {
  await formRef.value.validate()
  // 提交逻辑
}
</script>
```

### 表格分页

```vue
<template>
  <el-table :data="tableData" v-loading="loading">
    <el-table-column prop="name" label="名称" />
  </el-table>
  <el-pagination
    v-model:current-page="page"
    v-model:page-size="pageSize"
    :total="total"
    @change="fetchData"
  />
</template>
```

## 开发检查清单

开发前端功能前必须逐项确认：

- [ ] 后端 API 是否已开发完成？
- [ ] 是否运行了 `pnpm run generate:api`？
- [ ] 是否查看了 `src/models/` 中的类型定义？
- [ ] 是否使用了生成的 API 函数？
- [ ] IDE 是否显示类型错误？
- [ ] 是否遵循了组件命名约定？

## 常见问题

| 问题 | 解决方案 |
|------|----------|
| API 类型不匹配 | 运行 `pnpm run generate:api` |
| 登录后 401 | 检查 token 是否正确设置到请求头 |
| 页面空白 | 检查控制台错误，确认路由配置 |
