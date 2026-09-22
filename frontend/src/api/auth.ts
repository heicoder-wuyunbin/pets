import request from '@/utils/request';
import type { ApiResult } from '@/types/api';
import type { LoginParams, LoginResult, SelectShopParams, RegisterParams, SendCodeParams, ResetPasswordParams } from '@/types/api';

/** 登录 */
export function login(data: LoginParams) {
  return request.post<ApiResult<LoginResult>>('/auth/login', data) as unknown as Promise<ApiResult<LoginResult>>;
}

/** 选店（携带预授权 token，网关解析后注入 X-User-Id） */
export function selectShop(data: SelectShopParams) {
  return request.post<ApiResult<LoginResult>>('/auth/select-shop', data) as unknown as Promise<ApiResult<LoginResult>>;
}

/** 注册 */
export function register(data: RegisterParams) {
  return request.post<ApiResult<null>>('/auth/register', data) as unknown as Promise<ApiResult<null>>;
}

/** 发送验证码（注册 / 重置密码） */
export function sendCode(data: SendCodeParams) {
  return request.post<ApiResult<null>>('/auth/send-code', data) as unknown as Promise<ApiResult<null>>;
}

/** 重置密码 */
export function resetPassword(data: ResetPasswordParams) {
  return request.post<ApiResult<null>>('/auth/reset-password', data) as unknown as Promise<ApiResult<null>>;
}
