# RBAC 动态菜单权限系统实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现 RBAC 动态菜单权限系统，让用户登录后能根据权限看到正确的菜单并能正常访问页面。

**Architecture:** 后端新增 `/auth/info` 和 `/auth/menus` 接口，根据用户 ID 过滤菜单；前端新增 Permission Store 管理动态路由，改造路由守卫实现动态路由加载。

**Tech Stack:** Spring Boot 3.5 + Sa-Token + Vue 3 + Pinia + Vue Router 5

---

## Task 1: 创建菜单初始化 SQL

**Files:**
- Create: `backend/src/main/resources/db/migration/V3.4.0__init_menu_data.sql`

**Step 1: 创建 Flyway 迁移脚本**

```sql
-- 菜单初始化数据
-- 仪表板
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (1, '仪表板', 0, '1', 1, '/dashboard', NULL, NULL, 'Odometer', 0, 0, 1, 1, 0, 0, NOW(), 'system');

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (2, '首页', 1, '2', 1, 'index', 'dashboard/index', NULL, NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system');

-- 系统管理
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (10, '系统管理', 0, '1', 2, '/system', NULL, NULL, 'Setting', 0, 0, 1, 1, 0, 0, NOW(), 'system');

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (11, '用户管理', 10, '2', 1, 'user', 'system/user/index', 'system:user:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system');

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (12, '角色管理', 10, '2', 2, 'role', 'system/role/index', 'system:role:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system');

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (13, '菜单管理', 10, '2', 3, 'menu', 'system/menu/index', 'system:menu:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system');

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (14, '部门管理', 10, '2', 4, 'dept', 'system/dept/index', 'system:dept:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system');

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (15, '岗位管理', 10, '2', 5, 'post', 'system/post/index', 'system:post:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system');

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (16, '字典管理', 10, '2', 6, 'dict', 'system/dict/index', 'system:dict:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system');

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (17, '权限管理', 10, '2', 7, 'permission', 'system/permission/index', 'system:permission:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system');

-- 企业管理
INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (20, '企业管理', 0, '1', 3, '/enterprise', NULL, NULL, 'OfficeBuilding', 0, 0, 1, 1, 0, 0, NOW(), 'system');

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (21, '公告通知', 20, '2', 1, 'notice', 'enterprise/notice/index', 'enterprise:notice:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system');

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (22, '消息中心', 20, '2', 2, 'message', 'enterprise/message/index', 'enterprise:message:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system');

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (23, '定时任务', 20, '2', 3, 'job', 'enterprise/job/index', 'enterprise:job:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system');

INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, perms, icon, is_external, is_cache, visible, status, deleted, version, create_time, create_by)
VALUES (24, '参数配置', 20, '2', 4, 'config', 'enterprise/config/index', 'enterprise:config:list', NULL, 0, 0, 1, 1, 0, 0, NOW(), 'system');

-- 为超级管理员角色分配所有菜单（假设角色ID=1是超级管理员）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE deleted = 0;
```

**Step 2: 提交**

```bash
git add backend/src/main/resources/db/migration/V3.4.0__init_menu_data.sql
git commit -m "feat(db): add menu initialization data"
```

---

## Task 2: 创建菜单 VO

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/auth/vo/MenuVO.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/auth/vo/UserInfoVO.java`

**Step 1: 创建 MenuVO**

```java
package com.devlovecode.aiperm.modules.auth.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单VO（用于返回给前端）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "菜单信息")
public class MenuVO {

    @Schema(description = "菜单ID")
    private Long id;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "父菜单ID")
    private Long parentId;

    @Schema(description = "菜单类型：1-目录，2-菜单，3-按钮")
    private String menuType;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "权限标识")
    private String perms;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否可见")
    private Integer visible;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "子菜单")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MenuVO> children = new ArrayList<>();
}
```

**Step 2: 创建 UserInfoVO（带角色和权限）**

```java
package com.devlovecode.aiperm.modules.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户信息VO（包含角色和权限）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息")
public class UserInfoVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "角色列表")
    private List<String> roles;

    @Schema(description = "权限列表")
    private List<String> permissions;
}
```

**Step 3: 提交**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/auth/vo/MenuVO.java \
        backend/src/main/java/com/devlovecode/aiperm/modules/auth/vo/UserInfoVO.java
git commit -m "feat(auth): add MenuVO and UserInfoVO"
```

---

## Task 3: 扩展 MenuRepository 添加用户菜单查询

**Files:**
- Modify: `backend/src/main/java/com/devlovecode/aiperm/modules/system/repository/MenuRepository.java`

**Step 1: 添加根据用户ID查询菜单的方法**

在 `MenuRepository.java` 文件末尾添加以下方法：

```java
/**
 * 根据用户ID查询菜单ID列表（通过角色关联）
 */
public List<Long> findMenuIdsByUserId(Long userId) {
    String sql = """
        SELECT DISTINCT rm.menu_id
        FROM sys_role_menu rm
        INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id
        WHERE ur.user_id = :userId
        """;
    return db.sql(sql)
            .param("userId", userId)
            .query(Long.class)
            .list();
}

/**
 * 根据菜单ID列表查询菜单（启用状态）
 */
public List<SysMenu> findByIds(List<Long> menuIds) {
    if (menuIds == null || menuIds.isEmpty()) {
        return List.of();
    }
    String placeholders = String.join(",", menuIds.stream().map(String::valueOf).toArray(String[]::new));
    String sql = String.format(
            "SELECT * FROM sys_menu WHERE id IN (%s) AND status = 1 AND deleted = 0 ORDER BY parent_id ASC, sort ASC",
            placeholders
    );
    return db.sql(sql).query(SysMenu.class).list();
}

/**
 * 查询所有启用的菜单（用于超级管理员）
 */
public List<SysMenu> findAllEnabled() {
    String sql = "SELECT * FROM sys_menu WHERE status = 1 AND deleted = 0 ORDER BY parent_id ASC, sort ASC";
    return db.sql(sql).query(SysMenu.class).list();
}
```

**Step 2: 提交**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/system/repository/MenuRepository.java
git commit -m "feat(menu): add user menu query methods"
```

---

## Task 4: 扩展 AuthService 添加用户信息和菜单查询

**Files:**
- Modify: `backend/src/main/java/com/devlovecode/aiperm/modules/auth/service/AuthService.java`

**Step 1: 添加依赖注入和常量**

在 `AuthService` 类中添加：

```java
private final MenuRepository menuRepo;
private static final Long SUPER_ADMIN_ID = 1L;
```

**Step 2: 添加获取用户信息方法**

```java
/**
 * 获取当前用户完整信息（包含角色和权限）
 */
public UserInfoVO getUserInfo() {
    Long userId = StpUtil.getLoginIdAsLong();
    SysUser user = userRepo.findById(userId)
            .orElseThrow(() -> new BusinessException("用户不存在"));

    List<String> roles;
    List<String> permissions;

    if (SUPER_ADMIN_ID.equals(userId)) {
        // 超级管理员
        roles = List.of("super_admin");
        permissions = List.of("*");
    } else {
        // 普通用户：查询角色和权限
        roles = getUserRoles(userId);
        permissions = getUserPermissions(userId);
    }

    return UserInfoVO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .nickname(user.getNickname())
            .avatar(user.getAvatar())
            .roles(roles)
            .permissions(permissions)
            .build();
}

/**
 * 获取当前用户可访问的菜单
 */
public List<MenuVO> getUserMenus() {
    Long userId = StpUtil.getLoginIdAsLong();

    List<SysMenu> menus;
    if (SUPER_ADMIN_ID.equals(userId)) {
        // 超级管理员：返回所有启用的菜单
        menus = menuRepo.findAllEnabled();
    } else {
        // 普通用户：根据角色查询
        List<Long> menuIds = menuRepo.findMenuIdsByUserId(userId);
        menus = menuRepo.findByIds(menuIds);
    }

    // 构建树形结构
    return buildMenuTree(menus, 0L);
}

// ========== 私有方法 ==========

private List<String> getUserRoles(Long userId) {
    String sql = """
        SELECT r.role_key
        FROM sys_role r
        INNER JOIN sys_user_role ur ON r.id = ur.role_id
        WHERE ur.user_id = :userId AND r.status = 1 AND r.deleted = 0
        """;
    return menuRepo.getJdbcClient().sql(sql)
            .param("userId", userId)
            .query(String.class)
            .list();
}

private List<String> getUserPermissions(Long userId) {
    // 获取用户所有角色的权限标识
    String sql = """
        SELECT DISTINCT m.perms
        FROM sys_menu m
        INNER JOIN sys_role_menu rm ON m.id = rm.menu_id
        INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id
        WHERE ur.user_id = :userId AND m.perms IS NOT NULL AND m.perms != ''
          AND m.status = 1 AND m.deleted = 0
        """;
    return menuRepo.getJdbcClient().sql(sql)
            .param("userId", userId)
            .query(String.class)
            .list();
}

private List<MenuVO> buildMenuTree(List<SysMenu> allMenus, Long parentId) {
    Map<Long, List<SysMenu>> groupedByParent = allMenus.stream()
            .collect(Collectors.groupingBy(SysMenu::getParentId));

    List<SysMenu> roots = groupedByParent.getOrDefault(parentId, new ArrayList<>());
    return roots.stream()
            .map(menu -> toMenuVO(menu, groupedByParent))
            .collect(Collectors.toList());
}

private MenuVO toMenuVO(SysMenu menu, Map<Long, List<SysMenu>> groupedByParent) {
    MenuVO vo = MenuVO.builder()
            .id(menu.getId())
            .menuName(menu.getMenuName())
            .parentId(menu.getParentId())
            .menuType(menu.getMenuType())
            .path(menu.getPath())
            .component(menu.getComponent())
            .perms(menu.getPerms())
            .icon(menu.getIcon())
            .sort(menu.getSort())
            .visible(menu.getVisible())
            .status(menu.getStatus())
            .build();

    List<SysMenu> children = groupedByParent.getOrDefault(menu.getId(), new ArrayList<>());
    if (!children.isEmpty()) {
        vo.setChildren(children.stream()
                .map(child -> toMenuVO(child, groupedByParent))
                .collect(Collectors.toList()));
    }

    return vo;
}
```

**Step 3: 添加必要的导入**

```java
import com.devlovecode.aiperm.modules.auth.vo.MenuVO;
import com.devlovecode.aiperm.modules.auth.vo.UserInfoVO;
import com.devlovecode.aiperm.modules.system.repository.MenuRepository;
import java.util.Map;
import java.util.stream.Collectors;
```

**Step 4: 提交**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/auth/service/AuthService.java
git commit -m "feat(auth): add getUserInfo and getUserMenus methods"
```

---

## Task 5: 扩展 AuthController 添加新接口

**Files:**
- Modify: `backend/src/main/java/com/devlovecode/aiperm/modules/auth/controller/AuthController.java`

**Step 1: 添加新接口**

在 `AuthController` 类中添加：

```java
@Operation(summary = "获取当前用户信息（含角色权限）")
@SaCheckLogin
@GetMapping("/info")
public R<UserInfoVO> info() {
    return R.ok(authService.getUserInfo());
}

@Operation(summary = "获取当前用户菜单")
@SaCheckLogin
@GetMapping("/menus")
public R<List<MenuVO>> menus() {
    return R.ok(authService.getUserMenus());
}
```

**Step 2: 添加导入**

```java
import com.devlovecode.aiperm.modules.auth.vo.MenuVO;
import com.devlovecode.aiperm.modules.auth.vo.UserInfoVO;
import java.util.List;
```

**Step 3: 提交**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/auth/controller/AuthController.java
git commit -m "feat(auth): add /auth/info and /auth/menus endpoints"
```

---

## Task 6: 生成前端 API 客户端

**Files:**
- Generated: `frontend/src/api/generated.ts`
- Generated: `frontend/src/models/`

**Step 1: 启动后端服务**

```bash
cd backend && ./gradlew bootRun
```

等待后端启动完成（看到 "Started AipermApplication" 日志）。

**Step 2: 生成 API 客户端**

```bash
cd frontend && pnpm run generate:api
```

**Step 3: 验证生成结果**

检查 `frontend/src/models/` 目录下是否生成了 `MenuVO.ts` 和 `UserInfoVO.ts` 类型定义。

**Step 4: 提交**

```bash
git add frontend/src/api/ frontend/src/models/
git commit -m "feat(frontend): generate API client for auth endpoints"
```

---

## Task 7: 改造 User Store

**Files:**
- Modify: `frontend/src/stores/user.ts`

**Step 1: 添加获取用户信息的方法**

```typescript
import { defineStore } from 'pinia'
import { ref, shallowRef, computed } from 'vue'
import { authControllerInfo } from '@/api/generated'

export interface UserInfo {
  id: number
  username: string
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  roles: string[]
  permissions: string[]
}

export const useUserStore = defineStore(
  'user',
  () => {
    const token = ref<string | null>(null)
    const userInfo = shallowRef<UserInfo | null>(null)

    const isLoggedIn = computed(() => !!token.value)
    const username = computed(() => userInfo.value?.username ?? '')
    const roles = computed(() => userInfo.value?.roles ?? [])
    const permissions = computed(() => userInfo.value?.permissions ?? [])

    function setToken(newToken: string) {
      token.value = newToken
    }

    function setUserInfo(info: UserInfo) {
      userInfo.value = info
    }

    // 从后端获取用户信息
    async function fetchUserInfo() {
      try {
        const { data } = await authControllerInfo()
        userInfo.value = {
          id: data.id,
          username: data.username,
          nickname: data.nickname,
          avatar: data.avatar,
          roles: data.roles,
          permissions: data.permissions,
        }
        return userInfo.value
      }
      catch (error) {
        console.error('Failed to fetch user info:', error)
        throw error
      }
    }

    function hasRole(role: string): boolean {
      return roles.value.includes(role)
    }

    function hasPermission(permission: string): boolean {
      // 超级管理员拥有所有权限
      if (permissions.value.includes('*')) {
        return true
      }
      return permissions.value.includes(permission)
    }

    function hasAnyPermission(permissionList: string[]): boolean {
      return permissionList.some(p => hasPermission(p))
    }

    function logout() {
      token.value = null
      userInfo.value = null
    }

    return {
      token,
      userInfo,
      isLoggedIn,
      username,
      roles,
      permissions,
      setToken,
      setUserInfo,
      fetchUserInfo,
      hasRole,
      hasPermission,
      hasAnyPermission,
      logout,
    }
  },
  {
    persist: {
      key: 'aiperm-user',
      storage: localStorage,
      pick: ['token', 'userInfo'],
    },
  },
)
```

**Step 2: 提交**

```bash
git add frontend/src/stores/user.ts
git commit -m "feat(store): add fetchUserInfo method to UserStore"
```

---

## Task 8: 创建 Permission Store

**Files:**
- Create: `frontend/src/stores/permission.ts`

**Step 1: 创建 Permission Store**

```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import { authControllerMenus } from '@/api/generated'
import type { MenuVO } from '@/models'

// 组件映射表
const componentModules = import.meta.glob('@/views/**/*.vue')

function loadComponent(component: string | undefined) {
  if (!component) {
    return undefined
  }
  const path = `/src/views/${component}.vue`
  return componentModules[path]
}

interface MenuItem extends MenuVO {
  children?: MenuItem[]
}

export const usePermissionStore = defineStore('permission', () => {
  const menus = ref<MenuItem[]>([])
  const routes = ref<RouteRecordRaw[]>([])
  const isRoutesLoaded = ref(false)

  // 获取用户菜单
  async function fetchMenus() {
    try {
      const { data } = await authControllerMenus()
      menus.value = data as MenuItem[]
      return menus.value
    }
    catch (error) {
      console.error('Failed to fetch menus:', error)
      throw error
    }
  }

  // 根据菜单生成路由
  function generateRoutes() {
    const dynamicRoutes: RouteRecordRaw[] = []

    // 遍历菜单生成路由
    menus.value.forEach((menu) => {
      const route = generateRoute(menu)
      if (route) {
        dynamicRoutes.push(route)
      }
    })

    // 根路由
    const rootRoute: RouteRecordRaw = {
      path: '/',
      name: 'Root',
      component: () => import('@/components/layout/MainLayout.vue'),
      redirect: '/dashboard',
      children: dynamicRoutes,
    }

    routes.value = [rootRoute]
    isRoutesLoaded.value = true

    return routes.value
  }

  // 生成单个路由
  function generateRoute(menu: MenuItem): RouteRecordRaw | null {
    // 只处理菜单类型（menuType = '2'）
    if (menu.menuType !== '2') {
      return null
    }

    const component = loadComponent(menu.component)
    if (!component) {
      console.warn(`Component not found: ${menu.component}`)
      return null
    }

    // 构建完整路径
    const fullPath = buildFullPath(menu)

    return {
      path: fullPath,
      name: `menu-${menu.id}`,
      component,
      meta: {
        title: menu.menuName,
        icon: menu.icon,
        requiresAuth: true,
      },
    }
  }

  // 构建完整路径
  function buildFullPath(menu: MenuItem): string {
    // 查找父菜单
    const parent = findParent(menus.value, menu.parentId)
    if (parent && parent.path) {
      // 父菜单路径是 /system，子菜单路径是 user，完整路径是 /system/user
      const parentPath = parent.path.startsWith('/') ? parent.path : `/${parent.path}`
      return `${parentPath}/${menu.path}`
    }
    // 没有父菜单或父菜单没有路径，直接使用自己的路径
    return menu.path?.startsWith('/') ? menu.path : `/${menu.path}`
  }

  // 查找父菜单
  function findParent(menus: MenuItem[], parentId: number | null): MenuItem | null {
    if (!parentId || parentId === 0) {
      return null
    }
    for (const menu of menus) {
      if (menu.id === parentId) {
        return menu
      }
      if (menu.children) {
        const found = findParent(menu.children, parentId)
        if (found) {
          return found
        }
      }
    }
    return null
  }

  // 重置权限
  function resetPermission() {
    menus.value = []
    routes.value = []
    isRoutesLoaded.value = false
  }

  return {
    menus,
    routes,
    isRoutesLoaded,
    fetchMenus,
    generateRoutes,
    resetPermission,
  }
})
```

**Step 2: 提交**

```bash
git add frontend/src/stores/permission.ts
git commit -m "feat(store): create PermissionStore for dynamic routes"
```

---

## Task 9: 改造路由配置

**Files:**
- Modify: `frontend/src/router/index.ts`

**Step 1: 重构路由配置**

```typescript
import {
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
  type Router,
} from 'vue-router'
import type { App } from 'vue'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'

// 静态路由（不需要权限）
export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: {
      title: '登录',
      requiresAuth: false,
    },
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '404',
    },
  },
]

// 创建路由实例
const router: Router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: constantRoutes,
  scrollBehavior: () => ({ top: 0 }),
})

// 白名单路由
const whiteList = ['/login', '/404']

// 设置路由守卫
function setupRouterGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    const userStore = useUserStore()
    const permissionStore = usePermissionStore()
    const token = userStore.token

    // 设置页面标题
    document.title = `${to.meta.title ?? 'AIPerm'} - 权限管理系统`

    // 未登录
    if (!token) {
      if (whiteList.includes(to.path)) {
        next()
      }
      else {
        next(`/login?redirect=${to.path}`)
      }
      return
    }

    // 已登录访问登录页
    if (to.path === '/login') {
      next('/dashboard')
      return
    }

    // 路由已加载
    if (permissionStore.isRoutesLoaded) {
      next()
      return
    }

    // 动态加载路由
    try {
      // 1. 获取用户信息
      await userStore.fetchUserInfo()

      // 2. 获取菜单
      await permissionStore.fetchMenus()

      // 3. 生成路由
      const routes = permissionStore.generateRoutes()

      // 4. 动态添加路由
      routes.forEach((route) => {
        router.addRoute(route)
      })

      // 5. 添加 404 兜底路由（必须最后添加）
      router.addRoute({
        path: '/:pathMatch(.*)*',
        redirect: '/404',
      })

      // 6. 重新导航
      next({ ...to, replace: true })
    }
    catch (error) {
      console.error('Failed to load routes:', error)
      // 加载失败，清除 token 并跳转登录
      userStore.logout()
      next(`/login?redirect=${to.path}`)
    }
  })

  router.afterEach(() => {
    // 路由切换后处理
  })
}

// 初始化路由
export function setupRouter(app: App) {
  setupRouterGuard(router)
  app.use(router)
}

export default router
```

**Step 2: 提交**

```bash
git add frontend/src/router/index.ts
git commit -m "feat(router): add dynamic route loading with permission"
```

---

## Task 10: 改造侧边栏组件

**Files:**
- Modify: `frontend/src/components/layout/AppSidebar.vue`

**Step 1: 重构侧边栏组件**

```vue
<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { usePermissionStore } from '@/stores/permission'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const permissionStore = usePermissionStore()

// 从 PermissionStore 获取菜单
const menuItems = computed(() => permissionStore.menus)

// 侧边栏折叠状态
const collapsed = computed(() => appStore.sidebarCollapsed)

// 切换侧边栏
function toggleSidebar() {
  appStore.toggleSidebar()
}

// 导航到指定路径
function navigateTo(menu: typeof menuItems.value[0]) {
  // 构建完整路径
  const fullPath = buildMenuPath(menu)
  router.push(fullPath)
}

// 构建菜单路径
function buildMenuPath(menu: typeof menuItems.value[0]): string {
  if (menu.menuType === '1') {
    // 目录类型，跳转到第一个子菜单
    if (menu.children && menu.children.length > 0) {
      return buildMenuPath(menu.children[0])
    }
    return menu.path || '/'
  }
  // 菜单类型，查找父菜单构建完整路径
  const parent = findParent(menuItems.value, menu.parentId)
  if (parent && parent.path) {
    const parentPath = parent.path.startsWith('/') ? parent.path : `/${parent.path}`
    return `${parentPath}/${menu.path}`
  }
  return menu.path?.startsWith('/') ? menu.path : `/${menu.path}`
}

// 查找父菜单
function findParent(menus: typeof menuItems.value, parentId: number | null) {
  if (!parentId || parentId === 0) {
    return null
  }
  for (const menu of menus) {
    if (menu.id === parentId) {
      return menu
    }
    if (menu.children) {
      const found = findParent(menu.children, parentId)
      if (found) return found
    }
  }
  return null
}

// 检查是否激活
function isActive(menu: typeof menuItems.value[0]): boolean {
  const fullPath = buildMenuPath(menu)
  return route.path === fullPath || route.path.startsWith(fullPath + '/')
}

// 获取需要显示的菜单（过滤掉目录，只显示一级菜单）
const displayMenus = computed(() => {
  return menuItems.value.map((menu) => {
    // 如果是目录，显示第一个子菜单的信息
    if (menu.menuType === '1' && menu.children && menu.children.length > 0) {
      return {
        ...menu,
        firstChild: menu.children[0],
      }
    }
    return menu
  })
})
</script>

<template>
  <aside
    class="sidebar flex flex-col bg-gray-800 text-white transition-all duration-300"
    :class="collapsed ? 'w-16' : 'w-64'"
  >
    <!-- Logo 区域 -->
    <div class="h-16 flex items-center justify-center border-b border-gray-700">
      <h1
        v-if="!collapsed"
        class="text-xl font-bold"
      >
        AIPerm
      </h1>
      <el-icon
        v-else
        class="text-2xl"
      >
        <Box />
      </el-icon>
    </div>

    <!-- 菜单列表 -->
    <nav class="flex-1 p-2 overflow-y-auto">
      <ul class="space-y-1">
        <li
          v-for="menu in displayMenus"
          :key="menu.id"
        >
          <div
            class="menu-item flex items-center px-3 py-2 rounded cursor-pointer transition-colors"
            :class="isActive(menu) ? 'bg-blue-600 text-white' : 'hover:bg-gray-700'"
            @click="navigateTo(menu)"
          >
            <el-icon class="text-lg">
              <component :is="menu.icon || 'Document'" />
            </el-icon>
            <span
              v-if="!collapsed"
              class="ml-3"
            >
              {{ menu.menuName }}
            </span>
          </div>
        </li>
      </ul>
    </nav>

    <!-- 折叠按钮 -->
    <div class="p-2 border-t border-gray-700">
      <div
        class="flex items-center justify-center py-2 rounded cursor-pointer hover:bg-gray-700"
        @click="toggleSidebar"
      >
        <el-icon class="text-lg">
          <Expand v-if="collapsed" />
          <Fold v-else />
        </el-icon>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  min-height: 100vh;
}

.menu-item {
  white-space: nowrap;
}
</style>
```

**Step 2: 提交**

```bash
git add frontend/src/components/layout/AppSidebar.vue
git commit -m "feat(layout): use dynamic menus in AppSidebar"
```

---

## Task 11: 创建权限工具函数

**Files:**
- Create: `frontend/src/utils/permission.ts`

**Step 1: 创建权限工具函数**

```typescript
import { useUserStore } from '@/stores/user'

/**
 * 检查是否有指定权限
 * @param permission 权限标识，如 'system:user:add'
 */
export function hasPermission(permission: string): boolean {
  const userStore = useUserStore()
  return userStore.hasPermission(permission)
}

/**
 * 检查是否有任一权限
 * @param permissions 权限标识列表
 */
export function hasAnyPermission(permissions: string[]): boolean {
  const userStore = useUserStore()
  return userStore.hasAnyPermission(permissions)
}

/**
 * 检查是否有指定角色
 * @param role 角色标识
 */
export function hasRole(role: string): boolean {
  const userStore = useUserStore()
  return userStore.hasRole(role)
}

/**
 * 检查是否有任一角色
 * @param roles 角色标识列表
 */
export function hasAnyRole(roles: string[]): boolean {
  const userStore = useUserStore()
  return roles.some(role => userStore.hasRole(role))
}
```

**Step 2: 提交**

```bash
git add frontend/src/utils/permission.ts
git commit -m "feat(utils): add permission utility functions"
```

---

## Task 12: 创建 v-permission 指令

**Files:**
- Create: `frontend/src/directives/permission.ts`
- Modify: `frontend/src/main.ts`

**Step 1: 创建权限指令**

```typescript
import type { Directive, DirectiveBinding } from 'vue'
import { hasPermission } from '@/utils/permission'

/**
 * v-permission 指令
 * 用法: v-permission="'system:user:add'" 或 v-permission="['system:user:add', 'system:user:edit']"
 */
export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    const value = binding.value

    if (!value) {
      return
    }

    let hasAuth = false

    if (Array.isArray(value)) {
      hasAuth = value.some(p => hasPermission(p))
    }
    else {
      hasAuth = hasPermission(value)
    }

    if (!hasAuth) {
      el.parentNode?.removeChild(el)
    }
  },
}

/**
 * v-role 指令
 * 用法: v-role="'admin'" 或 v-role="['admin', 'editor']"
 */
export const role: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    const { useUserStore } = require('@/stores/user')
    const userStore = useUserStore()
    const value = binding.value

    if (!value) {
      return
    }

    let hasAuth = false

    if (Array.isArray(value)) {
      hasAuth = value.some(r => userStore.hasRole(r))
    }
    else {
      hasAuth = userStore.hasRole(value)
    }

    if (!hasAuth) {
      el.parentNode?.removeChild(el)
    }
  },
}

export default {
  permission,
  role,
}
```

**Step 2: 在 main.ts 中注册指令**

```typescript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'uno.css'

import App from './App.vue'
import { setupRouter } from './router'
import permissionDirectives from './directives/permission'

const app = createApp(App)

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 创建 Pinia 实例并配置持久化插件
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

app.use(pinia)
app.use(ElementPlus)

// 注册权限指令
app.directive('permission', permissionDirectives.permission)
app.directive('role', permissionDirectives.role)

setupRouter(app)

app.mount('#app')
```

**Step 3: 提交**

```bash
git add frontend/src/directives/permission.ts frontend/src/main.ts
git commit -m "feat(directives): add v-permission and v-role directives"
```

---

## Task 13: 修复 MenuRepository 的 JdbcClient 访问

**Files:**
- Modify: `backend/src/main/java/com/devlovecode/aiperm/modules/system/repository/MenuRepository.java`

**Step 1: 添加公开的 JdbcClient getter**

在 `MenuRepository` 类中添加：

```java
/**
 * 获取 JdbcClient（供其他服务使用）
 */
public JdbcClient getJdbcClient() {
    return this.db;
}
```

**Step 2: 提交**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/system/repository/MenuRepository.java
git commit -m "fix(menu): expose JdbcClient getter for AuthService"
```

---

## Task 14: 测试验证

**Step 1: 重启后端服务**

```bash
cd backend && ./gradlew bootRun
```

确认 Flyway 执行了 `V3.4.0__init_menu_data.sql` 迁移。

**Step 2: 重启前端服务**

```bash
cd frontend && pnpm run dev
```

**Step 3: 验证功能**

1. 使用 `admin` / `admin123` 登录
2. 确认能看到所有菜单
3. 点击各个菜单，确认不再 404
4. 刷新页面，确认菜单和路由仍然正常

**验收标准：**
- [ ] 登录后能看到所有菜单
- [ ] 点击任意菜单能正常跳转
- [ ] 刷新页面后路由正常
- [ ] 无控制台错误

---

## Task 15: 最终提交

```bash
git add -A
git commit -m "feat(rbac): complete dynamic menu and permission system

- Add menu initialization SQL
- Add /auth/info and /auth/menus endpoints
- Add PermissionStore for dynamic routes
- Refactor router with permission guard
- Update AppSidebar with dynamic menus
- Add v-permission directive

Closes: RBAC dynamic menu implementation"
```

---

## 执行顺序总结

```
Task 1  → Task 2  → Task 3  → Task 4  → Task 5
(SQL)     (VO)      (Repo)    (Service) (Controller)

    ↓
Task 6  → Task 7  → Task 8  → Task 9  → Task 10
(Gen API)  (User)    (Perm)    (Router)   (Sidebar)

    ↓
Task 11 → Task 12 → Task 13 → Task 14 → Task 15
(Utils)   (Directive) (Fix)    (Test)     (Final)
```
