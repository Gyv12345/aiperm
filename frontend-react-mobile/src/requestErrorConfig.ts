/**
 * umi/max request 错误处理与响应/请求拦截器（移动端）
 *
 * 与桌面端 frontend-react/src/requestErrorConfig.ts 逻辑一致，唯一差异：
 * 提示组件由 antd 的 message 换成 antd-mobile 的 Toast。
 *
 * 后端约定：
 * - 鉴权：Sa-Token，请求头 Authorization 直接放裸 token（无 Bearer 前缀）。
 * - 统一响应 R<T> = { code, message, data }，code===200 为成功（兼容 0）。
 * - 分页 PageResult<T> = { total, list, pageNum, pageSize, pages }。
 * - HTTP 401：未登录 → 清 token，跳 /user/login。
 * - HTTP 423：需要二次验证（MFA）→ 派发 'mfa-required' 事件（Phase 4 由 MfaVerifyDialog 处理）。
 * - 业务 code 非 200：Toast 提示 message 字段。
 *
 * 文档：https://umijs.org/docs/max/request
 */
import { Toast } from 'antd-mobile';
import type { RequestInterceptor, ResponseInterceptor } from '@umijs/max';

/** localStorage 中 token 的 key */
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
  Toast.show({ content: '登录状态已失效，请重新登录', icon: 'fail' });
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
 * 业务失败的静默错误标记：携带该标记的 Error 用于避免 errorHandler 重复提示。
 */
export const SILENT_BUSINESS_ERROR = '__silentBusinessError';

/**
 * 响应拦截器：解包 R<T>.data
 * - 文件流（blob/arraybuffer/非 JSON）原样放行。
 * - JSON：业务码非 200 → Toast 提示 + reject 带标记的错误；成功 → 改写为业务数据。
 */
export const responseInterceptor: ResponseInterceptor = (response: any) => {
  const responseType = response.config?.responseType;
  const headers = response.headers || {};
  const contentType: string =
    (typeof headers.get === 'function'
      ? headers.get('Content-Type')
      : headers['Content-Type']) || '';

  if (
    responseType === 'blob' ||
    responseType === 'arraybuffer' ||
    contentType.includes('application/octet-stream') ||
    contentType.includes('application/vnd.ms-excel')
  ) {
    return response;
  }

  if (!contentType.includes('application/json')) {
    return response;
  }

  const data: API = response.data;
  if (data == null || typeof data !== 'object') return response;

  if (SUCCESS_CODES.has(data.code)) {
    response.data = data.data;
    return response;
  }

  const reqUrl = `${response.config?.method?.toUpperCase() || ''} ${response.config?.url || ''}`;
  // eslint-disable-next-line no-console
  console.warn('[request] 业务失败:', reqUrl, data);
  const msg = data.message || data.msg || '请求失败';
  Toast.show({ content: msg, icon: 'fail' });
  const err = new Error(msg) as Error & { [SILENT_BUSINESS_ERROR]?: boolean };
  err[SILENT_BUSINESS_ERROR] = true;
  return Promise.reject(err);
};

/** umi request errorConfig：统一错误处理（HTTP 层） */
export const errorHandler = (error: any) => {
  if (error?.[SILENT_BUSINESS_ERROR]) {
    return;
  }
  const response = error?.response;
  if (!response) {
    const msg = error?.message || '网络异常，请稍后重试';
    if (msg.includes('timeout')) {
      Toast.show({ content: '请求超时，请稍后重试', icon: 'fail' });
    }
    return;
  }

  const status: number = response.status;

  if (status === 401) {
    setToken(null);
    redirectToLogin();
    return;
  }

  if (status === 423) {
    // MFA 二次验证：Phase 4 由 MfaVerifyDialog 监听
    window.dispatchEvent(new CustomEvent('mfa-required'));
    return;
  }

  if (status === 403) {
    Toast.show({ content: '没有访问权限', icon: 'fail' });
    return;
  }

  if (status === 404) {
    return;
  }

  response
    ?.clone?.()
    ?.json?.()
    ?.then?.((body: API) => {
      Toast.show({
        content: body?.message || body?.msg || `请求错误（${status}）`,
        icon: 'fail',
      });
    })
    ?.catch?.(() => {});
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
