/**
 * 请求工具
 * 基于现有的 axiosInstance 封装
 */
import { axiosInstance } from './api-mutator'
import type { R } from '@/types'

// 请求封装
const request = {
  get: <T>(url: string, config?: any): Promise<T> =>
    axiosInstance.get<R<T>>(url, config).then(res => res.data.data),

  post: <T>(url: string, data?: any, config?: any): Promise<T> =>
    axiosInstance.post<R<T>>(url, data, config).then(res => res.data.data),

  put: <T>(url: string, data?: any, config?: any): Promise<T> =>
    axiosInstance.put<R<T>>(url, data, config).then(res => res.data.data),

  delete: <T>(url: string, config?: any): Promise<T> =>
    axiosInstance.delete<R<T>>(url, config).then(res => res.data.data),

  upload: <T>(url: string, file: File, fieldName = 'file'): Promise<T> => {
    const formData = new FormData()
    formData.append(fieldName, file)
    return axiosInstance.post<R<T>>(url, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then(res => res.data.data)
  },
}

export default request
