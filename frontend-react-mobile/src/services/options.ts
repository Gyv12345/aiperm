/**
 * 表单下拉数据源：部门 / 角色 / 岗位。
 * 统一返回 { label, value }[] 供 Selector/Picker 使用。
 */
import { list3 as deptList } from '@/services/aiperm/dept';
import { page1 as rolePage } from '@/services/aiperm/role';
import { list9 as postList } from '@/services/aiperm/post';

export type Option = { label: string; value: number };

/** 部门下拉 */
export async function getDeptOptions(): Promise<Option[]> {
  try {
    const res: any = await deptList();
    const list = Array.isArray(res) ? res : res?.list ?? [];
    return (list as any[]).map((d) => ({ label: d.deptName ?? d.name ?? '', value: d.id }));
  } catch {
    return [];
  }
}

/** 角色下拉（拉前 100 个） */
export async function getRoleOptions(): Promise<Option[]> {
  try {
    // 生成的 page1Params 要求完整 RoleDTO，实际 DTO 字段均为可选；用 as 绕过结构类型校验
    const res: any = await rolePage({ dto: { page: 1, pageSize: 100 } } as API.page1Params);
    const list = res?.list ?? [];
    return (list as any[]).map((r) => ({ label: r.roleName ?? r.name ?? '', value: r.id }));
  } catch {
    return [];
  }
}

/** 岗位下拉 */
export async function getPostOptions(): Promise<Option[]> {
  try {
    const res: any = await postList();
    const list = Array.isArray(res) ? res : res?.list ?? [];
    return (list as any[]).map((p) => ({ label: p.postName ?? p.name ?? '', value: p.id }));
  } catch {
    return [];
  }
}
