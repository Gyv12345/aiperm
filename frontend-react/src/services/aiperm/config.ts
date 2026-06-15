// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 分页查询系统配置 GET /enterprise/config */
export async function list7(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.list7Params,
  options?: { [key: string]: any }
) {
  return request<API.PageResultConfigVO>("/enterprise/config", {
    method: "GET",
    params: {
      ...params,
      dto: undefined,
      ...params["dto"],
    },
    ...(options || {}),
  });
}

/** 创建系统配置 POST /enterprise/config */
export async function create11(
  body: API.ConfigDTO,
  options?: { [key: string]: any }
) {
  return request<API.RLong>("/enterprise/config", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 查询系统配置详情 GET /enterprise/config/${param0} */
export async function detail5(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.detail5Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.ConfigVO>(`/enterprise/config/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 更新系统配置 PUT /enterprise/config/${param0} */
export async function update12(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.update12Params,
  body: API.ConfigDTO,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/enterprise/config/${param0}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}

/** 删除系统配置 DELETE /enterprise/config/${param0} */
export async function delete11(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.delete11Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/enterprise/config/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 根据配置键查询 GET /enterprise/config/key/${param0} */
export async function getByKey(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getByKeyParams,
  options?: { [key: string]: any }
) {
  const { configKey: param0, ...queryParams } = params;
  return request<API.ConfigVO>(`/enterprise/config/key/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}
