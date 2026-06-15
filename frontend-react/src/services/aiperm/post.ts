// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 分页查询岗位列表 GET /system/post */
export async function page2(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.page2Params,
  options?: { [key: string]: any }
) {
  return request<API.PageResultSysPost>("/system/post", {
    method: "GET",
    params: {
      ...params,
      dto: undefined,
      ...params["dto"],
    },
    ...(options || {}),
  });
}

/** 创建岗位 POST /system/post */
export async function create2(
  body: API.PostDTO,
  options?: { [key: string]: any }
) {
  return request<API.RVoid>("/system/post", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 根据ID查询岗位 GET /system/post/${param0} */
export async function getById2(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getById2Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.SysPost>(`/system/post/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 更新岗位 PUT /system/post/${param0} */
export async function update2(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.update2Params,
  body: API.PostDTO,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/post/${param0}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}

/** 删除岗位 DELETE /system/post/${param0} */
export async function delete2(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.delete2Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/post/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 查询所有岗位 GET /system/post/all */
export async function list9(options?: { [key: string]: any }) {
  return request<API.RListSysPost>("/system/post/all", {
    method: "GET",
    ...(options || {}),
  });
}
