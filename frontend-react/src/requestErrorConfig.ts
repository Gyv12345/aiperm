/**
 * umi/max request 错误处理与响应/请求拦截器
 *
 * 后端约定：
 * - 鉴权：Sa-Token，请求头 Authorization 直接放裸 token（无 Bearer 前缀）。
 * - 统一响应 R<T> = { code, message, data }，code===200 为成功（兼容 0）。
 * - 分页 PageResult<T> = { total, list, pageNum, pageSize, pages }。
 * - HTTP 401：未登录 → 清 token，跳 /user/login。
 * - HTTP 423：需要二次验证（MFA）→ 派发全局 'mfa-required' 事件，由布局层弹窗处理。
 * - 业务 code 非 200：antd message.error 提示 message 字段。
 * - 文件下载（blob）：直接放行，不解包。
 *
 * 文档：https://umijs.org/docs/max/request
 */
import { message } from 'antd';
import type { RequestInterceptor, ResponseInterceptor } from '@umijs/max';

/** localStorage 中 token 的 key（getInitialState 与拦截器统一从此读取） */
export const TOKEN_KEY = 'token';
/** 业务成功码（200 为标准，0 兼容） */
const SUCCESS_CODES = new Set([0, 200]);
/** 防止 401 重复跳转 */
let isRedirecting = false;

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string | null) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}

/** 跳转登录页（带防重入） */
function redirectToLogin() {
  if (isRedirecting) return;
  isRedirecting = true;
  message.warning('登录状态已失效，请重新登录');
  setTimeout(() => {
    isRedirecting = false;
    const redirect = encodeURIComponent(
      window.location.pathname + window.location.search,
    );
    window.location.href = `/user/login?redirect=${redirect}`;
  }, 600);
}

/** 请求拦截器：注入 Authorization（无 Bearer 前缀） */
export const requestInterceptor: RequestInterceptor = (config: any) => {
  const token = getToken();
  if (token) {
    config.headers = {
      ...config.headers,
      Authorization: token,
    };
  }
  return config;
};

/**
 * 业务失败的静默错误标记：携带该标记的 Error 会被 app.tsx 中注册的
 * capture 阶段 unhandledrejection 监听器吞掉，不触发 React dev 全屏错误页。
 * 业务提示已在拦截器内通过 message.error 完成。
 */
export const SILENT_BUSINESS_ERROR = '__silentBusinessError';

/**
 * 响应拦截器：解包 R<T>.data
 * - blob/arraybuffer / 非 JSON：文件流或非业务响应，原样放行。
 * - JSON：业务码非 200 → message.error 提示 + reject 带标记的错误；成功 →
 *   把 response.data 改写为业务数据（R<T>.data），返回 response。
 *
 * 调用方的 try/catch 能感知失败。未被立即 catch 的 rejection 在 dev 环境会
 * 触发 React 全屏错误页——由 app.tsx 的 capture 阶段监听器按标记吞掉。
 */
export const responseInterceptor: ResponseInterceptor = (response: any) => {
  const responseType = response.config?.responseType;
  const headers = response.headers || {};
  const contentType: string =
    (typeof headers.get === 'function'
      ? headers.get('Content-Type')
      : headers['Content-Type']) || '';

  // 文件流直接放行
  if (
    responseType === 'blob' ||
    responseType === 'arraybuffer' ||
    contentType.includes('application/octet-stream') ||
    contentType.includes('application/vnd.ms-excel')
  ) {
    return response;
  }

  // 兜底：非 JSON 直接返回
  if (!contentType.includes('application/json')) {
    return response;
  }

  const data: API = response.data;
  if (data == null || typeof data !== 'object') return response;

  if (SUCCESS_CODES.has(data.code)) {
    // 解包：改写 response.data 为业务数据，axios 流程结束后 request() 即拿到它
    response.data = data.data;
    return response;
  }

  // 业务失败：提示后 reject 带静默标记的错误
  const reqUrl = `${response.config?.method?.toUpperCase() || ''} ${response.config?.url || ''}`;
  // eslint-disable-next-line no-console
  console.warn('[request] 业务失败:', reqUrl, data);
  const msg = data.message || data.msg || '请求失败';
  message.error(msg);
  const err = new Error(msg) as Error & { [SILENT_BUSINESS_ERROR]?: boolean };
  err[SILENT_BUSINESS_ERROR] = true;
  return Promise.reject(err);
};

/** umi request errorConfig：统一错误处理（针对 HTTP 层错误） */
export const errorHandler = (error: any, opts: any) => {
  // 业务失败已在 responseInterceptor 提示过，静默 reject 不重复处理
  if (error?.[SILENT_BUSINESS_ERROR]) {
    return Promise.reject(error);
  }
  const response = error?.response;
  if (!response) {
    // 网络错误 / 超时
    const msg = error?.message || '网络异常，请稍后重试';
    if (msg.includes('timeout')) {
      message.error('请求超时，请稍后重试');
    }
    return Promise.reject(error);
  }

  const status: number = response.status;

  // 401：未登录
  if (status === 401) {
    setToken(null);
    redirectToLogin();
    return Promise.reject(error);
  }

  // 423：需要 MFA 二次验证
  if (status === 423) {
    window.dispatchEvent(new CustomEvent('mfa-required'));
    return Promise.reject(error);
  }

  // 403：无权限
  if (status === 403) {
    message.error('没有访问权限');
    return Promise.reject(error);
  }

  // 404：静默
  if (status === 404) {
    return Promise.reject(error);
  }

  // 其余：尝试读后端 message
  response
    ?.clone?.()
    ?.json?.()
    ?.then?.((body: API) => {
      message.error(body?.message || body?.msg || `请求错误（${status}）`);
    })
    ?.catch?.(() => {});

  return Promise.reject(error);
};

/** 透传给 config.request 的配置对象 */
export const requestConfig = {
  baseURL: '/api',
  timeout: 30000,
  requestInterceptors: [requestInterceptor],
  responseInterceptors: [responseInterceptor],
  errorConfig: {
    errorThrower: () => {},
    errorHandler,
  },
};
