// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 分页查询公告 GET /enterprise/notice */
export async function list4(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.list4Params,
  options?: { [key: string]: any }
) {
  return request<API.PageResultNoticeVO>("/enterprise/notice", {
    method: "GET",
    params: {
      ...params,
      dto: undefined,
      ...params["dto"],
    },
    ...(options || {}),
  });
}

/** 创建公告 POST /enterprise/notice */
export async function create9(
  body: API.NoticeDTO,
  options?: { [key: string]: any }
) {
  return request<API.RLong>("/enterprise/notice", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 查询公告详情 GET /enterprise/notice/${param0} */
export async function detail3(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.detail3Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.NoticeVO>(`/enterprise/notice/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 更新公告 PUT /enterprise/notice/${param0} */
export async function update10(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.update10Params,
  body: API.NoticeDTO,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/enterprise/notice/${param0}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}

/** 删除公告 DELETE /enterprise/notice/${param0} */
export async function delete9(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.delete9Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/enterprise/notice/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 发布公告 PUT /enterprise/notice/${param0}/publish */
export async function publish(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.publishParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/enterprise/notice/${param0}/publish`, {
    method: "PUT",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 撤回公告 PUT /enterprise/notice/${param0}/withdraw */
export async function withdraw(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.withdrawParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/enterprise/notice/${param0}/withdraw`, {
    method: "PUT",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 首页公告通知流 GET /enterprise/notice/feed */
export async function feed(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.feedParams,
  options?: { [key: string]: any }
) {
  return request<API.RListNoticeVO>("/enterprise/notice/feed", {
    method: "GET",
    params: {
      // limit has a default value: 10
      limit: "10",
      ...params,
    },
    ...(options || {}),
  });
}

/** 查询已发布公告 GET /enterprise/notice/published */
export async function published(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.publishedParams,
  options?: { [key: string]: any }
) {
  return request<API.RListNoticeVO>("/enterprise/notice/published", {
    method: "GET",
    params: {
      // limit has a default value: 10
      limit: "10",
      ...params,
    },
    ...(options || {}),
  });
}
