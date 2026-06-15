// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 查询所有菜单 GET /system/menu */
export async function list1(options?: { [key: string]: any }) {
  return request<API.RListSysMenu>("/system/menu", {
    method: "GET",
    ...(options || {}),
  });
}

/** 创建菜单 POST /system/menu */
export async function create4(
  body: API.MenuDTO,
  options?: { [key: string]: any }
) {
  return request<API.RVoid>("/system/menu", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 根据ID查询菜单 GET /system/menu/${param0} */
export async function getById3(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getById3Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.SysMenu>(`/system/menu/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 更新菜单 PUT /system/menu/${param0} */
export async function update4(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.update4Params,
  body: API.MenuDTO,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/menu/${param0}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}

/** 删除菜单 DELETE /system/menu/${param0} */
export async function delete4(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.delete4Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/menu/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 查询子菜单列表 GET /system/menu/children/${param0} */
export async function getChildren(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getChildrenParams,
  options?: { [key: string]: any }
) {
  const { parentId: param0, ...queryParams } = params;
  return request<API.RListSysMenu>(`/system/menu/children/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 查询菜单树 GET /system/menu/tree */
export async function tree(options?: { [key: string]: any }) {
  return request<API.RListSysMenu>("/system/menu/tree", {
    method: "GET",
    ...(options || {}),
  });
}
