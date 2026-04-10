/**
 * 请求工具
 * 基于现有的 axiosInstance 封装
 */
import type {AxiosRequestConfig} from 'axios'
import {axiosInstance} from './api-mutator'
import type {R} from '@/types'

function resolveFilename(contentDisposition?: string, fallback = 'download.xlsx') {
  if (!contentDisposition) {
    return fallback
  }

  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1])
  }

  const match = contentDisposition.match(/filename="?([^"]+)"?/i)
  if (match?.[1]) {
    return decodeURIComponent(match[1])
  }

  return fallback
}

function triggerDownload(blob: Blob, filename: string) {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

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

  download: async (url: string, config?: AxiosRequestConfig, fallbackFilename?: string): Promise<void> => {
    const response = await axiosInstance.get(url, {
      ...config,
      responseType: 'blob',
    })
    const filename = resolveFilename(response.headers['content-disposition'], fallbackFilename)
    const blob = response.data instanceof Blob
      ? response.data
      : new Blob([response.data], { type: response.headers['content-type'] })
    triggerDownload(blob, filename || 'download.xlsx')
  },
}

export default request
