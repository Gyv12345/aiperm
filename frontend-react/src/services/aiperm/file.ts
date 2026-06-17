/**
 * 文件管理服务（手写，未走 openapi 生成）。
 *
 * 后端接口（SysFileController）：
 * - GET    /system/file          分页查询
 * - GET    /system/file/{id}     详情
 * - POST   /system/file/upload   上传（multipart/form-data）
 * - DELETE /system/file/{id}     删除
 */
import { request } from '@umijs/max';

/** 分页查询文件列表 GET /system/file */
export async function pageFile(
  params: { dto: API.FileDTO },
  options?: { [key: string]: any },
) {
  return request<API.PageResultSysFile>('/system/file', {
    method: 'GET',
    params,
    ...(options || {}),
  });
}

/** 文件详情 GET /system/file/{id} */
export async function getFile(
  params: { id: number },
  options?: { [key: string]: any },
) {
  const { id } = params;
  return request<API.SysFile>(`/system/file/${id}`, {
    method: 'GET',
    ...(options || {}),
  });
}

/**
 * 上传文件 POST /system/file/upload
 * 使用 FormData 发送 multipart/form-data（不要手动设 Content-Type，浏览器会自动加 boundary）。
 */
export async function uploadFile(file: File | Blob, options?: { [key: string]: any }) {
  const formData = new FormData();
  formData.append('file', file);
  return request<API.SysFile>('/system/file/upload', {
    method: 'POST',
    data: formData,
    requestType: 'form',
    ...(options || {}),
  });
}

/** 删除文件 DELETE /system/file/{id} */
export async function deleteFile(
  params: { id: number },
  options?: { [key: string]: any },
) {
  const { id } = params;
  return request<void>(`/system/file/${id}`, {
    method: 'DELETE',
    ...(options || {}),
  });
}
