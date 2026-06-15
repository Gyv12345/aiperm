// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 删除文件 DELETE /oss */
export async function delete14(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.delete14Params,
  options?: { [key: string]: any }
) {
  return request<API.RVoid>("/oss", {
    method: "DELETE",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 上传文件 POST /oss/upload */
export async function upload(body: {}, options?: { [key: string]: any }) {
  return request<API.OssResult>("/oss/upload", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
