// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 分页查询字典类型 GET /system/dict/type */
export async function list2(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.list2Params,
  options?: { [key: string]: any }
) {
  return request<API.PageResultDictTypeVO>("/system/dict/type", {
    method: "GET",
    params: {
      ...params,
      dto: undefined,
      ...params["dto"],
    },
    ...(options || {}),
  });
}

/** 创建字典类型 POST /system/dict/type */
export async function create5(
  body: API.DictTypeDTO,
  options?: { [key: string]: any }
) {
  return request<API.RLong>("/system/dict/type", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 查询字典类型详情 GET /system/dict/type/${param0} */
export async function detail1(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.detail1Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.DictTypeVO>(`/system/dict/type/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 更新字典类型 PUT /system/dict/type/${param0} */
export async function update6(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.update6Params,
  body: API.DictTypeDTO,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/dict/type/${param0}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}

/** 删除字典类型 DELETE /system/dict/type/${param0} */
export async function delete5(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.delete5Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/dict/type/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 查询所有启用的字典类型 GET /system/dict/type/all */
export async function all(options?: { [key: string]: any }) {
  return request<API.RListDictTypeVO>("/system/dict/type/all", {
    method: "GET",
    ...(options || {}),
  });
}

/** 导出字典类型 GET /system/dict/type/export */
export async function export1(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.export1Params,
  options?: { [key: string]: any }
) {
  return request<any>("/system/dict/type/export", {
    method: "GET",
    params: {
      ...params,
      dto: undefined,
      ...params["dto"],
    },
    ...(options || {}),
  });
}
