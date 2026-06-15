// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 分页查询消息 GET /enterprise/message */
export async function list5(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.list5Params,
  options?: { [key: string]: any }
) {
  return request<API.PageResultMessageVO>("/enterprise/message", {
    method: "GET",
    params: {
      ...params,
      dto: undefined,
      ...params["dto"],
    },
    ...(options || {}),
  });
}

/** 发送消息 POST /enterprise/message */
export async function send(
  body: API.MessageDTO,
  options?: { [key: string]: any }
) {
  return request<API.RLong>("/enterprise/message", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 查询消息详情 GET /enterprise/message/${param0} */
export async function detail6(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.detail6Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.MessageVO>(`/enterprise/message/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 删除消息 DELETE /enterprise/message/${param0} */
export async function delete13(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.delete13Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/enterprise/message/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 标记消息为已读 PUT /enterprise/message/${param0}/read */
export async function markAsRead(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.markAsReadParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/enterprise/message/${param0}/read`, {
    method: "PUT",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 批量标记消息为已读 PUT /enterprise/message/read-all */
export async function markAllAsRead(options?: { [key: string]: any }) {
  return request<API.RMapStringInteger>("/enterprise/message/read-all", {
    method: "PUT",
    ...(options || {}),
  });
}

/** 批量标记指定消息为已读 PUT /enterprise/message/read-batch */
export async function markAsReadByIds(
  body: API.MessageDTO,
  options?: { [key: string]: any }
) {
  return request<API.RMapStringInteger>("/enterprise/message/read-batch", {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 获取消息接收人列表 GET /enterprise/message/receivers */
export async function receivers(options?: { [key: string]: any }) {
  return request<API.RListMessageReceiverVO>("/enterprise/message/receivers", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取未读消息数量 GET /enterprise/message/unread-count */
export async function unreadCount(options?: { [key: string]: any }) {
  return request<API.RInteger>("/enterprise/message/unread-count", {
    method: "GET",
    ...(options || {}),
  });
}
