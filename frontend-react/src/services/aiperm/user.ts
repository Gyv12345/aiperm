// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 分页查询用户列表 GET /system/user */
export async function page(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.pageParams,
  options?: { [key: string]: any }
) {
  return request<API.PageResultUserVO>("/system/user", {
    method: "GET",
    params: {
      ...params,
      dto: undefined,
      ...params["dto"],
    },
    ...(options || {}),
  });
}

/** 创建用户 POST /system/user */
export async function create(
  body: API.UserDTO,
  options?: { [key: string]: any }
) {
  return request<API.RVoid>("/system/user", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 根据ID查询用户 GET /system/user/${param0} */
export async function getById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getByIdParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.UserVO>(`/system/user/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 更新用户 PUT /system/user/${param0} */
export async function update(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateParams,
  body: API.UserDTO,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/user/${param0}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}

/** 删除用户 DELETE /system/user/${param0} */
export async function deleteUsingDelete(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteUsingDELETEParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/user/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 重置用户密码 PUT /system/user/${param0}/reset-password */
export async function resetPassword(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.resetPasswordParams,
  body: API.UserDTO,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/user/${param0}/reset-password`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}

/** 修改用户状态 PUT /system/user/${param0}/status */
export async function changeStatus(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.changeStatusParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/user/${param0}/status`, {
    method: "PUT",
    params: {
      ...queryParams,
    },
    ...(options || {}),
  });
}

/** 批量删除用户 DELETE /system/user/batch */
export async function deleteBatch(
  body: number[],
  options?: { [key: string]: any }
) {
  return request<API.RVoid>("/system/user/batch", {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 导出用户 GET /system/user/export */
export async function exportUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.exportUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<any>("/system/user/export", {
    method: "GET",
    params: {
      ...params,
      dto: undefined,
      ...params["dto"],
    },
    ...(options || {}),
  });
}

/** 导入用户 POST /system/user/import */
export async function importUsers(body: {}, options?: { [key: string]: any }) {
  return request<API.ImportResultVO>("/system/user/import", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 下载用户导入模板 GET /system/user/import-template */
export async function importTemplate(options?: { [key: string]: any }) {
  return request<any>("/system/user/import-template", {
    method: "GET",
    ...(options || {}),
  });
}
