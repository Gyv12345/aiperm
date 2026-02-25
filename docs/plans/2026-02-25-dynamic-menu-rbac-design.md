# RBAC 动态菜单权限系统设计

## 概述

完善 aiperm RBAC 系统，实现动态菜单生成和完整的权限控制。

## 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        登录流程                              │
├─────────────────────────────────────────────────────────────┤
│  1. 用户登录 → 返回 token                                    │
│  2. 获取用户信息 → 返回 { user, roles, permissions }         │
│  3. 获取用户菜单 → 返回树形菜单列表                           │
│  4. 前端动态生成路由 + 侧边栏                                 │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     权限控制层级                              │
├─────────────────────────────────────────────────────────────┤
│  菜单级：后端过滤，用户只能看到有权限的菜单                    │
│  路由级：前端动态路由，无权限的路由不存在                      │
│  按钮级：v-permission 指令或 hasPermission() 函数             │
└─────────────────────────────────────────────────────────────┘
```

## 核心规则

- **超级管理员**：用户 ID = 1，拥有所有菜单和权限（permissions: ['*']）
- **普通用户**：根据 user → role → menu 关联查询可访问菜单

## 后端 API 设计

### 新增接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/auth/info` | GET | 获取当前用户信息（角色 + 权限标识列表） |
| `/auth/menus` | GET | 获取当前用户可访问的菜单树 |

### `/auth/info` 返回结构

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "超级管理员",
    "avatar": null,
    "roles": ["super_admin"],
    "permissions": ["*"]
  }
}
```

### `/auth/menus` 返回结构

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "menuName": "系统管理",
      "path": "/system",
      "icon": "Setting",
      "sort": 1,
      "children": [
        {
          "id": 2,
          "menuName": "用户管理",
          "path": "user",
          "component": "system/user/index",
          "perms": "system:user:list",
          "sort": 1
        }
      ]
    }
  ]
}
```

### 权限过滤逻辑

```java
// 超级管理员 (userId = 1)：返回所有启用的菜单
// 普通用户：根据 user → role → menu 关联查询可访问菜单
```

## 数据库设计

### 菜单数据（sys_menu）

| ID | 菜单名称 | 父ID | 类型 | 路由路径 | 组件路径 | 图标 | 权限标识 |
|----|----------|------|------|----------|----------|------|----------|
| 1 | 仪表板 | 0 | 目录 | /dashboard | - | Odometer | - |
| 2 | 首页 | 1 | 菜单 | index | dashboard/index | - | - |
| 10 | 系统管理 | 0 | 目录 | /system | - | Setting | - |
| 11 | 用户管理 | 10 | 菜单 | user | system/user/index | - | system:user:list |
| 12 | 角色管理 | 10 | 菜单 | role | system/role/index | - | system:role:list |
| 13 | 菜单管理 | 10 | 菜单 | menu | system/menu/index | - | system:menu:list |
| 14 | 部门管理 | 10 | 菜单 | dept | system/dept/index | - | system:dept:list |
| 15 | 岗位管理 | 10 | 菜单 | post | system/post/index | - | system:post:list |
| 16 | 字典管理 | 10 | 菜单 | dict | system/dict/index | - | system:dict:list |
| 20 | 企业管理 | 0 | 目录 | /enterprise | - | OfficeBuilding | - |
| 21 | 公告通知 | 20 | 菜单 | notice | enterprise/notice/index | - | enterprise:notice:list |
| 22 | 消息中心 | 20 | 菜单 | message | enterprise/message/index | - | enterprise:message:list |

### Flyway 迁移脚本

- `V3.4.0__init_menu_data.sql` - 初始化菜单数据

## 前端设计

### User Store 改造

```typescript
interface UserState {
  token: string
  userInfo: {
    id: number
    username: string
    nickname: string
    avatar: string | null
    roles: string[]        // ['super_admin'] 或 ['admin', 'editor']
    permissions: string[]  // ['*'] 或 ['system:user:list', 'system:role:list']
  } | null
}
```

### Permission Store

```typescript
interface PermissionState {
  menus: MenuItem[]           // 用户可访问菜单树
  routes: RouteRecordRaw[]    // 动态生成的路由
  isRoutesLoaded: boolean     // 路由是否已加载
}

// Actions
- generateRoutes()  // 根据菜单生成动态路由
- resetPermission() // 退出登录时重置
```

### 路由映射表

```typescript
// 前端维护的 component 映射
const componentMap = {
  'dashboard/index': () => import('@/views/dashboard/index.vue'),
  'system/user/index': () => import('@/views/system/user/index.vue'),
  'system/role/index': () => import('@/views/system/role/index.vue'),
  // ... 其他组件映射
}
```

### 路由结构

```typescript
// 1. 静态路由（无需权限）
const constantRoutes = [
  { path: '/login', component: () => import('@/views/login/index.vue') },
  { path: '/404', component: () => import('@/views/error/404.vue') },
]

// 2. 动态路由（由 Permission Store 生成后 addRoute）
const rootRoute = {
  path: '/',
  component: Layout,
  redirect: '/dashboard',
  children: []  // 动态填充
}

// 3. 404 兜底路由（必须最后添加）
{ path: '/:pathMatch(.*)*', redirect: '/404' }
```

### 路由守卫

```typescript
router.beforeEach(async (to, from, next) => {
  const token = userStore.token

  if (!token) {
    to.path === '/login' ? next() : next('/login')
    return
  }

  if (to.path === '/login') {
    next('/dashboard')
    return
  }

  if (!permissionStore.isRoutesLoaded) {
    await userStore.getUserInfo()
    await permissionStore.generateRoutes()
    permissionStore.routes.forEach(route => router.addRoute(route))
    next({ ...to, replace: true })
    return
  }

  next()
})
```

## 按钮级权限控制

### 权限判断函数

```typescript
// utils/permission.ts

export function hasPermission(permission: string): boolean {
  const userStore = useUserStore()
  const permissions = userStore.userInfo?.permissions || []

  if (permissions.includes('*')) return true

  return permissions.includes(permission)
}

export function hasAnyPermission(perms: string[]): boolean {
  return perms.some(p => hasPermission(p))
}
```

### v-permission 指令

```typescript
app.directive('permission', {
  mounted(el, binding) {
    const permission = binding.value
    if (!hasPermission(permission)) {
      el.parentNode?.removeChild(el)
    }
  }
})
```

### 使用方式

```vue
<template>
  <!-- 方式1：指令 -->
  <el-button v-permission="'system:user:add'">新增</el-button>

  <!-- 方式2：函数 -->
  <el-button v-if="hasPermission('system:user:edit')">编辑</el-button>
</template>
```

## 实现任务清单

### 后端任务

| 序号 | 任务 | 文件 |
|------|------|------|
| B1 | 创建菜单 VO | `MenuVO.java` |
| B2 | 菜单初始化 SQL | `V3.4.0__init_menu_data.sql` |
| B3 | 新增 `/auth/info` 接口 | `AuthController.java` |
| B4 | 新增 `/auth/menus` 接口 | `AuthController.java` |
| B5 | 实现用户菜单查询逻辑 | `AuthService.java` |

### 前端任务

| 序号 | 任务 | 文件 |
|------|------|------|
| F1 | 生成 API 客户端 | `pnpm run generate:api` |
| F2 | 改造 User Store | `stores/user.ts` |
| F3 | 新增 Permission Store | `stores/permission.ts` |
| F4 | 创建路由映射表 | `router/component-map.ts` |
| F5 | 改造路由守卫 | `router/index.ts` |
| F6 | 改造侧边栏组件 | `components/layout/AppSidebar.vue` |
| F7 | 新增权限工具函数 | `utils/permission.ts` |
| F8 | 新增 v-permission 指令 | `directives/permission.ts` |

### 执行顺序

```
B1,B2 → B3,B4,B5 → F1 → F2,F3,F4 → F5 → F6 → F7,F8 → 测试验证
```

## 验收标准

1. 使用 admin/admin123 登录后能看到所有菜单
2. 点击任意菜单能正常跳转，不再 404
3. 新建普通用户并分配部分菜单权限，登录后只能看到授权菜单
4. 按钮级权限控制生效（无权限按钮不显示）
