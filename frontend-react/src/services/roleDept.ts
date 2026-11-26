/**
 * 角色自定义部门 API（手写过渡）
 *
 * 对应后端 SysRoleController 新增的 GET /system/role/{id}/depts。
 * 该接口尚不在生成的 services/aiperm/role.ts 中，后端重启后执行
 * `pnpm run generate:api` 可生成正式版本，届时用生成版本替换本文件。
 */
import { request } from '@umijs/max';

/** 获取角色的自定义部门ID列表（dataScope=5 自定义部门时使用） */
export async function getRoleDepts(id: number) {
  return request<number[]>(`/system/role/${id}/depts`, {
    method: 'GET',
  });
}
