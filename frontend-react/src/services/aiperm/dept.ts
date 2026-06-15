// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 查询所有部门 GET /system/dept */
export async function list3(options?: { [key: string]: any }) {
  return request<API.RListSysDept>("/system/dept", {
    method: "GET",
    ...(options || {}),
  });
}

/** 创建部门 POST /system/dept */
export async function create7(
  body: API.DeptDTO,
  options?: { [key: string]: any }
) {
  return request<API.RVoid>("/system/dept", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 根据ID查询部门 GET /system/dept/${param0} */
export async function getById4(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getById4Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.SysDept>(`/system/dept/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 更新部门 PUT /system/dept/${param0} */
export async function update8(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.update8Params,
  body: API.DeptDTO,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/dept/${param0}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}

/** 删除部门 DELETE /system/dept/${param0} */
export async function delete7(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.delete7Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/dept/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 查询子部门列表 GET /system/dept/children/${param0} */
export async function getChildren1(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getChildren1Params,
  options?: { [key: string]: any }
) {
  const { parentId: param0, ...queryParams } = params;
  return request<API.RListSysDept>(`/system/dept/children/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 查询部门树 GET /system/dept/tree */
export async function tree1(options?: { [key: string]: any }) {
  return request<API.RListSysDept>("/system/dept/tree", {
    method: "GET",
    ...(options || {}),
  });
}
