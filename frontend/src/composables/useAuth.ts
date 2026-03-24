import {computed} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {useUserStore} from '@/stores/user'

/**
 * 认证相关的组合式函数
 * 封装登录、登出、权限检查等逻辑
 */
export function useAuth() {
  const router = useRouter()
  const userStore = useUserStore()

  // 计算属性
  const isLoggedIn = computed(() => userStore.isLoggedIn)
  const username = computed(() => userStore.username)
  const roles = computed(() => userStore.roles)
  const permissions = computed(() => userStore.permissions)
  const userInfo = computed(() => userStore.userInfo)

  /**
   * 检查是否有指定角色
   */
  function hasRole(role: string | string[]): boolean {
    if (Array.isArray(role)) {
      return role.some(r => userStore.hasRole(r))
    }
    return userStore.hasRole(role)
  }

  /**
   * 检查是否有指定权限
   */
  function hasPermission(permission: string | string[]): boolean {
    if (Array.isArray(permission)) {
      return userStore.hasAnyPermission(permission)
    }
    return userStore.hasPermission(permission)
  }

  /**
   * 检查是否有所有指定权限
   */
  function hasAllPermissions(permissionList: string[]): boolean {
    return permissionList.every(p => userStore.hasPermission(p))
  }

  /**
   * 登出
   */
  async function logout(options?: { confirm?: boolean; redirect?: string }) {
    const { confirm = true, redirect = '/login' } = options ?? {}

    if (confirm) {
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        })
      }
      catch {
        // 用户取消
        return false
      }
    }

    userStore.logout()
    ElMessage.success('已退出登录')
    router.push(redirect)
    return true
  }

  /**
   * 检查登录状态，未登录则跳转到登录页
   */
  function checkAuth(requireAuth = true): boolean {
    if (requireAuth && !isLoggedIn.value) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return false
    }
    return true
  }

  /**
   * 跳转到登录页
   */
  function goToLogin(redirect?: string) {
    const path = redirect ?? router.currentRoute.value.fullPath
    router.push(`/login?redirect=${encodeURIComponent(path)}`)
  }

  return {
    // 状态
    isLoggedIn,
    username,
    roles,
    permissions,
    userInfo,
    // 方法
    hasRole,
    hasPermission,
    hasAllPermissions,
    logout,
    checkAuth,
    goToLogin,
  }
}
