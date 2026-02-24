import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface UserInfo {
  id: string
  username: string
  email?: string
  roles?: string[]
}

export const useUserStore = defineStore(
  'user',
  () => {
    const token = ref<string | null>(localStorage.getItem('access_token'))
    const userInfo = ref<UserInfo | null>(null)

    const isLoggedIn = computed(() => !!token.value)

    function setToken(newToken: string) {
      token.value = newToken
      localStorage.setItem('access_token', newToken)
    }

    function setUserInfo(info: UserInfo) {
      userInfo.value = info
    }

    function logout() {
      token.value = null
      userInfo.value = null
      localStorage.removeItem('access_token')
    }

    return {
      token,
      userInfo,
      isLoggedIn,
      setToken,
      setUserInfo,
      logout,
    }
  },
)
