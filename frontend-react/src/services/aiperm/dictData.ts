// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 创建字典数据 POST /system/dict/data */
export async function create6(
  body: API.DictDataDTO,
  options?: { [key: string]: any }
) {
  return request<API.RVoid>("/system/dict/data", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 更新字典数据 PUT /system/dict/data/${param0} */
export async function update7(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.update7Params,
  body: API.DictDataDTO,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/dict/data/${param0}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}

/** 删除字典数据 DELETE /system/dict/data/${param0} */
export async function delete6(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.delete6Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/dict/data/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 导出字典数据 GET /system/dict/data/export */
export async function export2(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.export2Params,
  options?: { [key: string]: any }
) {
  return request<any>("/system/dict/data/export", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 导入字典数据 POST /system/dict/data/import */
export async function importData(body: {}, options?: { [key: string]: any }) {
  return request<API.ImportResultVO>("/system/dict/data/import", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 下载字典数据导入模板 GET /system/dict/data/import-template */
export async function importTemplate1(options?: { [key: string]: any }) {
  return request<any>("/system/dict/data/import-template", {
    method: "GET",
    ...(options || {}),
  });
}

/** 根据字典类型查询字典数据 GET /system/dict/data/list */
export async function listByDictType(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listByDictTypeParams,
  options?: { [key: string]: any }
) {
  return request<API.RListDictDataVO>("/system/dict/data/list", {
    method: "GET",
    params: {
      ...params,
      dto: undefined,
      ...params["dto"],
    },
    ...(options || {}),
  });
}
