// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 确认绑定（验证TOTP码后持久化） POST /mfa/bind/confirm */
export async function confirmBind(
  body: API.MfaVerifyDTO,
  options?: { [key: string]: any }
) {
  return request<API.RVoid>("/mfa/bind/confirm", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 获取绑定二维码（TOTP URI） GET /mfa/bind/qrcode */
export async function qrcode(options?: { [key: string]: any }) {
  return request<API.MfaQrcodeVO>("/mfa/bind/qrcode", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取2FA绑定状态 GET /mfa/status */
export async function status(options?: { [key: string]: any }) {
  return request<API.MfaStatusVO>("/mfa/status", {
    method: "GET",
    ...(options || {}),
  });
}

/** 解绑2FA（需先验证） POST /mfa/unbind */
export async function unbind(
  body: API.MfaVerifyDTO,
  options?: { [key: string]: any }
) {
  return request<API.RVoid>("/mfa/unbind", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 验证2FA（敏感操作前调用，写入Redis有效期） POST /mfa/verify */
export async function verify(
  body: API.MfaVerifyDTO,
  options?: { [key: string]: any }
) {
  return request<API.RVoid>("/mfa/verify", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
