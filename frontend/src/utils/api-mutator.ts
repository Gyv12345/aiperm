import axios, { type AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

// 错误码映射
const ERROR_MESSAGES: Record<number, string> = {
  400: '请求参数错误',
  401: '登录已过期，请重新登录',
  403: '没有权限访问该资源',
  404: '请求的资源不存在',
  405: '请求方法不允许',
  408: '请求超时',
  500: '服务器内部错误',
  502: '网关错误',
  503: '服务暂不可用',
  504: '网关超时',
}

// 是否正在刷新登录状态
let isRefreshing = false

// 创建 axios 实例
export const axiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('access_token')
    if (token) {
      // Sa-Token 默认直接读取 token 值，不需要 Bearer 前缀
      config.headers.Authorization = token
    }
    return config
  },
  (error: AxiosError) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  },
)

// 响应拦截器
axiosInstance.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response

    // 如果返回的是文件流，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }

    // 业务状态码判断（根据后端实际返回调整）
    if (data.code && data.code !== 200 && data.code !== 0) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
    }

    return response
  },
  async (error: AxiosError<{ message?: string }>) => {
    const { response, config } = error

    if (!response) {
      // 网络错误或请求被取消
      if (error.code === 'ECONNABORTED') {
        ElMessage.error('请求超时，请稍后重试')
      }
      else if (!axios.isCancel(error)) {
        ElMessage.error('网络连接异常，请检查网络')
      }
      return Promise.reject(error)
    }

    const { status, data } = response
    const message = data?.message || ERROR_MESSAGES[status] || '请求失败'

    switch (status) {
      case 401:
        // Token 过期
        if (!isRefreshing) {
          isRefreshing = true
          localStorage.removeItem('access_token')

          try {
            await ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
              confirmButtonText: '重新登录',
              cancelButtonText: '取消',
              type: 'warning',
            })
          }
          catch {
            // 用户取消
          }
          finally {
            isRefreshing = false
            window.location.href = '/login'
          }
        }
        break

      case 403:
        ElMessage.error(message)
        break

      case 404:
        // 静默处理 404，某些场景下是正常的
        console.warn('资源不存在:', config?.url)
        break

      case 500:
        ElMessage.error(message)
        console.error('服务器错误:', data)
        break

      case 423:
        // 需要2FA验证，触发全局事件
        window.dispatchEvent(new CustomEvent('mfa-required', {
          detail: { message }
        }))
        break

      default:
        ElMessage.error(message)
    }

    return Promise.reject(error)
  },
)

// 导出给 Orval 使用的 mutator 函数
export const customFetch = <T>(config: InternalAxiosRequestConfig): Promise<T> => {
  return axiosInstance.request<any, T>(config)
}
