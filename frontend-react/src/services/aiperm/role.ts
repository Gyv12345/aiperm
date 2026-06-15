// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 分页查询角色列表 GET /system/role */
export async function page1(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.page1Params,
  options?: { [key: string]: any }
) {
  return request<API.PageResultSysRole>("/system/role", {
    method: "GET",
    params: {
      ...params,
      dto: undefined,
      ...params["dto"],
    },
    ...(options || {}),
  });
}

/** 创建角色 POST /system/role */
export async function create1(
  body: API.RoleDTO,
  options?: { [key: string]: any }
) {
  return request<API.RVoid>("/system/role", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 根据ID查询角色 GET /system/role/${param0} */
export async function getById1(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getById1Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.SysRole>(`/system/role/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 更新角色 PUT /system/role/${param0} */
export async function update1(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.update1Params,
  body: API.RoleDTO,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/role/${param0}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}

/** 删除角色 DELETE /system/role/${param0} */
export async function delete1(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.delete1Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/role/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 获取角色的菜单ID列表 GET /system/role/${param0}/menus */
export async function getRoleMenus(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getRoleMenusParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RListLong>(`/system/role/${param0}/menus`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 分配角色菜单 POST /system/role/${param0}/menus */
export async function assignMenus(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.assignMenusParams,
  body: number[],
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/role/${param0}/menus`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}

/** 查询所有角色 GET /system/role/all */
export async function list8(options?: { [key: string]: any }) {
  return request<API.RListSysRole>("/system/role/all", {
    method: "GET",
    ...(options || {}),
  });
}
