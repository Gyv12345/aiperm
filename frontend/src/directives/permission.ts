import type { Directive, DirectiveBinding } from 'vue'
import { hasPermission } from '@/utils/permission'
import { useUserStore } from '@/stores/user'

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

    const hasAuth = Array.isArray(value)
      ? value.some(p => hasPermission(p))
      : hasPermission(value)

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
    const userStore = useUserStore()
    const value = binding.value

    if (!value) {
      return
    }

    const hasAuth = Array.isArray(value)
      ? value.some(r => userStore.hasRole(r))
      : userStore.hasRole(value)

    if (!hasAuth) {
      el.parentNode?.removeChild(el)
    }
  },
}

export default {
  permission,
  role,
}
