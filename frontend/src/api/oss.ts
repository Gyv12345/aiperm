/**
 * 文件管理 API
 * 对应后端 OssController (/oss)
 */
import request from '@/utils/request'

// ==================== 类型定义 ====================

/** 上传结果 */
export interface OssResult {
  fileName: string
  originalName: string
  url: string
  size: number
  contentType: string
}

// ==================== API 函数 ====================

export const ossApi = {
  /** 上传文件 */
  upload: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post<OssResult>('/oss/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },

  /** 删除文件 */
  delete: (fileName: string) =>
    request.delete<void>('/oss', { params: { fileName } }),
}
