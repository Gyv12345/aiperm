/**
 * 第三方社交登录 service（手写过渡）
 *
 * 对应后端 SocialAuthController 接口，响应已被 requestErrorConfig 自动解包（R<T>.data）。
 *
 * ⚠️ 说明：本文件为手写过渡封装。后端重启加载 social 模块后，可改用
 *    `pnpm run generate:api` 从 Swagger 生成正式 service（路径一致），
 *    届时用生成版本替换本文件即可。
 */
import { request } from '@umijs/max';

/** 第三方账号绑定信息 */
export interface SocialBindingVO {
  source: string;
  nickname?: string;
  avatar?: string;
  email?: string;
  boundAt?: string;
}

/** 授权链接响应 */
export interface SocialAuthorizeVO {
  authorizeUrl: string;
}

/** 获取第三方授权链接（action: login=登录页登录, bind=个人中心绑定） */
export async function socialAuthorize(platform: string, action: 'login' | 'bind' = 'login') {
  return request<SocialAuthorizeVO>('/auth/social/authorize', {
    method: 'GET',
    params: { platform, action },
  });
}

/** 我的第三方账号绑定列表 */
export async function getSocialBindings() {
  return request<SocialBindingVO[]>('/profile/social/bindings', { method: 'GET' });
}

/** 解绑第三方账号 */
export async function unbindSocial(platform: string) {
  return request<void>('/profile/social/unbind', { method: 'DELETE', params: { platform } });
}
