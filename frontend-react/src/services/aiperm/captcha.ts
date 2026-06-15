// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 发送验证码（短信或邮件） POST /captcha/send */
export async function send1(
  body: API.SendCaptchaDTO,
  options?: { [key: string]: any }
) {
  return request<API.RVoid>("/captcha/send", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
