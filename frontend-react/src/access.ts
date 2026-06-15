/**
 * umi/max access 权限定义
 *
 * 用法：
 *   import { Access, useAccess } from '@umijs/max';
 *   <Access accessible={access.canPermission('system:user:create')}>
 *     <Button>新增</Button>
 *   </Access>
 *
 * 权限来源：getInitialState 拉取 /auth/info 得到 { roles, permissions }。
 * 权限码格式若依风格：module:resource:action（如 system:user:create）。
 *
 * 文档：https://umijs.org/docs/max/access
 */
/** access 所需的初始状态结构（与 src/app.tsx 的 InitialState 对齐） */
interface AccessState {
  currentUser?: {
    roles?: string[];
    permissions?: string[];
  };
}

export default function access(initialState: AccessState | undefined) {
  const { currentUser } = initialState ?? {};
  const roles = currentUser?.roles ?? [];
  const permissions = currentUser?.permissions ?? [];
  const isSuperAdmin = roles.includes('admin');

  return {
    /** 已登录普通用户即可访问 */
    canUser: !!currentUser,

    /** 超管 */
    canAdmin: isSuperAdmin,

    /** 是否拥有某权限（超管直接放行） */
    canPermission: (perm: string) =>
      isSuperAdmin || permissions.includes(perm),

    /** 拥有任一权限即放行 */
    canAnyPermission: (perms: string[]) =>
      isSuperAdmin || perms.some((p) => permissions.includes(p)),

    /** 拥有全部权限 */
    canAllPermission: (perms: string[]) =>
      isSuperAdmin || perms.every((p) => permissions.includes(p)),

    /** 是否拥有某角色 */
    canRole: (role: string) => isSuperAdmin || roles.includes(role),

    /** 是否拥有任一角色 */
    canAnyRole: (rs: string[]) =>
      isSuperAdmin || rs.some((r) => roles.includes(r)),
  };
}
