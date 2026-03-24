import {defineStore} from 'pinia'
import {computed, ref, shallowRef} from 'vue'
import {authApi} from '@/api/auth'

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
    // 使用 shallowRef 优化大对象性能
    const token = ref<string | null>(null)
    const userInfo = shallowRef<UserInfo | null>(null)

    // 计算属性
    const isLoggedIn = computed(() => !!token.value)
    const username = computed(() => userInfo.value?.username ?? '')
    const roles = computed(() => userInfo.value?.roles ?? [])
    const permissions = computed(() => userInfo.value?.permissions ?? [])

    // 设置 Token
    function setToken(newToken: string) {
      token.value = newToken
    }

    // 设置用户信息
    function setUserInfo(info: UserInfo) {
      userInfo.value = info
    }

    // 从后端获取用户信息
    async function fetchUserInfo() {
      const data = await authApi.info()
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

    // 检查是否有指定角色
    function hasRole(role: string): boolean {
      return roles.value.includes(role)
    }

    // 检查是否有指定权限
    function hasPermission(permission: string): boolean {
      return permissions.value.includes(permission)
    }

    // 检查是否有任一权限
    function hasAnyPermission(permissionList: string[]): boolean {
      return permissionList.some(p => hasPermission(p))
    }

    // 登出
    async function logout() {
      try {
        await authApi.logout()
      }
      catch {
        // 忽略登出错误
      }
      finally {
        token.value = null
        userInfo.value = null
      }
    }

    return {
      // 状态
      token,
      userInfo,
      // 计算属性
      isLoggedIn,
      username,
      roles,
      permissions,
      // 方法
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
