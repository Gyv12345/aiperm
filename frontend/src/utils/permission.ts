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
